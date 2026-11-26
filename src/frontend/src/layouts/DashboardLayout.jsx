import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { MENU_POR_ROL } from '../constants/navigation'
import { RUTA_POR_ROL } from '../constants/roles'
import logoContorno from '../assets/logo-meru-contorno.png'

// Estructura común de los tres paneles: menú lateral + contenido.
export default function DashboardLayout() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()
  const menu = MENU_POR_ROL[usuario.rol] ?? []

  const cerrarSesion = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col bg-azul p-4 text-white">
        {/* Logo de la marca: lleva a la pantalla de inicio del panel del usuario. */}
        <Link
          to={RUTA_POR_ROL[usuario.rol]}
          aria-label="Meru App - ir al inicio"
          title="Ir al inicio"
          className="mb-6 block rounded-lg outline-none transition hover:opacity-90 focus-visible:ring-2 focus-visible:ring-celeste"
        >
          <img src={logoContorno} alt="Meru App" className="mx-auto w-36" />
        </Link>

        <nav className="flex flex-1 flex-col gap-1">
          {menu.map((item) =>
            item.disponible ? (
              <NavLink
                key={item.label}
                to={item.to}
                end
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2 text-sm font-medium transition ${
                    isActive ? 'bg-celeste text-negro' : 'text-white hover:bg-white/10'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ) : (
              <span
                key={item.label}
                className="flex cursor-not-allowed items-center justify-between rounded-lg px-3 py-2 text-sm text-white/50"
              >
                {item.label}
                <span className="text-[10px] uppercase tracking-wide">Pronto</span>
              </span>
            ),
          )}
        </nav>

        <div className="border-t border-white/20 pt-4">
          <p className="text-sm font-medium">{usuario.nombre}</p>
          <p className="mb-3 text-xs capitalize text-white/70">{usuario.rol}</p>
          <button
            onClick={cerrarSesion}
            className="w-full rounded-lg border border-white/40 px-3 py-2 text-sm transition hover:bg-white/10"
          >
            Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="flex-1 p-8">
        <Outlet />
      </main>
    </div>
  )
}
