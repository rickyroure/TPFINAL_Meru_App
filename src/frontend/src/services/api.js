import axios from 'axios'
import { API_URL } from '../config'
import { authService } from './auth'

// Cliente HTTP único para hablar con el backend.
export const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Antes de cada petición agrega el token del usuario (si hay uno),
// que es lo que el backend valida con Keycloak.
api.interceptors.request.use((config) => {
  const token = authService.obtenerToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})