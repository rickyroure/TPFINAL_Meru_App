// Datos de prueba que imitan lo que hace el backend en el Shop:
//  - OrdenResponseDTO (POST /ordenes, GET /ordenes/alumno/{id})
//  - PagoResponseDTO (POST /pagos con ordenId)
// Igual que el backend: la orden se crea PENDIENTE y NO descuenta stock;
// el stock se descuenta recién cuando el pago se aprueba.

import { guardarProductos, leerProductos } from './productos'

const CLAVE = 'meru_mock_ordenes'

const demora = (ms = 350) => new Promise((resolve) => setTimeout(resolve, ms))

function leer() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE))
    if (guardado) return guardado
  } catch {
    // Si el dato guardado está dañado se empieza de cero.
  }
  return []
}

function guardar(ordenes) {
  localStorage.setItem(CLAVE, JSON.stringify(ordenes))
}

// Fecha como la devuelve el backend (LocalDateTime): 2026-11-12T01:07:00
function ahoraLocalISO() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// POST /ordenes
export async function crearOrden({ alumnoId, items }) {
  await demora(450)
  if (!items || items.length === 0) throw new Error('La orden debe contener al menos un producto')

  const productos = leerProductos()
  const ordenes = leer()
  const idOrden = Math.max(0, ...ordenes.map((o) => o.id)) + 1
  let idItem = Math.max(0, ...ordenes.flatMap((o) => o.items.map((i) => i.id))) + 1

  const itemsOrden = items.map((pedido) => {
    const producto = productos.find((p) => p.id === pedido.productoId)
    if (!producto) throw new Error(`Producto no encontrado con ID: ${pedido.productoId}`)
    if (!Number.isInteger(pedido.cantidad) || pedido.cantidad < 1) {
      throw new Error('La cantidad debe ser mayor a 0')
    }
    if (producto.stock < pedido.cantidad) {
      throw new Error(
        `Stock insuficiente para el producto '${producto.nombre}'. Disponible: ${producto.stock}, solicitado: ${pedido.cantidad}`,
      )
    }
    return {
      id: idItem++,
      productoId: producto.id,
      productoNombre: producto.nombre,
      cantidad: pedido.cantidad,
      precioUnitario: producto.precio,
      subtotal: producto.precio * pedido.cantidad,
    }
  })

  const orden = {
    id: idOrden,
    alumnoId,
    total: itemsOrden.reduce((suma, i) => suma + i.subtotal, 0),
    estado: 'PENDIENTE',
    fecha: ahoraLocalISO(),
    items: itemsOrden,
  }
  guardar([...ordenes, orden])
  return orden
}

// POST /pagos con ordenId (pago aprobado simulado).
// Al aprobarse se descuenta el stock y la orden pasa a PAGADA.
export async function pagarOrden({ ordenId, monto }) {
  await demora(600)
  const ordenes = leer()
  const orden = ordenes.find((o) => o.id === ordenId)
  if (!orden) throw new Error(`Orden no encontrada con ID: ${ordenId}`)
  if (orden.estado !== 'PENDIENTE') {
    throw new Error(`La orden con ID ${ordenId} no se encuentra en estado PENDIENTE`)
  }

  const productos = leerProductos()
  for (const item of orden.items) {
    const producto = productos.find((p) => p.id === item.productoId)
    if (!producto || producto.stock < item.cantidad) {
      throw new Error(
        `Stock insuficiente al confirmar el pago para el producto '${item.productoNombre}'. Disponible: ${producto?.stock ?? 0}, requerido: ${item.cantidad}`,
      )
    }
  }
  guardarProductos(
    productos.map((p) => {
      const item = orden.items.find((i) => i.productoId === p.id)
      return item ? { ...p, stock: p.stock - item.cantidad } : p
    }),
  )
  guardar(ordenes.map((o) => (o.id === ordenId ? { ...o, estado: 'PAGADA' } : o)))

  return {
    id: ordenId,
    suscripcionId: null,
    ordenId,
    monto,
    metodo: 'MERCADOPAGO',
    estado: 'APROBADO',
    fecha: new Date().toISOString(),
    referenciaExterna: `MOCK-ORD-${ordenId}`,
    mensaje: 'Pago aprobado (simulado)',
    checkoutUrl: null,
  }
}

// GET /ordenes/alumno/{id}
export async function listarOrdenesPorAlumno(alumnoId) {
  await demora()
  return leer().filter((o) => o.alumnoId === alumnoId)
}