import { useEffect, useState } from 'react'
import { productosService } from '../../services/productos'
import { mensajeDeError } from '../../utils/errores'

const STOCK_BAJO = 5

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

const formatoPrecio = (precio) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(precio)

// Formulario para crear un producto o editar uno existente (si recibe "producto").
function FormularioProducto({ producto, onGuardado, onCancelar }) {
  const [nombre, setNombre] = useState(producto?.nombre ?? '')
  const [precio, setPrecio] = useState(producto ? String(producto.precio) : '')
  const [stock, setStock] = useState(producto ? String(producto.stock) : '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  const guardar = async (evento) => {
    evento.preventDefault()
    setError('')
    const datos = {
      nombre: nombre.trim(),
      precio: precio === '' ? null : Number(precio),
      stock: stock === '' ? null : Number(stock),
    }
    setGuardando(true)
    try {
      const resultado = producto
        ? await productosService.actualizar(producto.id, datos)
        : await productosService.crear(datos)
      onGuardado(resultado, Boolean(producto))
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={guardar} className="grid gap-3 rounded-2xl border border-emerald-500/40 bg-neutral-900 p-5 sm:grid-cols-4">
      <h3 className="font-semibold sm:col-span-4">
        {producto ? `Editar producto "${producto.nombre}"` : 'Nuevo producto'}
      </h3>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Nombre
        <input className={claseInput} value={nombre} onChange={(e) => setNombre(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Precio (ARS)
        <input type="number" min="0" className={claseInput} value={precio} onChange={(e) => setPrecio(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Stock (unidades)
        <input type="number" min="0" step="1" className={claseInput} value={stock} onChange={(e) => setStock(e.target.value)} />
      </label>
      <div className="flex items-end gap-2">
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Guardar'}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-neutral-700 px-4 py-2 text-sm text-neutral-300 hover:bg-neutral-800"
        >
          Cancelar
        </button>
      </div>
      {error && <p className="text-sm text-red-400 sm:col-span-4">{error}</p>}
    </form>
  )
}

export default function Productos() {
  const [productos, setProductos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  // null = formulario cerrado, 'nuevo' = alta, o el producto que se está editando.
  const [formulario, setFormulario] = useState(null)

  useEffect(() => {
    let cancelado = false
    productosService
      .listar()
      .then((datos) => {
        if (!cancelado) setProductos(datos)
      })
      .catch((err) => {
        if (!cancelado) setError(mensajeDeError(err))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [])

  const alGuardar = (producto, eraEdicion) => {
    setProductos((anteriores) =>
      eraEdicion ? anteriores.map((p) => (p.id === producto.id ? producto : p)) : [...anteriores, producto],
    )
    setFormulario(null)
    setAviso(eraEdicion ? `Producto "${producto.nombre}" actualizado.` : `Producto "${producto.nombre}" creado.`)
  }

  if (cargando) return <p className="text-neutral-400">Cargando productos...</p>
  if (error) return <p className="text-red-400">{error}</p>

  return (
    <div className="max-w-4xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold">Productos del shop</h2>
          <p className="mt-1 text-sm text-neutral-400">
            Los cambios se ven al instante en el Shop de los alumnos. Para dejar de vender un producto, ponele stock 0.
          </p>
        </div>
        {formulario === null && (
          <button
            onClick={() => {
              setAviso('')
              setFormulario('nuevo')
            }}
            className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400"
          >
            + Nuevo producto
          </button>
        )}
      </div>

      {aviso && <p className="mb-4 rounded-lg bg-emerald-500/10 px-4 py-2 text-sm text-emerald-400">{aviso}</p>}

      {formulario !== null && (
        <div className="mb-6">
          <FormularioProducto
            key={formulario === 'nuevo' ? 'nuevo' : formulario.id}
            producto={formulario === 'nuevo' ? null : formulario}
            onGuardado={alGuardar}
            onCancelar={() => setFormulario(null)}
          />
        </div>
      )}

      {productos.length === 0 ? (
        <p className="text-neutral-400">Todavía no hay productos cargados.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {productos.map((p) => (
            <li
              key={p.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-neutral-800 bg-neutral-900 p-4"
            >
              <div>
                <p className="font-semibold">{p.nombre}</p>
                {p.stock <= 0 ? (
                  <p className="text-sm text-red-400">Sin stock</p>
                ) : p.stock <= STOCK_BAJO ? (
                  <p className="text-sm text-amber-400">Stock bajo: {p.stock}</p>
                ) : (
                  <p className="text-sm text-neutral-400">Stock: {p.stock}</p>
                )}
              </div>
              <div className="flex items-center gap-4">
                <span className="text-lg font-semibold text-emerald-400">{formatoPrecio(p.precio)}</span>
                <button
                  onClick={() => {
                    setAviso('')
                    setFormulario(p)
                  }}
                  className="rounded-lg border border-neutral-700 px-3 py-1.5 text-sm text-neutral-300 transition hover:bg-neutral-800"
                >
                  Editar
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}