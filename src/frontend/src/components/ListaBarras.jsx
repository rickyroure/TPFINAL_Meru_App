// Barras horizontales para comparar categorías con nombres largos (por ejemplo actividades).
//  - filas: [{ nombre, valor, detalle }]  (detalle es el texto que aparece al pasar el mouse)
export default function ListaBarras({ filas, color }) {
  const maximo = Math.max(...filas.map((f) => f.valor), 1)
  return (
    <ul className="flex flex-col gap-1">
      {filas.map((fila) => (
        <li
          key={fila.nombre}
          tabIndex={0}
          className="group relative grid grid-cols-[8.5rem_1fr] items-center gap-3 rounded-md py-1.5 outline-none focus-visible:bg-negro/10 hover:bg-negro/10"
        >
          <span className="truncate text-sm text-negro">{fila.nombre}</span>
          <div className="flex items-center gap-2">
            <div
              className="h-4 rounded-r-[4px] transition group-hover:brightness-125"
              style={{ width: `${(fila.valor / maximo) * 85}%`, backgroundColor: color }}
            />
            <span className="text-sm font-semibold text-negro">{fila.valor}</span>
          </div>
          <div className="pointer-events-none absolute right-2 top-full z-10 hidden rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-xs text-negro shadow-lg group-hover:block group-focus:block">
            {fila.detalle}
          </div>
        </li>
      ))}
    </ul>
  )
}