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
      <p className="text-xs uppercase tracking-wide text-negro0">{titulo}</p>
      <p className="mt-2 text-2xl font-bold">{valor ?? '—'}</p>
      {detalle && (
        <p className={`mt-1 text-sm ${aviso ? 'text-amber-700' : 'text-negro/70'}`}>{detalle}</p>
      )}
    </>
  )
  const clase = 'block rounded-2xl border border-negro/10 bg-gris p-5'

  if (to) {
    return (
      <Link to={to} className={`${clase} transition hover:border-celeste/60`}>
        {contenido}
      </Link>
    )
  }
  return <div className={clase}>{contenido}</div>
}