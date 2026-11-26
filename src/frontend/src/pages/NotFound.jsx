import { Link } from 'react-router-dom'
import logo from '../assets/logo-meru.png'

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 p-4">
      <img src={logo} alt="Meru App" className="w-40" />
      <h1 className="text-5xl font-bold text-azul">404</h1>
      <p className="text-negro/70">La página que buscás no existe.</p>
      <Link
        to="/"
        className="rounded-lg bg-celeste px-4 py-2 text-sm font-medium text-negro transition hover:bg-celeste/80"
      >
        Volver al inicio
      </Link>
    </div>
  )
}
