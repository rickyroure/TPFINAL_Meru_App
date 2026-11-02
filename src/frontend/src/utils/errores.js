// Convierte cualquier error en un mensaje entendible para mostrar en pantalla.
// El backend responde los errores con { detail: "mensaje" }.
export function mensajeDeError(error) {
  if (error?.code === 'ERR_NETWORK') {
    return 'No se pudo conectar con el servidor. Verificá que el backend esté encendido.'
  }
  return error?.response?.data?.detail ?? error?.message ?? 'Ocurrió un error inesperado.'
}