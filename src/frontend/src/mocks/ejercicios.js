// Catálogo de ejercicios de prueba. Imita GET /ejercicios (EjercicioResponseDTO).

const demora = (ms = 250) => new Promise((resolve) => setTimeout(resolve, ms))

export const EJERCICIOS = [
  { id: 1, nombre: 'Sentadilla', grupoMuscular: 'Piernas', descripcion: 'Sentadilla con barra' },
  { id: 2, nombre: 'Press de banca', grupoMuscular: 'Pecho', descripcion: 'Press plano con barra' },
  { id: 3, nombre: 'Remo con barra', grupoMuscular: 'Espalda', descripcion: 'Remo inclinado' },
  { id: 4, nombre: 'Peso muerto', grupoMuscular: 'Espalda', descripcion: 'Peso muerto convencional' },
  { id: 5, nombre: 'Press militar', grupoMuscular: 'Hombros', descripcion: 'Press de pie con barra' },
  { id: 6, nombre: 'Dominadas', grupoMuscular: 'Espalda', descripcion: 'Dominadas pronas' },
  { id: 7, nombre: 'Zancadas', grupoMuscular: 'Piernas', descripcion: 'Zancadas con mancuernas' },
  { id: 8, nombre: 'Curl de bíceps', grupoMuscular: 'Brazos', descripcion: 'Curl con barra Z' },
  { id: 9, nombre: 'Extensión de tríceps', grupoMuscular: 'Brazos', descripcion: 'Extensión en polea' },
  { id: 10, nombre: 'Plancha', grupoMuscular: 'Core', descripcion: 'Plancha abdominal' },
  { id: 11, nombre: 'Hip thrust', grupoMuscular: 'Glúteos', descripcion: 'Empuje de cadera con barra' },
  { id: 12, nombre: 'Prensa de piernas', grupoMuscular: 'Piernas', descripcion: 'Prensa 45 grados' },
]

export async function listarEjercicios() {
  await demora()
  return EJERCICIOS
}