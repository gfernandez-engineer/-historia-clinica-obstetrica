import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { useAuthStore } from '@/stores/authStore'

export function Layout() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const toggleLanguage = () => {
    const newLang = i18n.language === 'es' ? 'en' : 'es'
    i18n.changeLanguage(newLang)
    localStorage.setItem('language', newLang)
  }

  return (
    <>
      <Helmet>
        <title>{t('app.title')}</title>
      </Helmet>
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <header style={{
          background: '#1a365d',
          color: 'white',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          height: 56,
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
            <Link to="/" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold', fontSize: 18 }}>
              {t('app.title')}
            </Link>
            <nav style={{ display: 'flex', gap: 16 }}>
              <Link to="/pacientes" style={{ color: '#bee3f8', textDecoration: 'none' }}>
                {t('nav.pacientes')}
              </Link>
              <Link to="/historias" style={{ color: '#bee3f8', textDecoration: 'none' }}>
                {t('nav.historias')}
              </Link>
              <Link to="/exportaciones" style={{ color: '#bee3f8', textDecoration: 'none' }}>
                {t('nav.exportaciones')}
              </Link>
            </nav>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <button
              onClick={toggleLanguage}
              style={{ background: 'transparent', border: '1px solid #bee3f8', color: '#bee3f8', padding: '4px 8px', borderRadius: 4, cursor: 'pointer' }}
            >
              {i18n.language === 'es' ? 'EN' : 'ES'}
            </button>
            <span style={{ color: '#bee3f8', fontSize: 14 }}>{user?.email}</span>
            <button
              onClick={handleLogout}
              style={{ background: '#e53e3e', color: 'white', border: 'none', padding: '6px 12px', borderRadius: 4, cursor: 'pointer' }}
            >
              {t('nav.logout')}
            </button>
          </div>
        </header>
        <main style={{ flex: 1, padding: 24, background: '#f7fafc' }}>
          <Outlet />
        </main>
      </div>
    </>
  )
}
