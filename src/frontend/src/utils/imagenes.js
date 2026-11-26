// Elige qué ilustración corresponde a una clase o a un producto.
// El backend no guarda imagen ni categoría: la elección se hace por el nombre
// (sin tildes ni mayúsculas). Si no coincide nada se usa una ilustración genérica.

function normalizar(texto) {
  return (texto ?? '')
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
}

const ACTIVIDADES = [
  ['yoga', /yoga|meditaci/],
  ['spinning', /spinning|bici|cycl|ciclismo/],
  ['funcional', /funcional|crossfit|hiit|circuito|kettlebell/],
  ['musculacion', /muscul|pesas|fuerza|gym|sala/],
  ['pilates', /pilates|stretch|estiramiento|elongaci/],
  ['baile', /zumba|baile|danza|ritmo/],
  ['cardio', /cardio|aerobic|running|correr|aerobox/],
]

const PRODUCTOS = [
  ['barra', /barra|bar\b|snack/],
  ['preentreno', /pre[ -]?entren|pre[ -]?workout/],
  ['proteina', /proteina|whey|protein/],
  ['creatina', /creatina/],
  ['bcaa', /bcaa|aminoacid/],
  ['shaker', /shaker|botella|vaso mezclador/],
  ['vitaminas', /vitamin|multi/],
  ['banda', /banda|elastic|resistencia|liga/],
]

function buscar(lista, nombre) {
  const texto = normalizar(nombre)
  return lista.find(([, patron]) => patron.test(texto))?.[0] ?? 'generico'
}

export const claveActividad = (nombre) => buscar(ACTIVIDADES, nombre)
export const claveProducto = (nombre) => buscar(PRODUCTOS, nombre)
