import { useAuth } from '../hooks/useAuth'

// Pantalla de inicio provisoria de cada panel. Se reemplaza a medida que
// desarrollemos las pantallas reales.
export default function Bienvenida({ descripcion }) {
  const { usuario } = useAuth()

  return (
    <div>
      <h2 className="text-2xl font-bold">Hola, {usuario.nombre}</h2>
      <p className="mt-2 text-neutral-400">{descripcion}</p>
    </div>
  )
}