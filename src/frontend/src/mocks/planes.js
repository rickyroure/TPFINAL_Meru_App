// Datos de prueba que imitan las respuestas del backend:
//  - PlanResponseDTO (GET /planes)
//  - SuscripcionResponseDTO (GET /suscripciones/alumno/{id}, POST /suscripciones)
//  - PagoResponseDTO (POST /pagos)
// Las suscripciones se guardan en el navegador para que no se pierdan al recargar.
// Los precios son inventados, solo para el prototipo.

const CLAVE = 'meru_mock_suscripciones'
const DIA = 24 * 60 * 60 * 1000

const demora = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms))

const PLANES = [
  { id: 1, nombre: 'Mensual', precio: 25000, duracionDias: 30 },
  { id: 2, nombre: 'Trimestral', precio: 65000, duracionDias: 90 },
  { id: 3, nombre: 'Anual', precio: 220000, duracionDias: 365 },
]

// Fecha en formato del backend (LocalDate): 2026-10-28
function aFechaISO(fecha) {
  const p = (n) => String(n).padStart(2, '0')
  return `${fecha.getFullYear()}-${p(fecha.getMonth() + 1)}-${p(fecha.getDate())}`
}

function medianocheDeHoy() {
  const ahora = new Date()
  return new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate())
}

// Misma regla que SuscripcionResponseDTO del backend: solo cuenta si está ACTIVA.
function calcularDiasRestantes(fechaFin, estado) {
  if (!fechaFin || estado !== 'ACTIVA') return 0
  const fin = new Date(`${fechaFin}T00:00:00`)
  return Math.max(0, Math.round((fin - medianocheDeHoy()) / DIA))
}

// La primera vez el alumno de prueba ya tiene un plan Mensual que vence en 10 días.
function suscripcionInicial() {
  const hoy = medianocheDeHoy()
  return {
    id: 1,
    alumnoId: 1,
    nombreAlumno: 'Alumno Demo',
    planId: 1,
    nombrePlan: 'Mensual',
    fechaInicio: aFechaISO(new Date(hoy.getTime() - 20 * DIA)),
    fechaFin: aFechaISO(new Date(hoy.getTime() + 10 * DIA)),
    estado: 'ACTIVA',
  }
}

function leer() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE))
    if (guardado) return guardado
  } catch {
    // Si el dato guardado está dañado se empieza de cero.
  }
  const inicial = [suscripcionInicial()]
  localStorage.setItem(CLAVE, JSON.stringify(inicial))
  return inicial
}

export async function listarPlanes() {
  await demora()
  return PLANES
}

export async function listarSuscripcionesPorAlumno(alumnoId) {
  await demora()
  return leer()
    .filter((s) => s.alumnoId === alumnoId)
    .map((s) => ({ ...s, diasRestantes: calcularDiasRestantes(s.fechaFin, s.estado) }))
}

// Equivale a POST /suscripciones seguido de POST /pagos (pago aprobado simulado).
export async function contratarPlan(plan, alumnoId) {
  await demora(600)
  const todas = leer()
  const hoy = medianocheDeHoy()
  const suscripcion = {
    id: Math.max(0, ...todas.map((s) => s.id)) + 1,
    alumnoId,
    nombreAlumno: 'Alumno Demo',
    planId: plan.id,
    nombrePlan: plan.nombre,
    fechaInicio: aFechaISO(hoy),
    fechaFin: aFechaISO(new Date(hoy.getTime() + plan.duracionDias * DIA)),
    estado: 'ACTIVA',
  }
  localStorage.setItem(CLAVE, JSON.stringify([...todas, suscripcion]))

  return {
    id: suscripcion.id,
    suscripcionId: suscripcion.id,
    ordenId: null,
    monto: plan.precio,
    metodo: 'MERCADOPAGO',
    estado: 'APROBADO',
    fecha: new Date().toISOString(),
    referenciaExterna: `MOCK-${suscripcion.id}`,
    mensaje: 'Pago aprobado (simulado)',
    checkoutUrl: null,
  }
}