import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { RUTA_POR_ROL } from '../constants/roles'

// Guardia de rutas: deja pasar solo a quien tenga el rol indicado.
export default function ProtectedRoute({ rol }) {
  const { usuario } = useAuth()

  // Sin sesión -> al login.
  if (!usuario) {
    return <Navigate to="/login" replace />
  }
  // Con sesión pero de otro rol -> a su propio panel.
  if (usuario.rol !== rol) {
    return <Navigate to={RUTA_POR_ROL[usuario.rol]} replace />
  }
  return <Outlet />
}