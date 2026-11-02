import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { RUTA_POR_ROL } from '../constants/roles'
import { USE_MOCK } from '../config'

const CUENTAS_PRUEBA = USE_MOCK
  ? [
      { etiqueta: 'Alumno', usuario: 'alumno@meru.com', password: 'alumno123' },
      { etiqueta: 'Profesor', usuario: 'profesor@meru.com', password: 'profe123' },
      { etiqueta: 'Admin', usuario: 'admin@meru.com', password: 'admin123' },
    ]
  : [
      { etiqueta: 'Alumno', usuario: 'alumno.test', password: 'alumno1234' },
      { etiqueta: 'Profesor', usuario: 'profesor.test', password: 'profesor1234' },
      { etiqueta: 'Admin', usuario: 'admin.test', password: 'admin1234' },
    ]

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

  const autocompletar = (cuenta) => {
    setEmail(cuenta.usuario)
    setPassword(cuenta.password)
    setError('')
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <div className="w-full max-w-sm rounded-2xl border border-neutral-800 bg-neutral-900 p-8">
        <h1 className="text-3xl font-bold text-emerald-500">Meru</h1>
        <p className="mb-6 mt-1 text-sm text-neutral-400">Ingresá a tu cuenta</p>

        <form onSubmit={enviarFormulario} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm">
            Usuario o Email
            <input
              type="text"
              required
              autoComplete="username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder={USE_MOCK ? 'alumno@meru.com' : 'alumno.test'}
              className="rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 outline-none focus:border-emerald-500"
            />
          </label>

          <label className="flex flex-col gap-1 text-sm">
            Contraseña
            <input
              type="password"
              required
              autoComplete="current-password"
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

        <div className="mt-6 rounded-lg bg-neutral-950 p-3 text-xs text-neutral-500">
          <p className="mb-2 font-medium text-neutral-400">
            Cuentas de prueba ({USE_MOCK ? 'Modo Mock' : 'Keycloak Local'}) — tocá para completar:
          </p>
          <div className="flex flex-col gap-1.5">
            {CUENTAS_PRUEBA.map((c) => (
              <button
                key={c.usuario}
                type="button"
                onClick={() => autocompletar(c)}
                className="flex items-center justify-between rounded px-2 py-1 text-left transition hover:bg-neutral-900 hover:text-emerald-400"
              >
                <span className="font-medium text-neutral-300">{c.etiqueta}:</span>
                <span>
                  {c.usuario} / {c.password}
                </span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
