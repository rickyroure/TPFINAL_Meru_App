// Datos de prueba que imitan GET /profesores, POST /profesores y GET /profesores/{id}/alumnos.

const CLAVE_PROFESORES = 'meru_mock_profesores'
const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

const PROFESORES_INICIALES = [
  { id: 1, nombre: 'Profesor', apellido: 'Demo', email: 'profesor@meru.com', especialidad: 'Musculación' },
  { id: 2, nombre: 'Carolina', apellido: 'Paz', email: 'carolina@meru.com', especialidad: 'Funcional y Crossfit' },
]

const ALUMNOS = [
  {
    id: 1,
    nombre: 'Alumno',
    apellido: 'Demo',
    email: 'alumno@meru.com',
    aptoFisico: true,
    fechaIngreso: '2026-09-01',
    profesor: PROFESORES_INICIALES[0],
  },
  {
    id: 2,
    nombre: 'Lucía',
    apellido: 'Fernández',
    email: 'lucia@meru.com',
    aptoFisico: false,
    fechaIngreso: '2026-10-12',
    profesor: PROFESORES_INICIALES[0],
  },
  {
    id: 3,
    nombre: 'Mateo',
    apellido: 'Gómez',
    email: 'mateo@meru.com',
    aptoFisico: true,
    fechaIngreso: '2026-10-19',
    profesor: PROFESORES_INICIALES[0],
  },
]

function leerProfesores() {
  try {
    const guardados = JSON.parse(localStorage.getItem(CLAVE_PROFESORES))
    if (guardados) return guardados
  } catch {
    // Si está dañado se empieza de cero.
  }
  localStorage.setItem(CLAVE_PROFESORES, JSON.stringify(PROFESORES_INICIALES))
  return PROFESORES_INICIALES
}

export async function listarProfesores() {
  await demora()
  return leerProfesores()
}

export async function registrarProfesor({ usuarioId, especialidad }) {
  await demora(400)
  if (!usuarioId) throw new Error('El ID del usuario es obligatorio')
  if (!especialidad?.trim()) throw new Error('La especialidad es obligatoria')
  const lista = leerProfesores()
  const nuevo = {
    id: Math.max(0, ...lista.map((p) => p.id)) + 1,
    nombre: `Profesor #${usuarioId}`,
    apellido: 'Meru',
    email: `profesor.${usuarioId}@meru.app`,
    especialidad: especialidad.trim(),
  }
  localStorage.setItem(CLAVE_PROFESORES, JSON.stringify([...lista, nuevo]))
  return nuevo
}

export async function listarAlumnosAsignados(profesorId) {
  await demora()
  return ALUMNOS.filter((a) => a.profesor.id === profesorId)
}
