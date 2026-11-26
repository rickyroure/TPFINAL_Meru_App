// Datos de prueba que imitan GET/POST /alumnos/{id}/1rm (Alumno1RMResponseDTO).
// Se guardan en el navegador para que no se pierdan al recargar.

import { EJERCICIOS } from './ejercicios'

const CLAVE = 'meru_mock_1rm'
const demora = (ms = 300) => new Promise((resolve) => setTimeout(resolve, ms))

const INICIALES = [
  { id: 1, alumnoId: 1, ejercicioId: 1, nombreEjercicio: 'Sentadilla', valor1rm: 100, fechaMedicion: '2026-09-15' },
  { id: 2, alumnoId: 1, ejercicioId: 2, nombreEjercicio: 'Press de banca', valor1rm: 80, fechaMedicion: '2026-09-15' },
]

function leer() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE))
    if (guardado) return guardado
  } catch {
    // Si el dato guardado está dañado se empieza de cero.
  }
  localStorage.setItem(CLAVE, JSON.stringify(INICIALES))
  return INICIALES
}

// Más reciente primero, igual que findByAlumnoIdOrderByFechaMedicionDesc del backend.
export async function listarHistorico1RM(alumnoId) {
  await demora()
  return leer()
    .filter((r) => r.alumnoId === alumnoId)
    .sort((a, b) => b.fechaMedicion.localeCompare(a.fechaMedicion) || b.id - a.id)
}

export async function registrar1RM(alumnoId, { ejercicioId, valor1rm, fechaMedicion }) {
  await demora(400)
  // Mismas validaciones que Registrar1RMRequestDTO.
  if (!ejercicioId) throw new Error('El ID del ejercicio es obligatorio')
  if (valor1rm === null || Number.isNaN(valor1rm)) throw new Error('El valor del 1RM es obligatorio')
  if (valor1rm < 0.01) throw new Error('El valor del 1RM debe ser mayor a 0')
  const ejercicio = EJERCICIOS.find((e) => e.id === ejercicioId)
  if (!ejercicio) throw new Error(`Ejercicio no encontrado con ID: ${ejercicioId}`)

  const todos = leer()
  const nuevo = {
    id: Math.max(0, ...todos.map((r) => r.id)) + 1,
    alumnoId,
    ejercicioId,
    nombreEjercicio: ejercicio.nombre,
    valor1rm,
    fechaMedicion: fechaMedicion || new Date().toISOString().slice(0, 10),
  }
  localStorage.setItem(CLAVE, JSON.stringify([...todos, nuevo]))
  return nuevo
}