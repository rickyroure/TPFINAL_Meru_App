// Datos de prueba que imitan las respuestas del backend:
//  - RutinaResponseDTO (GET /rutinas/alumno/{id}, GET /rutinas/profesor/{id}, POST /rutinas/...)
//  - ProgresoSetResponseDTO (GET/POST /progreso-sets)
// Las rutinas creadas/asignadas y el progreso se guardan en el navegador para no perderse al recargar.

import { EJERCICIOS } from './ejercicios'
import { listarAlumnosAsignados } from './profesores'

const CLAVE_PROGRESO = 'meru_mock_progreso'
const CLAVE_RUTINAS = 'meru_mock_rutinas'

const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

const hoyISO = () => new Date().toISOString().slice(0, 10)

// Arma los ejercicios de una rutina de ejemplo. Los IDs son únicos para que cada uno tenga su propio progreso.
// El backend exige que la sesión sea una letra de la A a la E (PlanEjercicioItemDTO).
function armarItems(semanas, primerId) {
  // [ejercicioId, series, reps, pausa, carga %, rpe]
  const sesiones = {
    A: [
      [1, 4, 8, 120, 70, 7],
      [2, 4, 8, 90, 70, 7],
      [3, 3, 10, 90, 65, 7],
    ],
    B: [
      [4, 3, 5, 180, 75, 8],
      [5, 3, 8, 90, 65, 7],
      [6, 3, 6, 90, null, 8],
    ],
  }
  const items = []
  let id = primerId
  for (const semana of semanas) {
    for (const [sesion, ejercicios] of Object.entries(sesiones)) {
      ejercicios.forEach(([ejercicioId, series, reps, pausa, carga, rpe], indice) => {
        items.push({
          id: id++,
          ejercicioId,
          nombreEjercicio: EJERCICIOS.find((e) => e.id === ejercicioId).nombre,
          sesion,
          numeroSemana: semana,
          orden: indice + 1,
          series,
          reps,
          pausaSegundos: pausa,
          // La carga sube de a poco cada semana.
          cargaPct: carga === null ? null : carga + (semana - 1) * 2.5,
          rpe,
        })
      })
    }
  }
  return items
}

const PROFESOR_DEMO = { profesorId: 1, nombreProfesor: 'Profesor Demo' }

const RUTINAS_BASE = [
  {
    id: 1,
    ...PROFESOR_DEMO,
    alumnoId: 1,
    nombreAlumno: 'Alumno Demo',
    nombre: 'Fuerza e hipertrofia',
    fechaInicio: '2026-10-05',
    duracionSemanas: 4,
    items: armarItems([1, 2, 3, 4], 1),
  },
  {
    id: 2,
    ...PROFESOR_DEMO,
    alumnoId: 3,
    nombreAlumno: 'Mateo Gómez',
    nombre: 'Adaptación al gimnasio',
    fechaInicio: '2026-10-19',
    duracionSemanas: 2,
    // Los IDs arrancan en 1000 para no pisar los de la rutina 1 (el progreso se guarda por ID).
    items: armarItems([1, 2], 1000),
  },
  {
    // Plantilla: no tiene alumno. Sirve de modelo para asignar.
    id: 3,
    ...PROFESOR_DEMO,
    alumnoId: null,
    nombreAlumno: null,
    nombre: 'Full body principiante',
    fechaInicio: null,
    duracionSemanas: 2,
    items: armarItems([1, 2], 2000),
  },
]

function leerJSON(clave, porDefecto) {
  try {
    return JSON.parse(localStorage.getItem(clave)) ?? porDefecto
  } catch {
    return porDefecto
  }
}

const leerProgreso = () => leerJSON(CLAVE_PROGRESO, [])
const leerExtras = () => leerJSON(CLAVE_RUTINAS, [])
const todasLasRutinas = () => [...RUTINAS_BASE, ...leerExtras()]

// Próximo ID libre para ejercicios del plan (nunca repite uno ya usado).
function proximoIdItem(rutinas) {
  const ids = rutinas.flatMap((r) => r.items.map((i) => i.id))
  return Math.max(5000, ...ids) + 1
}

export async function listarRutinasPorAlumno(alumnoId) {
  await demora()
  return todasLasRutinas().filter((rutina) => rutina.alumnoId === alumnoId)
}

export async function listarRutinasPorProfesor(profesorId) {
  await demora()
  return todasLasRutinas().filter((rutina) => rutina.profesorId === profesorId)
}

