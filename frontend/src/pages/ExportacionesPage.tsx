import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { exportacionesApi } from '@/api/exportaciones'
import type { ExportJob } from '@/types'

const estadoColors: Record<string, string> = {
  PENDIENTE: '#d69e2e',
  PROCESANDO: '#3182ce',
  COMPLETADO: '#38a169',
  ERROR: '#e53e3e',
}

export function ExportacionesPage() {
  const { t } = useTranslation()
  const [exports, setExports] = useState<ExportJob[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadExports()
  }, [])

  const loadExports = async () => {
    try {
      const { data } = await exportacionesApi.listar({ page: 0, size: 20 })
      setExports(data.content)
    } catch {
      setExports([])
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async (exportJob: ExportJob) => {
    try {
      const { data } = await exportacionesApi.descargar(exportJob.id)
      const url = window.URL.createObjectURL(new Blob([data], { type: 'application/pdf' }))
      const link = document.createElement('a')
      link.href = url
      link.download = `historia-clinica-${exportJob.historiaClinicaId.slice(0, 8)}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    } catch {
      alert(t('common.error'))
    }
  }

  return (
    <>
      <Helmet><title>{t('exportaciones.title')} - {t('app.title')}</title></Helmet>
      <div>
        <h1 style={{ marginBottom: 24 }}>{t('exportaciones.title')}</h1>

        {loading ? (
          <p>{t('common.cargando')}</p>
        ) : exports.length === 0 ? (
          <p style={{ color: '#718096' }}>{t('common.sinResultados')}</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: 8 }}>
            <thead>
              <tr style={{ background: '#edf2f7' }}>
                <th style={thStyle}>Historia</th>
                <th style={thStyle}>{t('exportaciones.estado')}</th>
                <th style={thStyle}>{t('exportaciones.fecha')}</th>
                <th style={thStyle}>{t('historias.acciones')}</th>
              </tr>
            </thead>
            <tbody>
              {exports.map((e) => (
                <tr key={e.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                  <td style={tdStyle}>{e.historiaClinicaId.slice(0, 8)}...</td>
                  <td style={tdStyle}>
                    <span style={{
                      background: estadoColors[e.estado] || '#718096',
                      color: 'white', padding: '2px 8px', borderRadius: 12, fontSize: 12,
                    }}>
                      {e.estado}
                    </span>
                  </td>
                  <td style={tdStyle}>{new Date(e.createdAt).toLocaleString()}</td>
                  <td style={tdStyle}>
                    {e.estado === 'COMPLETADO' && (
                      <button onClick={() => handleDownload(e)}
                        style={{ padding: '4px 12px', background: '#2b6cb0', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
                        {t('exportaciones.descargar')}
                      </button>
                    )}
                    {e.estado === 'ERROR' && (
                      <span style={{ color: '#e53e3e', fontSize: 12 }}>{e.errorMensaje}</span>
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
