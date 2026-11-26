import { useState } from 'react'

// Gráfico de columnas dibujado con SVG (sin librerías). Sirve para una serie o para columnas apiladas.
//  - categorias: [{ etiqueta, titulo, valores: [n, n...] }]  (un valor por serie)
//  - series: [{ nombre, color }]
//  - formato: función que convierte un número en texto
//  - etiquetarMaximo: escribe el valor sobre la columna más alta (la única etiqueta directa)
//  - ancho: ancho lógico del dibujo. Usar ~440 en tarjetas angostas para que el texto no quede diminuto
const ANCHO_DEFECTO = 640 // ancho lógico del dibujo; el SVG se escala al ancho de la tarjeta
const ALTO = 240
const MARGEN = { izq: 56, der: 12, sup: 22, inf: 28 }
const COLOR_GRILLA = 'rgba(30, 30, 36, 0.15)' // Negro Deportivo, tenue
const COLOR_TEXTO = 'rgba(30, 30, 36, 0.7)' // Negro Deportivo

// Marcas del eje Y redondeadas (0, 100, 200...) que cubren el valor máximo.
function marcasEjeY(maximo) {
  if (maximo <= 0) return [0, 1]
  const base = 10 ** Math.floor(Math.log10(maximo / 4))
  let paso = base
  for (const multiplo of [1, 2, 5, 10]) {
    paso = multiplo * base
    if (Math.ceil(maximo / paso) <= 5) break
  }
  const cantidad = Math.ceil(maximo / paso)
  return Array.from({ length: cantidad + 1 }, (_, i) => i * paso)
}

// Segmento con las esquinas superiores redondeadas (4 px) y la base recta.
function rutaRedondeada(x, y0, y1, ancho) {
  const r = Math.min(4, (y1 - y0) / 2, ancho / 2)
  return (
    `M${x},${y1} L${x},${y0 + r} Q${x},${y0} ${x + r},${y0} L${x + ancho - r},${y0} ` +
    `Q${x + ancho},${y0} ${x + ancho},${y0 + r} L${x + ancho},${y1} Z`
  )
}

export default function GraficoColumnas({
  categorias,
  series,
  formato,
  etiquetarMaximo = false,
  descripcion,
  ancho = ANCHO_DEFECTO,
}) {
  const ANCHO = ancho
  const [activa, setActiva] = useState(null)

  const totales = categorias.map((c) => c.valores.reduce((a, b) => a + b, 0))
  const marcas = marcasEjeY(Math.max(...totales))
  const tope = marcas[marcas.length - 1]
  const alturaUtil = ALTO - MARGEN.sup - MARGEN.inf
  const base = ALTO - MARGEN.inf
  const escala = alturaUtil / tope
  const banda = (ANCHO - MARGEN.izq - MARGEN.der) / categorias.length
  const grosor = Math.min(24, banda * 0.6)
  const indiceMaximo = totales.indexOf(Math.max(...totales))

  return (
    <div className="relative">
      <svg viewBox={`0 0 ${ANCHO} ${ALTO}`} className="w-full" role="group" aria-label={descripcion}>
        {marcas.map((marca) => {
          const y = base - marca * escala
          return (
            <g key={marca}>
              <line x1={MARGEN.izq} x2={ANCHO - MARGEN.der} y1={y} y2={y} stroke={COLOR_GRILLA} strokeWidth="1" />
              <text x={MARGEN.izq - 8} y={y + 4} textAnchor="end" fontSize="11" fill={COLOR_TEXTO}>
                {formato(marca, true)}
              </text>
            </g>
          )
        })}

        {categorias.map((categoria, i) => {
          const x = MARGEN.izq + banda * i + (banda - grosor) / 2
          const centro = MARGEN.izq + banda * (i + 0.5)
          const ultimoConValor = categoria.valores.map((v) => v > 0).lastIndexOf(true)
          let acumulado = 0
          return (
            <g key={categoria.etiqueta} style={activa === i ? { filter: 'brightness(1.2)' } : undefined}>
              {categoria.valores.map((valor, k) => {
                if (valor <= 0) return null
                const y0 = base - (acumulado + valor) * escala
                // 2 px de aire entre un segmento y el que tiene debajo.
                const y1 = base - acumulado * escala - (acumulado > 0 ? 2 : 0)
                acumulado += valor
                if (y1 - y0 < 1) return null
                return k === ultimoConValor ? (
                  <path key={k} d={rutaRedondeada(x, y0, y1, grosor)} fill={series[k].color} />
                ) : (
                  <rect key={k} x={x} y={y0} width={grosor} height={y1 - y0} fill={series[k].color} />
                )
              })}
              {etiquetarMaximo && i === indiceMaximo && totales[i] > 0 && (
                <text
                  x={centro}
                  y={base - totales[i] * escala - 6}
                  textAnchor="middle"
                  fontSize="11"
                  fontWeight="600"
                  fill="#1e1e24"
                >
                  {formato(totales[i], true)}
                </text>
              )}
              <text x={centro} y={ALTO - 8} textAnchor="middle" fontSize="11" fill={COLOR_TEXTO}>
                {categoria.etiqueta}
              </text>
              {/* Zona sensible al mouse y al teclado: más grande que la columna. */}
              <rect
                x={MARGEN.izq + banda * i}
                y={MARGEN.sup}
                width={banda}
                height={alturaUtil + MARGEN.inf}
                fill="transparent"
                tabIndex={0}
                aria-label={`${categoria.titulo}: ${categoria.valores
                  .map((v, k) => `${series[k].nombre} ${formato(v)}`)
                  .join(', ')}`}
                onPointerEnter={() => setActiva(i)}
                onPointerLeave={() => setActiva(null)}
                onFocus={() => setActiva(i)}
                onBlur={() => setActiva(null)}
                style={{ outline: 'none' }}
              />
            </g>
          )
        })}
      </svg>

      {activa !== null && (
        <div
          className={`pointer-events-none absolute top-2 z-10 min-w-36 rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-xs shadow-lg ${
            activa < categorias.length / 2 ? '' : '-translate-x-full'
          }`}
          // El cuadro se abre al costado de la columna para no tapar su parte alta.
          style={{
            left: `${((MARGEN.izq + banda * (activa < categorias.length / 2 ? activa + 1 : activa)) / ANCHO) * 100}%`,
          }}
        >
          <p className="mb-1 text-negro/70">{categorias[activa].titulo}</p>
          {categorias[activa].valores.map((valor, k) => (
            <p key={k} className="flex items-center gap-2">
              <span className="inline-block h-0.5 w-3" style={{ backgroundColor: series[k].color }} />
              <span className="font-semibold text-negro">{formato(valor)}</span>
              {series.length > 1 && <span className="text-negro/70">{series[k].nombre}</span>}
            </p>
          ))}
        </div>
      )}
    </div>
  )
}