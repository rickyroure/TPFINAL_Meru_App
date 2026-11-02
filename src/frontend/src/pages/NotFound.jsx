import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <h1 className="text-5xl font-bold text-emerald-500">404</h1>
      <p className="text-neutral-400">La página que buscás no existe.</p>
      <Link to="/" className="text-sm text-emerald-400 hover:underline">
        Volver al inicio
      </Link>
    </div>
  )
}