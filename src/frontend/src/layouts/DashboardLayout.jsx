import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { MENU_POR_ROL } from '../constants/navigation'

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
      <aside className="flex w-60 shrink-0 flex-col border-r border-neutral-800 bg-neutral-900 p-4">
        <h1 className="mb-8 text-2xl font-bold text-emerald-500">Meru</h1>

        <nav className="flex flex-1 flex-col gap-1">
          {menu.map((item) =>
            item.disponible ? (
              <NavLink
                key={item.label}
                to={item.to}
                end
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2 text-sm font-medium transition ${
                    isActive
                      ? 'bg-emerald-500/15 text-emerald-400'
                      : 'text-neutral-300 hover:bg-neutral-800'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ) : (
              <span
                key={item.label}
                className="flex cursor-not-allowed items-center justify-between rounded-lg px-3 py-2 text-sm text-neutral-600"
              >
                {item.label}
                <span className="text-[10px] uppercase tracking-wide">Pronto</span>
              </span>
            ),
          )}
        </nav>

        <div className="border-t border-neutral-800 pt-4">
          <p className="text-sm font-medium">{usuario.nombre}</p>
          <p className="mb-3 text-xs text-neutral-500">{usuario.rol}</p>
          <button
            onClick={cerrarSesion}
            className="w-full rounded-lg border border-neutral-700 px-3 py-2 text-sm transition hover:bg-neutral-800"
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