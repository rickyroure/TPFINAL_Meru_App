import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/profesores'

// Operaciones del profesor. Usa datos de prueba o el backend real según VITE_USE_MOCK.
export const profesoresService = {
  // GET /profesores/{profesorId}/alumnos
  async listarAlumnos(profesorId) {
    if (USE_MOCK) return mock.listarAlumnosAsignados(profesorId)
    const { data } = await api.get(`/profesores/${profesorId}/alumnos`)
    return data
  },
}