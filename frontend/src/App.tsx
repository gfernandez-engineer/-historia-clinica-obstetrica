import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { HelmetProvider } from 'react-helmet-async'
import { useEffect } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { Layout } from '@/components/Layout'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { PacientesPage } from '@/pages/PacientesPage'
import { PacienteFormPage } from '@/pages/PacienteFormPage'
import { HistoriasPage } from '@/pages/HistoriasPage'
import { HistoriaFormPage } from '@/pages/HistoriaFormPage'
import { HistoriaDetallePage } from '@/pages/HistoriaDetallePage'
import { ExportacionesPage } from '@/pages/ExportacionesPage'

function App() {
  const loadFromStorage = useAuthStore((s) => s.loadFromStorage)

  useEffect(() => {
    loadFromStorage()
  }, [loadFromStorage])

  return (
    <HelmetProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<DashboardPage />} />
            <Route path="pacientes" element={<PacientesPage />} />
            <Route path="pacientes/nuevo" element={<PacienteFormPage />} />
            <Route path="pacientes/:id" element={<PacienteFormPage />} />
            <Route path="historias" element={<HistoriasPage />} />
            <Route path="historias/nueva" element={<HistoriaFormPage />} />
            <Route path="historias/:id" element={<HistoriaDetallePage />} />
            <Route path="historias/:id/editar" element={<HistoriaFormPage />} />
            <Route path="exportaciones" element={<ExportacionesPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </HelmetProvider>
  )
}

export default App
