import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { historiasApi } from '@/api/historias'
import type { HistoriaClinica } from '@/types'

const estadoColors: Record<string, string> = {
  BORRADOR: '#d69e2e',
  EN_REVISION: '#3182ce',
  FINALIZADA: '#38a169',
  ANULADA: '#e53e3e',
}

export function HistoriasPage() {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const [historias, setHistorias] = useState<HistoriaClinica[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadHistorias()
  }, [searchParams])

  const loadHistorias = async () => {
    setLoading(true)
    try {
      const { data } = await historiasApi.listar({ page: 0, size: 20 })
      const pacienteId = searchParams.get('pacienteId')
      const content = pacienteId
        ? data.content.filter((h) => h.pacienteId === pacienteId)
        : data.content
      setHistorias(content)
    } catch {
      setHistorias([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Helmet><title>{t('historias.title')} - {t('app.title')}</title></Helmet>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h1>{t('historias.title')}</h1>
          <Link
            to="/historias/nueva"
            style={{ background: '#38a169', color: 'white', padding: '8px 16px', borderRadius: 4, textDecoration: 'none' }}
          >
            {t('historias.nueva')}
          </Link>
        </div>

        {loading ? (
          <p>{t('common.cargando')}</p>
        ) : historias.length === 0 ? (
          <p style={{ color: '#718096' }}>{t('common.sinResultados')}</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: 8 }}>
            <thead>
              <tr style={{ background: '#edf2f7' }}>
                <th style={thStyle}>ID</th>
                <th style={thStyle}>{t('historias.version')}</th>
                <th style={thStyle}>{t('historias.estado')}</th>
                <th style={thStyle}>{t('historias.fecha')}</th>
                <th style={thStyle}>{t('historias.acciones')}</th>
              </tr>
            </thead>
            <tbody>
              {historias.map((h) => (
                <tr key={h.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                  <td style={tdStyle}>{h.id.slice(0, 8)}...</td>
                  <td style={tdStyle}>v{h.version}</td>
                  <td style={tdStyle}>
                    <span style={{
                      background: estadoColors[h.estado] || '#718096',
                      color: 'white',
                      padding: '2px 8px',
                      borderRadius: 12,
                      fontSize: 12,
                    }}>
                      {h.estado}
                    </span>
                  </td>
                  <td style={tdStyle}>{new Date(h.createdAt).toLocaleDateString()}</td>
                  <td style={tdStyle}>
                    <Link to={`/historias/${h.id}`} style={{ color: '#2b6cb0', marginRight: 12 }}>
                      {t('historias.ver')}
                    </Link>
                    {h.estado === 'BORRADOR' && (
                      <Link to={`/historias/${h.id}/editar`} style={{ color: '#38a169' }}>
                        {t('historias.editar')}
                      </Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

const thStyle: React.CSSProperties = { padding: '10px 12px', textAlign: 'left', fontWeight: 600, fontSize: 14 }
const tdStyle: React.CSSProperties = { padding: '10px 12px', fontSize: 14 }
