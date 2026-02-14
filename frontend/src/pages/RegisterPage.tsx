import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { authApi } from '@/api/auth'

export function RegisterPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    email: '',
    password: '',
    nombre: '',
    apellido: '',
    rol: 'OBSTETRA' as const,
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await authApi.register(form)
      navigate('/login')
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } }
      setError(axiosErr.response?.data?.message || t('common.error'))
    } finally {
      setLoading(false)
    }
  }

  const update = (field: string, value: string) => setForm((prev) => ({ ...prev, [field]: value }))

  return (
    <>
      <Helmet><title>{t('auth.register')} - {t('app.title')}</title></Helmet>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f7fafc' }}>
        <div style={{ background: 'white', padding: 32, borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.1)', width: 400 }}>
          <h1 style={{ textAlign: 'center', marginBottom: 8, color: '#1a365d' }}>{t('app.title')}</h1>
          <p style={{ textAlign: 'center', marginBottom: 24, color: '#718096' }}>{t('auth.register')}</p>

          {error && (
            <div style={{ background: '#fed7d7', color: '#c53030', padding: 12, borderRadius: 4, marginBottom: 16 }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.nombre')}</label>
              <input
                type="text" value={form.nombre} onChange={(e) => update('nombre', e.target.value)} required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.apellido')}</label>
              <input
                type="text" value={form.apellido} onChange={(e) => update('apellido', e.target.value)} required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.email')}</label>
              <input
                type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.password')}</label>
              <input
                type="password" value={form.password} onChange={(e) => update('password', e.target.value)} required
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: 24 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('auth.rol')}</label>
              <select
                value={form.rol} onChange={(e) => update('rol', e.target.value)}
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              >
                <option value="OBSTETRA">Obstetra</option>
                <option value="AUDITOR">Auditor</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
            <button
              type="submit" disabled={loading}
              style={{
                width: '100%', padding: 10, background: '#2b6cb0', color: 'white',
                border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600,
                opacity: loading ? 0.7 : 1,
              }}
            >
              {loading ? t('common.cargando') : t('auth.register')}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 16, color: '#718096' }}>
            {t('auth.hasAccount')}{' '}
            <Link to="/login" style={{ color: '#2b6cb0' }}>{t('auth.loginHere')}</Link>
          </p>
        </div>
      </div>
    </>
  )
}
