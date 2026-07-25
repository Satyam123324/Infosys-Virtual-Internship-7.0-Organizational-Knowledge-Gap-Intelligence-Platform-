import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // OAuth redirect URIs are registered for a fixed port, so don't let Vite
    // silently drift to 5174/5175 when 5173 is taken — fail loudly instead.
    strictPort: true,
  },
})
