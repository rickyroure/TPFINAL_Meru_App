// Tabla con los mismos datos del gráfico: es la alternativa accesible y no depende del hover.
export default function TablaDatos({ columnas, filas }) {
  return (
    <details className="mt-4">
      <summary className="cursor-pointer text-sm text-emerald-400">Ver como tabla</summary>
      <div className="mt-2 overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="text-xs uppercase tracking-wide text-neutral-500">
            <tr>
              {columnas.map((c) => (
                <th key={c} className="py-1.5 pr-4 font-medium">
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-800">
            {filas.map((fila, i) => (
              <tr key={i}>
                {fila.map((celda, j) => (
                  <td key={j} className="py-1.5 pr-4 tabular-nums text-neutral-300">
                    {celda}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </details>
  )
}