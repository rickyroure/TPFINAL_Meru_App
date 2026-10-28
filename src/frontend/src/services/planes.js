import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/planes'

// Operaciones sobre planes, suscripciones y pagos. Cada función usa datos de
// prueba o el backend real según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const planesService = {
  // GET /planes
  async listarPlanes() {
    if (USE_MOCK) return mock.listarPlanes()
    const { data } = await api.get('/planes')
    return data
  },

  // GET /suscripciones/alumno/{alumnoId}
  async listarSuscripcionesDelAlumno(alumnoId) {
    if (USE_MOCK) return mock.listarSuscripcionesPorAlumno(alumnoId)
    const { data } = await api.get(`/suscripciones/alumno/${alumnoId}`)
    return data
  },

  // Contratar un plan son dos pasos en el backend:
  //   1) POST /suscripciones  -> crea la suscripción
  //   2) POST /pagos          -> paga con Mercado Pago y devuelve la URL del checkout
  // Devuelve el pago. Si trae checkoutUrl, hay que mandar al usuario a esa dirección.
  async contratar(plan, alumnoId) {
    if (USE_MOCK) return mock.contratarPlan(plan, alumnoId)
    const { data: suscripcion } = await api.post('/suscripciones', {
      alumnoId,
      planId: plan.id,
    })
    const { data: pago } = await api.post('/pagos', {
      monto: plan.precio,
      metodo: 'MERCADOPAGO',
      suscripcionId: suscripcion.id,
    })
    return pago
  },
}