// Datos de prueba que imitan las respuestas del backend:
//  - ClaseResponseDTO (GET /clases, POST /clases)
//  - ReservaResponseDTO (GET /reservas/alumno/{id}, POST /reservas, PATCH /reservas/{id}/cancelar)
// Las reservas se guardan en el navegador para que no se pierdan al recargar.

const CLAVE = 'meru_mock_clases'
const CLAVE_CREADAS = 'meru_mock_clases_creadas'
const DOCE_HORAS = 12 * 60 * 60 * 1000

const demora = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms))

// Las clases se definen "a partir de ahora" para que siempre haya clases futuras.
// "ocupados" son lugares tomados por otros alumnos.
const MINUTO = 60 * 1000
const DEFINICIONES = [
  { id: 1, tipoActividad: 'Yoga', desdeBaseMin: 20, cupoMaximo: 10, ocupados: 7 },
  { id: 2, tipoActividad: 'Funcional', desdeBaseMin: 180, cupoMaximo: 12, ocupados: 4 },
  { id: 3, tipoActividad: 'Spinning', desdeBaseMin: 24 * 60, cupoMaximo: 8, ocupados: 8 },
  { id: 4, tipoActividad: 'Musculación guiada', desdeBaseMin: 48 * 60, cupoMaximo: 15, ocupados: 5 },
  { id: 5, tipoActividad: 'Pilates', desdeBaseMin: 72 * 60, cupoMaximo: 10, ocupados: 2 },
]

// Formato que usa el backend para LocalDateTime: 2026-10-29T18:00:00 (sin zona horaria).
function aLocalISO(fecha) {
  const p = (n) => String(n).padStart(2, '0')
  return (
    `${fecha.getFullYear()}-${p(fecha.getMonth() + 1)}-${p(fecha.getDate())}` +
    `T${p(fecha.getHours())}:${p(fecha.getMinutes())}:${p(fecha.getSeconds())}`
  )
}

// Estado guardado: { base: milisegundos, reservas: [...] }.
// Pasadas 12 horas se reinicia para que las clases de prueba no queden vencidas.
function leerEstado() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE))
    if (guardado && Date.now() - guardado.base < DOCE_HORAS) return guardado
  } catch {
    // Si el dato guardado está dañado se empieza de cero.
  }
  return { base: Date.now(), reservas: [] }
}

function guardarEstado(estado) {
  localStorage.setItem(CLAVE, JSON.stringify(estado))
}

// Clases dadas de alta por el admin. Se guardan aparte para que no se reinicien cada 12 horas.
function leerCreadas() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_CREADAS)) ?? []
  } catch {
    return []
  }
}

function armarClases(estado) {
  const creadas = leerCreadas().map((c) => ({
    ...c,
    cupoDisponible:
      c.cupoMaximo - estado.reservas.filter((r) => r.claseId === c.id && r.estado === 'CONFIRMADA').length,
  }))
  const deEjemplo = DEFINICIONES.map((d) => {
    const confirmadas = estado.reservas.filter(
      (r) => r.claseId === d.id && r.estado === 'CONFIRMADA',
    ).length
    return {
      id: d.id,
      profesorId: 1,
      nombreProfesor: 'Profesor Demo',
      tipoActividad: d.tipoActividad,
      horario: aLocalISO(new Date(estado.base + d.desdeBaseMin * MINUTO)),
      cupoMaximo: d.cupoMaximo,
      cupoDisponible: d.cupoMaximo - d.ocupados - confirmadas,
    }
  })
  return [...deEjemplo, ...creadas]
}

export async function listarClases() {
  await demora()
  const estado = leerEstado()
  guardarEstado(estado)
  const ahora = Date.now()
  return armarClases(estado).filter((c) => new Date(c.horario).getTime() > ahora)
}

