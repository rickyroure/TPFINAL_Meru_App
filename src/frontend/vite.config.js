import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  plugins: [react(), tailwindcss()],
  // En desarrollo la app vive en "/". Al generar el build para GitHub Pages
  // vive en "/<nombre-del-repo>/", por eso la ruta base cambia.
  base: command === 'build' ? '/TPFINAL_Meru_App/' : '/',
}))