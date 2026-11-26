import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/productos'

// Operaciones sobre los productos del shop. Cada función usa datos de prueba
// o el backend real según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const productosService = {
  // GET /productos
  async listar() {
    if (USE_MOCK) return mock.listarProductos()
    const { data } = await api.get('/productos')
    return data
  },

  // POST /productos (solo admin)
  async crear(datos) {
    if (USE_MOCK) return mock.crearProducto(datos)
    const { data } = await api.post('/productos', datos)
    return data
  },

  // PUT /productos/{id} (solo admin)
  async actualizar(id, datos) {
    if (USE_MOCK) return mock.actualizarProducto(id, datos)
    const { data } = await api.put(`/productos/${id}`, datos)
    return data
  },
}