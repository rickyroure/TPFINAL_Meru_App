// Datos de prueba que imitan ProductoResponseDTO del backend
// (GET /productos, POST /productos, PUT /productos/{id}).
// Se guardan en el navegador para que no se pierdan al recargar.
// Los precios son inventados, solo para el prototipo.

const CLAVE = 'meru_mock_productos'

const demora = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms))

const PRODUCTOS_INICIALES = [
  { id: 1, nombre: 'Proteína Whey 1 kg', precio: 38000, stock: 25 },
  { id: 2, nombre: 'Creatina monohidrato 300 g', precio: 22000, stock: 40 },
  { id: 3, nombre: 'BCAA 200 g', precio: 19000, stock: 4 },
  { id: 4, nombre: 'Pre-entreno 300 g', precio: 27000, stock: 12 },
  { id: 5, nombre: 'Shaker 600 ml', precio: 6500, stock: 60 },
  { id: 6, nombre: 'Barras de proteína x12', precio: 24000, stock: 0 },
  { id: 7, nombre: 'Multivitamínico x60', precio: 15000, stock: 30 },
  { id: 8, nombre: 'Banda elástica de resistencia', precio: 9000, stock: 15 },
]

// Se usan también desde mocks/ordenes.js para descontar el stock al pagar.
export function leerProductos() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE))
    if (guardado) return guardado
  } catch {
    // Si el dato guardado está dañado se empieza de cero.
  }
  localStorage.setItem(CLAVE, JSON.stringify(PRODUCTOS_INICIALES))
  return PRODUCTOS_INICIALES
}

export function guardarProductos(productos) {
  localStorage.setItem(CLAVE, JSON.stringify(productos))
}

// Mismas reglas que ProductoRequestDTO del backend.
function validarProducto({ nombre, precio, stock }) {
  if (!nombre || !nombre.trim()) throw new Error('El nombre del producto es obligatorio')
  if (nombre.trim().length > 150) throw new Error('El nombre no puede superar los 150 caracteres')
  if (precio === null || precio === undefined || Number.isNaN(precio)) throw new Error('El precio es obligatorio')
  if (precio < 0) throw new Error('El precio no puede ser negativo')
  if (stock === null || stock === undefined || Number.isNaN(stock)) throw new Error('El stock es obligatorio')
  if (!Number.isInteger(stock)) throw new Error('El stock debe ser un número entero')
  if (stock < 0) throw new Error('El stock no puede ser negativo')
}

// GET /productos
export async function listarProductos() {
  await demora()
  return leerProductos()
}

// POST /productos (solo admin)
export async function crearProducto(datos) {
  await demora(450)
  validarProducto(datos)
  const productos = leerProductos()
  const nuevo = {
    id: Math.max(0, ...productos.map((p) => p.id)) + 1,
    nombre: datos.nombre.trim(),
    precio: datos.precio,
    stock: datos.stock,
  }
  guardarProductos([...productos, nuevo])
  return nuevo
}

// PUT /productos/{id} (solo admin)
export async function actualizarProducto(id, datos) {
  await demora(450)
  validarProducto(datos)
  const productos = leerProductos()
  if (!productos.some((p) => p.id === id)) throw new Error(`Producto no encontrado con ID: ${id}`)
  const actualizado = { id, nombre: datos.nombre.trim(), precio: datos.precio, stock: datos.stock }
  guardarProductos(productos.map((p) => (p.id === id ? actualizado : p)))
  return actualizado
}