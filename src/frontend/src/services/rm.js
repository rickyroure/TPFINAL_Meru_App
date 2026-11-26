import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/rm'

// Máximos (1RM) de un alumno. Usa datos de prueba o el backend real según VITE_USE_MOCK.
export const rmService = {
  // GET /alumnos/{alumnoId}/1rm
  async listarHistorico(alumnoId) {
    if (USE_MOCK) return mock.listarHistorico1RM(alumnoId)
    const { data } = await api.get(`/alumnos/${alumnoId}/1rm`)
    return data
  },

  // POST /alumnos/{alumnoId}/1rm
  async registrar(alumnoId, datos) {
    if (USE_MOCK) return mock.registrar1RM(alumnoId, datos)
    const { data } = await api.post(`/alumnos/${alumnoId}/1rm`, datos)
    return data
  },
}