// Equivale a GET /clases sin filtros: devuelve todas, también las que ya pasaron.
export async function listarTodasLasClases() {
  await demora()
  const estado = leerEstado()
  guardarEstado(estado)
  return armarClases(estado).sort((a, b) => a.horario.localeCompare(b.horario))
}

// Equivale a POST /clases. Mismas validaciones que CrearClaseRequestDTO.
export async function crearClase({ profesorId, tipoActividad, horario, cupoMaximo }) {
  await demora(450)
  if (!profesorId) throw new Error('El profesorId es obligatorio')
  if (!tipoActividad || !tipoActividad.trim()) throw new Error('El tipo de actividad es obligatorio')
  if (!horario) throw new Error('El horario es obligatorio')
  if (new Date(horario).getTime() <= Date.now()) throw new Error('El horario debe ser en el futuro')
  if (!Number.isInteger(cupoMaximo) || cupoMaximo < 1) throw new Error('El cupo máximo debe ser al menos 1')
  // En la base de prueba solo existe el profesor 1.
  if (profesorId !== 1) throw new Error(`Profesor no encontrado con ID: ${profesorId}`)

  const todas = armarClases(leerEstado())
  const nueva = {
    id: Math.max(100, ...todas.map((c) => c.id)) + 1,
    profesorId,
    nombreProfesor: 'Profesor Demo',
    tipoActividad: tipoActividad.trim(),
    horario: horario.length === 16 ? `${horario}:00` : horario,
    cupoMaximo,
  }
  localStorage.setItem(CLAVE_CREADAS, JSON.stringify([...leerCreadas(), nueva]))
  return { ...nueva, cupoDisponible: cupoMaximo }
}

export async function listarReservasPorAlumno(alumnoId) {
  await demora(250)
  return leerEstado().reservas.filter((r) => r.alumnoId === alumnoId)
}

export async function reservar({ claseId, alumnoId }) {
  await demora()
  const estado = leerEstado()
  const clase = armarClases(estado).find((c) => c.id === claseId)
  if (!clase) throw new Error(`Clase no encontrada con ID: ${claseId}`)
  // Mismas reglas que ReservaService.reservar del backend.
  if (clase.cupoDisponible <= 0) {
    throw new Error(`No hay cupo disponible para la clase ${claseId}`)
  }
  if (
    estado.reservas.some(
      (r) => r.claseId === claseId && r.alumnoId === alumnoId && r.estado === 'CONFIRMADA',
    )
  ) {
    throw new Error('El alumno ya posee una reserva confirmada para esta clase')
  }

  const nueva = {
    id: estado.reservas.length ? Math.max(...estado.reservas.map((r) => r.id)) + 1 : 1,
    claseId,
    tipoActividadClase: clase.tipoActividad,
    horarioClase: clase.horario,
    alumnoId,
    nombreAlumno: 'Alumno Demo',
    estado: 'CONFIRMADA',
    fechaReserva: aLocalISO(new Date()),
  }
  guardarEstado({ ...estado, reservas: [...estado.reservas, nueva] })
  return nueva
}

export async function cancelarReserva(reservaId) {
  await demora()
  const estado = leerEstado()
  const reserva = estado.reservas.find((r) => r.id === reservaId)
  if (!reserva) throw new Error(`Reserva no encontrada con ID: ${reservaId}`)
  if (reserva.estado === 'CANCELADA') return reserva

  // Misma regla que el backend: no se cancela con menos de 30 minutos de antelación.
  const minutosHastaClase = (new Date(reserva.horarioClase).getTime() - Date.now()) / MINUTO
  if (minutosHastaClase < 30) {
    throw new Error(
      'No se puede cancelar una reserva con menos de 30 minutos de antelación al inicio de la clase',
    )
  }

  const cancelada = { ...reserva, estado: 'CANCELADA' }
  guardarEstado({
    ...estado,
    reservas: estado.reservas.map((r) => (r.id === reservaId ? cancelada : r)),
  })
  return cancelada
}