// Equivale a POST /rutinas/plantillas. Mismas validaciones que CrearRutinaRequestDTO y PlanEjercicioItemDTO.
export async function crearPlantilla({ profesorId, nombre, duracionSemanas, items }) {
  await demora(500)
  if (!nombre || !nombre.trim()) throw new Error('El nombre de la rutina es obligatorio')
  if (!Number.isInteger(duracionSemanas) || duracionSemanas < 1) {
    throw new Error('La duración mínima es de 1 semana')
  }
  for (const item of items ?? []) {
    if (!/^[A-E]$/.test(item.sesion ?? '')) throw new Error('La sesión debe ser una letra entre A y E')
    if (!item.numeroSemana || item.numeroSemana < 1) throw new Error('El número de semana debe ser al menos 1')
    if (!item.series || item.series < 1) throw new Error('Debe haber al menos 1 serie')
    if (!item.reps || item.reps < 1) throw new Error('Debe haber al menos 1 repetición')
    if (item.cargaPct != null && (item.cargaPct < 0 || item.cargaPct > 100)) {
      throw new Error('El porcentaje de carga debe estar entre 0 y 100')
    }
    if (item.rpe != null && (item.rpe < 1 || item.rpe > 10)) throw new Error('El RPE debe estar entre 1 y 10')
    if (!EJERCICIOS.some((e) => e.id === item.ejercicioId)) {
      throw new Error(`Ejercicio no encontrado con ID: ${item.ejercicioId}`)
    }
  }

  const todas = todasLasRutinas()
  let idItem = proximoIdItem(todas)
  const rutina = {
    id: Math.max(...todas.map((r) => r.id)) + 1,
    profesorId,
    nombreProfesor: 'Profesor Demo',
    alumnoId: null,
    nombreAlumno: null,
    nombre: nombre.trim(),
    fechaInicio: null,
    duracionSemanas,
    items: (items ?? []).map((item) => ({
      id: idItem++,
      ejercicioId: item.ejercicioId,
      nombreEjercicio: EJERCICIOS.find((e) => e.id === item.ejercicioId).nombre,
      sesion: item.sesion,
      numeroSemana: item.numeroSemana,
      orden: item.orden ?? 1,
      series: item.series,
      reps: item.reps,
      pausaSegundos: item.pausaSegundos ?? null,
      cargaPct: item.cargaPct ?? null,
      rpe: item.rpe ?? null,
    })),
  }
  localStorage.setItem(CLAVE_RUTINAS, JSON.stringify([...leerExtras(), rutina]))
  return rutina
}

// Equivale a POST /rutinas/{id}/asignar: el backend CLONA la rutina para el alumno.
export async function asignarRutina(rutinaId, { alumnoId, fechaInicio }) {
  await demora(500)
  if (!alumnoId) throw new Error('El alumnoId es obligatorio')
  const todas = todasLasRutinas()
  const original = todas.find((r) => r.id === rutinaId)
  if (!original) throw new Error(`Rutina no encontrada con ID: ${rutinaId}`)

  const alumnos = await listarAlumnosAsignados(original.profesorId)
  const alumno = alumnos.find((a) => a.id === alumnoId)
  if (!alumno) throw new Error(`Alumno no encontrado con ID: ${alumnoId}`)

  let idItem = proximoIdItem(todas)
  const clon = {
    ...original,
    id: Math.max(...todas.map((r) => r.id)) + 1,
    alumnoId,
    nombreAlumno: `${alumno.nombre} ${alumno.apellido}`,
    fechaInicio: fechaInicio || hoyISO(),
    items: original.items.map((item) => ({ ...item, id: idItem++ })),
  }
  localStorage.setItem(CLAVE_RUTINAS, JSON.stringify([...leerExtras(), clon]))
  return clon
}

export async function listarProgresoPorPlanEjercicio(planEjercicioId) {
  await demora(200)
  return leerProgreso()
    .filter((p) => p.planEjercicioId === planEjercicioId)
    .sort((a, b) => a.fecha.localeCompare(b.fecha) || a.numeroSerie - b.numeroSerie)
}

export async function registrarProgresoSet({
  planEjercicioId,
  numeroSerie,
  pesoRealizado,
  repeticionesRealizadas,
  fecha,
}) {
  await demora(250)
  // Mismas validaciones que RegistrarProgresoSetRequestDTO del backend.
  if (!Number.isInteger(numeroSerie) || numeroSerie < 1) {
    throw new Error('El número de serie debe ser mayor o igual a 1')
  }
  if (pesoRealizado === null || Number.isNaN(pesoRealizado) || pesoRealizado < 0) {
    throw new Error('El peso realizado es obligatorio y no puede ser negativo')
  }
  if (repeticionesRealizadas !== null && repeticionesRealizadas < 0) {
    throw new Error('Las repeticiones realizadas no pueden ser negativas')
  }

  const todos = leerProgreso()
  const nuevo = {
    id: todos.length ? Math.max(...todos.map((p) => p.id)) + 1 : 1,
    planEjercicioId,
    numeroSerie,
    pesoRealizado,
    repeticionesRealizadas,
    fecha: fecha ?? hoyISO(),
  }
  localStorage.setItem(CLAVE_PROGRESO, JSON.stringify([...todos, nuevo]))
  return nuevo
}