import { Link } from 'react-router-dom'

// Tarjeta con un dato destacado, para las pantallas de inicio.
//  - titulo: qué es el dato ("Mi plan")
//  - valor: el dato en grande ("Mensual"). Si es null se muestra "—".
//  - detalle: una línea de apoyo
//  - aviso: true pinta el detalle en amarillo (algo que requiere atención)
//  - to: si se indica, toda la tarjeta lleva a esa pantalla
export default function TarjetaResumen({ titulo, valor, detalle, aviso = false, to }) {
  const contenido = (
    <>
      <p className="text-xs uppercase tracking-wide text-neutral-500">{titulo}</p>
      <p className="mt-2 text-2xl font-bold">{valor ?? '—'}</p>
      {detalle && (
        <p className={`mt-1 text-sm ${aviso ? 'text-amber-400' : 'text-neutral-400'}`}>{detalle}</p>
      )}
    </>
  )
  const clase = 'block rounded-2xl border border-neutral-800 bg-neutral-900 p-5'

  if (to) {
    return (
      <Link to={to} className={`${clase} transition hover:border-emerald-500/60`}>
        {contenido}
      </Link>
    )
  }
  return <div className={clase}>{contenido}</div>
}