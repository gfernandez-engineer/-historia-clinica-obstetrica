import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { Link } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function DashboardPage() {
  const { t } = useTranslation()
  const user = useAuthStore((s) => s.user)

  return (
    <>
      <Helmet><title>{t('nav.dashboard')} - {t('app.title')}</title></Helmet>
      <div>
        <h1 style={{ marginBottom: 8 }}>{t('app.title')}</h1>
        <p style={{ color: '#718096', marginBottom: 32 }}>
          {t('app.subtitle')} — {user?.email} ({user?.rol})
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: 16 }}>
          <Link to="/pacientes" style={{ textDecoration: 'none' }}>
            <div style={{ background: 'white', padding: 24, borderRadius: 8, boxShadow: '0 1px 4px rgba(0,0,0,0.1)', borderLeft: '4px solid #2b6cb0' }}>
              <h3 style={{ color: '#1a365d', marginBottom: 8 }}>{t('nav.pacientes')}</h3>
              <p style={{ color: '#718096' }}>Gestionar pacientes registrados</p>
            </div>
          </Link>
          <Link to="/historias" style={{ textDecoration: 'none' }}>
            <div style={{ background: 'white', padding: 24, borderRadius: 8, boxShadow: '0 1px 4px rgba(0,0,0,0.1)', borderLeft: '4px solid #38a169' }}>
              <h3 style={{ color: '#1a365d', marginBottom: 8 }}>{t('nav.historias')}</h3>
              <p style={{ color: '#718096' }}>Historias clinicas con transcripcion por voz</p>
            </div>
          </Link>
          <Link to="/exportaciones" style={{ textDecoration: 'none' }}>
            <div style={{ background: 'white', padding: 24, borderRadius: 8, boxShadow: '0 1px 4px rgba(0,0,0,0.1)', borderLeft: '4px solid #d69e2e' }}>
              <h3 style={{ color: '#1a365d', marginBottom: 8 }}>{t('nav.exportaciones')}</h3>
              <p style={{ color: '#718096' }}>Exportar historias clinicas a PDF</p>
            </div>
          </Link>
        </div>
      </div>
    </>
  )
}
