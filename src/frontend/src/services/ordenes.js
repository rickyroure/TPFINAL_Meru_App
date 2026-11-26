import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/ordenes'

// Compras del shop. Cada función usa datos de prueba o el backend real
// según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const ordenesService = {
  // Comprar son dos pasos en el backend:
  //   1) POST /ordenes -> crea la orden (queda PENDIENTE, todavía no descuenta stock)
  //   2) POST /pagos   -> paga con Mercado Pago y devuelve la URL del checkout
  // "items" es una lista { productoId, cantidad }.
  // Devuelve el pago. Si trae checkoutUrl hay que mandar al usuario a esa dirección.
  async comprar(alumnoId, items, total) {
    if (USE_MOCK) {
      const orden = await mock.crearOrden({ alumnoId, items })
      return mock.pagarOrden({ ordenId: orden.id, monto: orden.total })
    }
    const { data: orden } = await api.post('/ordenes', { alumnoId, items })
    const { data: pago } = await api.post('/pagos', {
      monto: orden.total ?? total,
      metodo: 'MERCADOPAGO',
      ordenId: orden.id,
    })
    return pago
  },

  // GET /ordenes/alumno/{alumnoId}
  async listarDelAlumno(alumnoId) {
    if (USE_MOCK) return mock.listarOrdenesPorAlumno(alumnoId)
    const { data } = await api.get(`/ordenes/alumno/${alumnoId}`)
    const pendientes = data.filter((o) => o.estado === 'PENDIENTE')
    if (pendientes.length === 0) return data
    // Consultar los pagos de una orden hace que el backend sincronice con Mercado Pago
    // los que están pendientes. Después se vuelve a pedir la lista para ver el estado nuevo.
    await Promise.all(pendientes.map((o) => api.get(`/pagos/orden/${o.id}`).catch(() => {})))
    const { data: actualizadas } = await api.get(`/ordenes/alumno/${alumnoId}`)
    return actualizadas
  },
}