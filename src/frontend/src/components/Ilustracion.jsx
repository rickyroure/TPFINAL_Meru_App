// Ilustraciones de referencia para clases y productos.
// Mismo estilo para todas: trazo blanco con acento Celeste Activo, sobre un fondo
// degradado de Azul Principal a Negro Deportivo (los colores del logo).
//  - clave: la devuelve claveActividad() o claveProducto() (utils/imagenes.js)
//  - variante: "banner" (parte superior de una tarjeta) o "miniatura" (para listas)

const A = '#00bfff' // Celeste Activo
const N = '#1e1e24' // Negro Deportivo

function Pote({ emblema }) {
  return (
    <>
      <rect x="16" y="14" width="32" height="9" rx="3" fill={A} />
      <rect x="18" y="23" width="28" height="31" rx="4" />
      <rect x="23" y="29" width="18" height="18" rx="3" fill="#fff" stroke="none" />
      {emblema}
    </>
  )
}

const DIBUJOS = {
  // ---- Actividades ----
  yoga: (
    <>
      <path d="M8 34 A24 24 0 0 1 56 34" stroke={A} strokeDasharray="2 5" />
      <circle cx="32" cy="13" r="5" fill={A} />
      <path d="M32 20 V34" />
      <path d="M32 26 Q22 28 15 38" />
      <path d="M32 26 Q42 28 49 38" />
      <path d="M12 50 Q22 40 32 46 Q42 40 52 50 Q42 56 32 52 Q22 56 12 50 Z" />
    </>
  ),
  spinning: (
    <>
      <path d="M10 54 H56" />
      <circle cx="22" cy="40" r="11" />
      <circle cx="22" cy="40" r="3" fill={A} stroke="none" />
      <path d="M22 40 L24 18" />
      <path d="M18 18 H30" stroke={A} />
      <path d="M22 40 L44 26" />
      <path d="M44 26 V54" />
      <path d="M37 24 H52" stroke={A} />
    </>
  ),
  funcional: (
    <>
      <path d="M23 25 C21 8 43 8 41 25" />
      <circle cx="32" cy="40" r="15" />
      <path d="M22 36 Q32 30 42 36" stroke={A} />
    </>
  ),
  musculacion: (
    <>
      <path d="M22 32 H42" />
      <rect x="8" y="26" width="6" height="12" rx="2" />
      <rect x="15" y="19" width="7" height="26" rx="2" fill={A} />
      <rect x="50" y="26" width="6" height="12" rx="2" />
      <rect x="42" y="19" width="7" height="26" rx="2" fill={A} />
    </>
  ),
  pilates: (
    <>
      <circle cx="32" cy="28" r="17" />
      <path d="M16 22 Q32 34 48 22" />
      <path d="M19 40 Q32 30 45 40" stroke={A} />
      <path d="M8 54 H56" />
    </>
  ),
  baile: (
    <>
      <circle cx="21" cy="46" r="6" fill={A} />
      <circle cx="43" cy="40" r="6" fill={A} />
      <path d="M27 46 V16 L49 10 V40" />
      <path d="M27 24 L49 18" />
    </>
  ),
  cardio: (
    <>
      <path d="M32 52 C12 38 10 22 22 18 C28 16 32 20 32 24 C32 20 36 16 42 18 C54 22 52 38 32 52 Z" />
      <path d="M14 34 H24 L28 26 L34 42 L38 34 H50" stroke={A} />
    </>
  ),

  // ---- Productos ----
  proteina: <Pote emblema={<path d="M32 31 Q39 39 32 45 Q25 39 32 31 Z" fill={A} stroke="none" />} />,
  preentreno: <Pote emblema={<path d="M33 31 L27 40 H32 L30 46 L38 36 H33 Z" fill={N} stroke="none" />} />,
  creatina: (
    <Pote emblema={<path d="M32 31 L38 34.5 V41.5 L32 45 L26 41.5 V34.5 Z" fill="none" stroke={N} strokeWidth="2" />} />
  ),
  bcaa: (
    <Pote
      emblema={
        <g fill={N} stroke="none">
          <rect x="25" y="39" width="3" height="6" />
          <rect x="30.5" y="34" width="3" height="11" />
          <rect x="36" y="30" width="3" height="15" />
        </g>
      }
    />
  ),
  shaker: (
    <>
      <rect x="26" y="8" width="12" height="6" rx="2" />
      <rect x="18" y="14" width="28" height="8" rx="3" fill={A} />
      <path d="M20 22 L24 54 Q24 56 26 56 H38 Q40 56 40 54 L44 22" />
      <path d="M28 32 H36 M27 40 H37 M27 48 H37" stroke={A} />
    </>
  ),
  barra: (
    <g transform="rotate(-20 32 32)">
      <rect x="8" y="22" width="48" height="20" rx="5" />
      <path d="M14 22 V42 M50 22 V42" stroke={A} strokeDasharray="3 3" />
      <path d="M24 32 H40" stroke={A} strokeWidth="4" />
    </g>
  ),
  vitaminas: (
    <>
      <rect x="25" y="12" width="14" height="10" rx="3" fill={A} />
      <rect x="22" y="22" width="20" height="32" rx="5" />
      <g transform="rotate(-40 32 38)">
        <rect x="25" y="33" width="14" height="10" rx="5" fill="#fff" stroke="none" />
        <path d="M32 33 V43" stroke={N} strokeWidth="1.5" />
      </g>
    </>
  ),
  banda: (
    <>
      <ellipse cx="32" cy="32" rx="20" ry="12" stroke={A} strokeWidth="5" />
      <rect x="6" y="26" width="7" height="12" rx="3" fill="#fff" stroke="none" />
      <rect x="51" y="26" width="7" height="12" rx="3" fill="#fff" stroke="none" />
    </>
  ),

  // ---- Genérica (montaña de la marca) ----
  generico: (
    <>
      <path d="M8 52 L26 22 L35 36 L41 28 L56 52 Z" />
      <circle cx="46" cy="16" r="5" fill={A} stroke="none" />
    </>
  ),
}

const TAMANOS = {
  banner: { caja: '-mx-5 -mt-5 mb-4 h-28 rounded-t-2xl', icono: 'h-20 w-20' },
  miniatura: { caja: 'h-12 w-12 shrink-0 rounded-xl', icono: 'h-8 w-8' },
}

export default function Ilustracion({ clave, variante = 'banner' }) {
  const { caja, icono } = TAMANOS[variante]
  return (
    <div
      aria-hidden="true"
      className={`relative flex items-center justify-center overflow-hidden bg-gradient-to-br from-azul to-negro ${caja}`}
    >
      <div className="absolute -right-4 -top-4 h-16 w-16 rounded-full bg-celeste/15" />
      <svg
        viewBox="0 0 64 64"
        fill="none"
        stroke="#fff"
        strokeWidth="3"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={`relative ${icono}`}
      >
        {DIBUJOS[clave] ?? DIBUJOS.generico}
      </svg>
    </div>
  )
}
