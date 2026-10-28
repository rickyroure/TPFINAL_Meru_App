import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/rutinas'

// Operaciones sobre rutinas y progreso. Cada función usa datos de prueba o el
// backend real según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const rutinasService = {
  // GET /rutinas/alumno/{alumnoId}
  async listarPorAlumno(alumnoId) {
    if (USE_MOCK) return mock.listarRutinasPorAlumno(alumnoId)
    const { data } = await api.get(`/rutinas/alumno/${alumnoId}`)
    return data
  },

  // GET /progreso-sets/plan-ejercicio/{planEjercicioId}
  async listarProgreso(planEjercicioId) {
    if (USE_MOCK) return mock.listarProgresoPorPlanEjercicio(planEjercicioId)
    const { data } = await api.get(`/progreso-sets/plan-ejercicio/${planEjercicioId}`)
    return data
  },

  // POST /progreso-sets
  async registrarProgreso(datos) {
    if (USE_MOCK) return mock.registrarProgresoSet(datos)
    const { data } = await api.post('/progreso-sets', datos)
    return data
  },
}