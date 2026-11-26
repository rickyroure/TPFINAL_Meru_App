import { useContext } from 'react'
import { AuthContext } from '../context/auth-context'

// Atajo para que cualquier componente pueda hacer: const { usuario, login, logout } = useAuth()
export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return contexto
}