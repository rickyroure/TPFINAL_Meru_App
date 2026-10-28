// Datos de prueba que imitan las respuestas del backend (AlumnoResponseDTO).
// Los cambios se guardan en el navegador para que no se pierdan al recargar.

const CLAVE = 'meru_mock_alumno'

const ALUMNO_POR_DEFECTO = {
  id: 1,
  nombre: 'Alumno',
  apellido: 'Demo',
  email: 'alumno@meru.com',
  telefono: '',
  fechaNacimiento: '',
  aptoFisico: true,
  fechaIngreso: '2026-09-15',
  profesor: { id: 1, nombre: 'Profesor', apellido: 'Demo', especialidad: 'Musculación' },
}

const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

function leer() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE)) ?? ALUMNO_POR_DEFECTO
  } catch {
    return ALUMNO_POR_DEFECTO
  }
}

export async function obtenerAlumno() {
  await demora()
  return leer()
}

export async function actualizarPerfil(alumnoId, { nombre, apellido, telefono, fechaNacimiento }) {
  await demora()
  // Mismas validaciones que el backend (@NotBlank en nombre y apellido).
  if (!nombre?.trim()) throw new Error('El nombre es obligatorio')
  if (!apellido?.trim()) throw new Error('El apellido es obligatorio')

  const actualizado = {
    ...leer(),
    nombre: nombre.trim(),
    apellido: apellido.trim(),
    telefono: telefono ?? '',
    fechaNacimiento: fechaNacimiento ?? '',
  }
  localStorage.setItem(CLAVE, JSON.stringify(actualizado))
  return actualizado
}