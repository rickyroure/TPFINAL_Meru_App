import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/profesores'

// Operaciones del profesor. Usa datos de prueba o el backend real según VITE_USE_MOCK.
export const profesoresService = {
  // GET /profesores
  async listar() {
    if (USE_MOCK) return mock.listarProfesores()
    try {
      const { data } = await api.get('/profesores')
      return data
    } catch {
      // Fallback por si el backend aún no fue reiniciado tras agregar GET /profesores:
      // consulta los IDs de profesores conocidos en la base local.
      const promesas = [1, 9, 13].map((id) => api.get(`/profesores/${id}`).then((r) => r.data))
      const resultados = await Promise.allSettled(promesas)
      return resultados.filter((r) => r.status === 'fulfilled').map((r) => r.value)
    }
  },

  // POST /profesores (P3)
  async registrar(datos) {
    if (USE_MOCK) return mock.registrarProfesor(datos)
    const { data } = await api.post('/profesores', datos)
    return data
  },

  // GET /profesores/{profesorId}/alumnos (P1)
  async listarAlumnos(profesorId) {
    if (USE_MOCK) return mock.listarAlumnosAsignados(profesorId)
    const { data } = await api.get(`/profesores/${profesorId}/alumnos`)
    return data
  },
}
