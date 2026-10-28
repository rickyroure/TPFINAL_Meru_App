// Datos de prueba que imitan las respuestas del backend:
//  - RutinaResponseDTO (GET /rutinas/alumno/{id})
//  - ProgresoSetResponseDTO (GET/POST /progreso-sets)
// El progreso registrado se guarda en el navegador para que no se pierda al recargar.

const CLAVE_PROGRESO = 'meru_mock_progreso'

const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

// Arma los ejercicios de una sesión. Los IDs son únicos para que cada uno tenga su propio progreso.
function armarItems() {
  const sesiones = {
    'Día A': [
      ['Sentadilla', 4, 8, 120, 70, 7],
      ['Press de banca', 4, 8, 90, 70, 7],
      ['Remo con barra', 3, 10, 90, 65, 7],
    ],
    'Día B': [
      ['Peso muerto', 3, 5, 180, 75, 8],
      ['Press militar', 3, 8, 90, 65, 7],
      ['Dominadas', 3, 6, 90, null, 8],
    ],
  }
  const items = []
  let id = 1
  let ejercicioId = 1
  for (const semana of [1, 2, 3, 4]) {
    for (const [sesion, ejercicios] of Object.entries(sesiones)) {
      ejercicios.forEach(([nombre, series, reps, pausa, carga, rpe], indice) => {
        items.push({
          id: id++,
          ejercicioId: ejercicioId + indice,
          nombreEjercicio: nombre,
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
    ejercicioId = 1
  }
  return items
}

const RUTINAS = [
  {
    id: 1,
    profesorId: 1,
    nombreProfesor: 'Profesor Demo',
    alumnoId: 1,
    nombreAlumno: 'Alumno Demo',
    nombre: 'Fuerza e hipertrofia',
    fechaInicio: '2026-10-05',
    duracionSemanas: 4,
    items: armarItems(),
  },
]

function leerProgreso() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_PROGRESO)) ?? []
  } catch {
    return []
  }
}

export async function listarRutinasPorAlumno(alumnoId) {
  await demora()
  return RUTINAS.filter((rutina) => rutina.alumnoId === alumnoId)
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
    fecha: fecha ?? new Date().toISOString().slice(0, 10),
  }
  localStorage.setItem(CLAVE_PROGRESO, JSON.stringify([...todos, nuevo]))
  return nuevo
}