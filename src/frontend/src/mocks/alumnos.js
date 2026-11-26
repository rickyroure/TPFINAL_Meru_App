// Datos de prueba que imitan las respuestas del backend (AlumnoResponseDTO).
// Los cambios se guardan en el navegador para que no se pierdan al recargar.

const CLAVE = 'meru_mock_alumno'
const CLAVE_LISTA = 'meru_mock_alumnos_lista'

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

const LISTA_POR_DEFECTO = [
  ALUMNO_POR_DEFECTO,
  {
    id: 2,
    nombre: 'Lucía',
    apellido: 'Fernández',
    email: 'lucia@meru.com',
    telefono: '3515550101',
    fechaNacimiento: '1999-04-12',
    aptoFisico: false,
    fechaIngreso: '2026-10-12',
    profesor: { id: 1, nombre: 'Profesor', apellido: 'Demo', especialidad: 'Musculación' },
  },
  {
    id: 3,
    nombre: 'Mateo',
    apellido: 'Gómez',
    email: 'mateo@meru.com',
    telefono: '3515550202',
    fechaNacimiento: '1996-11-23',
    aptoFisico: true,
    fechaIngreso: '2026-10-19',
    profesor: null,
  },
]

const demora = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

function leer() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE)) ?? ALUMNO_POR_DEFECTO
  } catch {
    return ALUMNO_POR_DEFECTO
  }
}

function leerLista() {
  try {
    const guardada = JSON.parse(localStorage.getItem(CLAVE_LISTA))
    if (guardada) return guardada
  } catch {
    // Si está dañado se empieza de cero.
  }
  localStorage.setItem(CLAVE_LISTA, JSON.stringify(LISTA_POR_DEFECTO))
  return LISTA_POR_DEFECTO
}

export async function listarAlumnos() {
  await demora()
  return leerLista()
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

export async function actualizarAptoFisico(alumnoId, aptoFisico) {
  await demora(300)
  const lista = leerLista()
  const objetivo = lista.find((a) => a.id === alumnoId)
  if (!objetivo) throw new Error(`Alumno no encontrado con ID: ${alumnoId}`)
  const actualizado = { ...objetivo, aptoFisico: Boolean(aptoFisico) }
  localStorage.setItem(
    CLAVE_LISTA,
    JSON.stringify(lista.map((a) => (a.id === alumnoId ? actualizado : a))),
  )
  return actualizado
}

export async function asignarProfesor(alumnoId, profesor) {
  await demora(300)
  const lista = leerLista()
  const objetivo = lista.find((a) => a.id === alumnoId)
  if (!objetivo) throw new Error(`Alumno no encontrado con ID: ${alumnoId}`)
  const actualizado = { ...objetivo, profesor }
  localStorage.setItem(
    CLAVE_LISTA,
    JSON.stringify(lista.map((a) => (a.id === alumnoId ? actualizado : a))),
  )
  return actualizado
}
