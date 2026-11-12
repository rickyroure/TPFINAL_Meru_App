import { useAuth } from '../hooks/useAuth'

// Encabezado de la pantalla de inicio de cada panel. Lo que se pasa como
// "children" (las tarjetas de resumen) se muestra debajo del saludo.
export default function Bienvenida({ descripcion, children }) {
  const { usuario } = useAuth()

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Hola, {usuario.nombre}</h2>
      <p className="mb-6 mt-1 text-neutral-400">{descripcion}</p>
      {children}
    </div>
  )
}