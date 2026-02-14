import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useAuthStore } from '@/stores/authStore'

const storageMock: Record<string, string> = {}

beforeEach(() => {
  Object.keys(storageMock).forEach((key) => delete storageMock[key])

  vi.stubGlobal('localStorage', {
    getItem: (key: string) => storageMock[key] ?? null,
    setItem: (key: string, value: string) => { storageMock[key] = value },
    removeItem: (key: string) => { delete storageMock[key] },
    clear: () => { Object.keys(storageMock).forEach((key) => delete storageMock[key]) },
    length: 0,
    key: () => null,
  })
})

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: null,
      accessToken: null,
      isAuthenticated: false,
    })
  })

  it('debe iniciar sin autenticacion', () => {
    const state = useAuthStore.getState()
    expect(state.isAuthenticated).toBe(false)
    expect(state.user).toBeNull()
    expect(state.accessToken).toBeNull()
  })

  it('debe autenticar al usuario con login', () => {
    const user = { id: '1', email: 'test@test.com', nombre: 'Test', apellido: 'User', rol: 'OBSTETRA' as const }
    useAuthStore.getState().login(user, 'token123', 'refresh123')

    const state = useAuthStore.getState()
    expect(state.isAuthenticated).toBe(true)
    expect(state.user).toEqual(user)
    expect(state.accessToken).toBe('token123')
    expect(storageMock['accessToken']).toBe('token123')
    expect(storageMock['refreshToken']).toBe('refresh123')
  })

  it('debe limpiar estado con logout', () => {
    const user = { id: '1', email: 'test@test.com', nombre: 'Test', apellido: 'User', rol: 'OBSTETRA' as const }
    useAuthStore.getState().login(user, 'token123', 'refresh123')
    useAuthStore.getState().logout()

    const state = useAuthStore.getState()
    expect(state.isAuthenticated).toBe(false)
    expect(state.user).toBeNull()
    expect(storageMock['accessToken']).toBeUndefined()
    expect(storageMock['refreshToken']).toBeUndefined()
  })

  it('debe cargar usuario desde localStorage con token valido', () => {
    const payload = btoa(JSON.stringify({
      sub: 'user@test.com',
      userId: 'uuid-123',
      rol: 'OBSTETRA',
      nombre: 'Ana',
      apellido: 'Lopez',
    }))
    storageMock['accessToken'] = `header.${payload}.signature`

    useAuthStore.getState().loadFromStorage()

    const state = useAuthStore.getState()
    expect(state.isAuthenticated).toBe(true)
    expect(state.user?.email).toBe('user@test.com')
    expect(state.user?.rol).toBe('OBSTETRA')
  })

  it('no debe autenticar si no hay token en localStorage', () => {
    useAuthStore.getState().loadFromStorage()

    const state = useAuthStore.getState()
    expect(state.isAuthenticated).toBe(false)
    expect(state.user).toBeNull()
  })
})
