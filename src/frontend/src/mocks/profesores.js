// Datos de prueba que imitan GET /profesores/{id}/alumnos (lista de AlumnoResponseDTO).

const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

const PROFESOR = { id: 1, nombre: 'Profesor', apellido: 'Demo', especialidad: 'Musculación' }

const ALUMNOS = [
  {
    id: 1,
    nombre: 'Alumno',
    apellido: 'Demo',
    email: 'alumno@meru.com',
    aptoFisico: true,
    fechaIngreso: '2026-09-01',
    profesor: PROFESOR,
  },
  {
    id: 2,
    nombre: 'Lucía',
    apellido: 'Fernández',
    email: 'lucia@meru.com',
    aptoFisico: false,
    fechaIngreso: '2026-10-12',
    profesor: PROFESOR,
  },
  {
    id: 3,
    nombre: 'Mateo',
    apellido: 'Gómez',
    email: 'mateo@meru.com',
    aptoFisico: true,
    fechaIngreso: '2026-10-19',
    profesor: PROFESOR,
  },
]

export async function listarAlumnosAsignados(profesorId) {
  await demora()
  return ALUMNOS.filter((a) => a.profesor.id === profesorId)
}