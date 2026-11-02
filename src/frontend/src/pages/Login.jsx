import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { RUTA_POR_ROL } from '../constants/roles'

export default function Login() {
  const { usuario, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  // Si ya hay sesión iniciada, no tiene sentido mostrar el login.
  if (usuario) {
    return <Navigate to={RUTA_POR_ROL[usuario.rol]} replace />
  }

  const enviarFormulario = async (evento) => {
    evento.preventDefault()
    setError('')
    setCargando(true)
    try {
      const sesion = await login(email, password)
      navigate(RUTA_POR_ROL[sesion.rol], { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="w-full max-w-sm rounded-2xl border border-neutral-800 bg-neutral-900 p-8">
        <h1 className="text-3xl font-bold text-emerald-500">Meru</h1>
        <p className="mb-6 mt-1 text-sm text-neutral-400">Ingresá a tu cuenta</p>

        <form onSubmit={enviarFormulario} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm">
            Email
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 outline-none focus:border-emerald-500"
            />
          </label>

          <label className="flex flex-col gap-1 text-sm">
            Contraseña
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 outline-none focus:border-emerald-500"
            />
          </label>

          {error && <p className="text-sm text-red-400">{error}</p>}

          <button
            type="submit"
            disabled={cargando}
            className="rounded-lg bg-emerald-500 px-3 py-2 font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
          >
            {cargando ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>

        {/* Solo para desarrollo: se elimina cuando se conecte la autenticación real. */}
        <div className="mt-6 rounded-lg bg-neutral-950 p-3 text-xs text-neutral-500">
          <p className="mb-1 font-medium text-neutral-400">Usuarios de prueba</p>
          <p>alumno@meru.com / alumno123</p>
          <p>profesor@meru.com / profe123</p>
          <p>admin@meru.com / admin123</p>
        </div>
      </div>
    </div>
  )
}