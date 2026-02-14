import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/authStore'

export function LoginPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const { data } = await authApi.login({ email, password })
      login(
        { id: data.userId, email: data.email, nombre: '', apellido: '', rol: data.rol },
        data.accessToken,
        data.refreshToken
      )
      navigate('/')
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } }
      setError(axiosErr.response?.data?.message || t('common.error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Helmet><title>{t('auth.login')} - {t('app.title')}</title></Helmet>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f7fafc' }}>
        <div style={{ background: 'white', padding: 32, borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.1)', width: 400 }}>
          <h1 style={{ textAlign: 'center', marginBottom: 8, color: '#1a365d' }}>{t('app.title')}</h1>
          <p style={{ textAlign: 'center', marginBottom: 24, color: '#718096' }}>{t('auth.login')}</p>

          {error && (
            <div style={{ background: '#fed7d7', color: '#c53030', padding: 12, borderRadius: 4, marginBottom: 16 }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.email')}</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: 24 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.password')}</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%', padding: 10, background: '#2b6cb0', color: 'white',
                border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600,
                opacity: loading ? 0.7 : 1,
              }}
            >
              {loading ? t('common.cargando') : t('auth.login')}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 16, color: '#718096' }}>
            {t('auth.noAccount')}{' '}
            <Link to="/register" style={{ color: '#2b6cb0' }}>{t('auth.registerHere')}</Link>
          </p>
        </div>
      </div>
    </>
  )
}
