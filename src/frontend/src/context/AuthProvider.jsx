import { useState } from 'react'
import { AuthContext } from './auth-context'
import { authService } from '../services/auth'

export default function AuthProvider({ children }) {
  // Al abrir la app se recupera la sesión guardada (si hay una).
  const [usuario, setUsuario] = useState(() => authService.obtenerSesion())

  const login = async (email, password) => {
    const sesion = await authService.login(email, password)
    setUsuario(sesion)
    return sesion
  }

  const logout = () => {
    authService.logout()
    setUsuario(null)
  }

  return (
    <AuthContext.Provider value={{ usuario, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}