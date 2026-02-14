import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { ProtectedRoute } from '@/components/ProtectedRoute'

describe('ProtectedRoute', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: null,
      accessToken: null,
      isAuthenticated: false,
    })
  })

  it('debe redirigir a /login si no esta autenticado', () => {
    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Contenido protegido</div>
        </ProtectedRoute>
      </MemoryRouter>
    )

    expect(screen.queryByText('Contenido protegido')).not.toBeInTheDocument()
  })

  it('debe mostrar contenido si esta autenticado', () => {
    useAuthStore.setState({
      user: { id: '1', email: 'test@test.com', nombre: 'Test', apellido: 'User', rol: 'OBSTETRA' },
      accessToken: 'token123',
      isAuthenticated: true,
    })

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Contenido protegido</div>
        </ProtectedRoute>
      </MemoryRouter>
    )

    expect(screen.getByText('Contenido protegido')).toBeInTheDocument()
  })
})
