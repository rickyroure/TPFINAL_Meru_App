import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './hooks/useAuth'
import { ROLES, RUTA_POR_ROL } from './constants/roles'
import ProtectedRoute from './routes/ProtectedRoute'
import DashboardLayout from './layouts/DashboardLayout'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import InicioAlumno from './pages/alumno/InicioAlumno'
import InicioProfesor from './pages/profesor/InicioProfesor'
import InicioAdmin from './pages/admin/InicioAdmin'

// La ruta "/" manda al login o al panel que corresponda según la sesión.
function RedirigirSegunSesion() {
  const { usuario } = useAuth()
  return <Navigate to={usuario ? RUTA_POR_ROL[usuario.rol] : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RedirigirSegunSesion />} />
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute rol={ROLES.ALUMNO} />}>
        <Route element={<DashboardLayout />}>
          <Route path="/alumno" element={<InicioAlumno />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute rol={ROLES.PROFESOR} />}>
        <Route element={<DashboardLayout />}>
          <Route path="/profesor" element={<InicioProfesor />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute rol={ROLES.ADMIN} />}>
        <Route element={<DashboardLayout />}>
          <Route path="/admin" element={<InicioAdmin />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}