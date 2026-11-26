import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { RUTA_POR_ROL } from '../constants/roles'
import { USE_MOCK } from '../config'
import logo from '../assets/logo-meru.png'
import logoContorno from '../assets/logo-meru-contorno.png'

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
    <div className="flex min-h-screen">
      {/* Panel de marca (solo en pantallas medianas o grandes) */}
      <div className="hidden flex-1 items-center justify-center bg-azul p-10 md:flex">
        <div className="flex max-w-md flex-col items-center text-center text-white">
          <img src={logoContorno} alt="Meru App" className="w-72" />
          <p className="mt-6 text-lg font-medium">Entrená, reservá y avanzá.</p>
          <p className="mt-1 text-sm text-white/80">
            Rutinas, clases y membresías de tu gimnasio en un solo lugar.
          </p>
        </div>
      </div>

      <div className="flex flex-1 items-center justify-center p-4">
        <div className="w-full max-w-sm rounded-2xl border border-negro/10 bg-gris p-8">
          <img src={logo} alt="Meru App" className="mx-auto mb-4 w-32 md:hidden" />
          <h1 className="text-3xl font-bold text-azul">Bienvenido</h1>
          <p className="mb-6 mt-1 text-sm text-negro/70">Ingresá a tu cuenta</p>

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
                className="rounded-lg border border-negro/20 bg-fondo px-3 py-2 outline-none focus:border-celeste"
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
                className="rounded-lg border border-negro/20 bg-fondo px-3 py-2 outline-none focus:border-celeste"
              />
            </label>

            {error && <p className="text-sm text-red-700">{error}</p>}

            <button
              type="submit"
              disabled={cargando}
              className="rounded-lg bg-celeste px-3 py-2 font-medium text-negro transition hover:bg-celeste/80 disabled:opacity-60"
            >
              {cargando ? 'Ingresando...' : 'Ingresar'}
            </button>
          </form>

          <div className="mt-6 rounded-lg bg-fondo p-3 text-xs text-negro0">
            <p className="mb-2 font-medium text-negro/70">
              Cuentas de prueba ({USE_MOCK ? 'Modo Mock' : 'Keycloak Local'}) — tocá para completar:
            </p>
            <div className="flex flex-col gap-1.5">
              {CUENTAS_PRUEBA.map((c) => (
                <button
                  key={c.usuario}
                  type="button"
                  onClick={() => autocompletar(c)}
                  className="flex items-center justify-between rounded px-2 py-1 text-left transition hover:bg-gris hover:text-azul"
                >
                  <span className="font-medium text-negro">{c.etiqueta}:</span>
                  <span>
                    {c.usuario} / {c.password}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>
        </div>
    </div>
  )
}
