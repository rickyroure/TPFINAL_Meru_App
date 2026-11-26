import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/ejercicios'

export const ejerciciosService = {
  // GET /ejercicios
  async listar() {
    if (USE_MOCK) return mock.listarEjercicios()
    const { data } = await api.get('/ejercicios')
    return data
  },
}