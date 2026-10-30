import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/clases'

// Operaciones sobre clases y reservas. Cada función usa datos de prueba o el
// backend real según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const clasesService = {
  // GET /clases
  async listar() {
    if (USE_MOCK) return mock.listarClases()
    const { data } = await api.get('/clases')
    return data
  },

  // GET /clases (sin filtros: incluye las que ya pasaron). Para el admin.
  async listarTodas() {
    if (USE_MOCK) return mock.listarTodasLasClases()
    const { data } = await api.get('/clases')
    return data
  },

  // POST /clases
  async crear(datos) {
    if (USE_MOCK) return mock.crearClase(datos)
    const { data } = await api.post('/clases', datos)
    return data
  },

  // GET /reservas/alumno/{alumnoId}
  async listarReservasDelAlumno(alumnoId) {
    if (USE_MOCK) return mock.listarReservasPorAlumno(alumnoId)
    const { data } = await api.get(`/reservas/alumno/${alumnoId}`)
    return data
  },

  // POST /reservas
  async reservar(claseId, alumnoId) {
    if (USE_MOCK) return mock.reservar({ claseId, alumnoId })
    const { data } = await api.post('/reservas', { claseId, alumnoId })
    return data
  },

  // PATCH /reservas/{id}/cancelar
  async cancelarReserva(reservaId) {
    if (USE_MOCK) return mock.cancelarReserva(reservaId)
    const { data } = await api.patch(`/reservas/${reservaId}/cancelar`)
    return data
  },
}