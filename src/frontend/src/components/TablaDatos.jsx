// Tabla con los mismos datos del gráfico: es la alternativa accesible y no depende del hover.
export default function TablaDatos({ columnas, filas }) {
  return (
    <details className="mt-4">
      <summary className="cursor-pointer text-sm text-azul">Ver como tabla</summary>
      <div className="mt-2 overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="text-xs uppercase tracking-wide text-negro/65">
            <tr>
              {columnas.map((c) => (
                <th key={c} className="py-1.5 pr-4 font-medium">
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-negro/10">
            {filas.map((fila, i) => (
              <tr key={i}>
                {fila.map((celda, j) => (
                  <td key={j} className="py-1.5 pr-4 tabular-nums text-negro">
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