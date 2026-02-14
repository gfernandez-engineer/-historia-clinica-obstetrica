import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { historiasApi } from '@/api/historias'
import { exportacionesApi } from '@/api/exportaciones'
import type { HistoriaClinica } from '@/types'

export function HistoriaDetallePage() {
  const { t } = useTranslation()
  const { id } = useParams()
  const navigate = useNavigate()
  const [historia, setHistoria] = useState<HistoriaClinica | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)

  useEffect(() => {
    if (id) {
      historiasApi.obtener(id).then(({ data }) => {
        setHistoria(data)
        setLoading(false)
      }).catch(() => setLoading(false))
    }
  }, [id])

  const handleAction = async (action: 'finalizar' | 'revision' | 'anular') => {
    if (!id) return
    setActionLoading(true)
    try {
      const { data } = await historiasApi[action](id)
      setHistoria(data)
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } }
      alert(axiosErr.response?.data?.message || t('common.error'))
    } finally {
      setActionLoading(false)
    }
  }

  const handleExport = async () => {
    if (!id) return
    setActionLoading(true)
    try {
      const { data } = await exportacionesApi.generar(id)
      navigate(`/exportaciones`)
      console.log('Export job created:', data.id)
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } }
      alert(axiosErr.response?.data?.message || t('common.error'))
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) return <p>{t('common.cargando')}</p>
  if (!historia) return <p>{t('common.error')}</p>

  const isBorrador = historia.estado === 'BORRADOR'
  const isEnRevision = historia.estado === 'EN_REVISION'

  return (
    <>
      <Helmet><title>Historia v{historia.version} - {t('app.title')}</title></Helmet>
      <div style={{ maxWidth: 900, margin: '0 auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <div>
            <h1 style={{ marginBottom: 4 }}>Historia Clinica v{historia.version}</h1>
            <span style={{
              background: historia.estado === 'FINALIZADA' ? '#38a169' : historia.estado === 'BORRADOR' ? '#d69e2e' : '#3182ce',
              color: 'white', padding: '2px 8px', borderRadius: 12, fontSize: 12,
            }}>
              {historia.estado}
            </span>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {isBorrador && (
              <>
                <Link to={`/historias/${id}/editar`}
                  style={{ padding: '8px 16px', background: '#2b6cb0', color: 'white', borderRadius: 4, textDecoration: 'none' }}>
                  {t('historias.editar')}
                </Link>
                <button onClick={() => handleAction('revision')} disabled={actionLoading}
                  style={{ padding: '8px 16px', background: '#3182ce', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
                  {t('historias.revision')}
                </button>
              </>
            )}
            {isEnRevision && (
              <button onClick={() => handleAction('finalizar')} disabled={actionLoading}
                style={{ padding: '8px 16px', background: '#38a169', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
                {t('historias.finalizar')}
              </button>
            )}
            {(isBorrador || isEnRevision) && (
              <button onClick={() => handleAction('anular')} disabled={actionLoading}
                style={{ padding: '8px 16px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
                {t('historias.anular')}
              </button>
            )}
            <button onClick={handleExport} disabled={actionLoading}
              style={{ padding: '8px 16px', background: '#d69e2e', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
              {t('historias.exportar')}
            </button>
          </div>
        </div>

        {historia.secciones
          .sort((a, b) => a.orden - b.orden)
          .map((s) => (
            <div key={s.tipo} style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
              <h3 style={{ color: '#1a365d', marginBottom: 8, borderBottom: '2px solid #e2e8f0', paddingBottom: 4 }}>
                {t(`secciones.${s.tipo}`)}
              </h3>
              <p style={{ whiteSpace: 'pre-wrap' }}>{s.contenido || <em style={{ color: '#a0aec0' }}>Sin contenido</em>}</p>
            </div>
          ))}

        {historia.eventos.length > 0 && (
          <div style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
            <h3 style={{ color: '#1a365d', marginBottom: 8 }}>Eventos Obstetricos</h3>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#edf2f7' }}>
                  <th style={{ padding: 8, textAlign: 'left' }}>Tipo</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Fecha</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Semana</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Observaciones</th>
                </tr>
              </thead>
              <tbody>
                {historia.eventos.map((e, i) => (
                  <tr key={i} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: 8 }}>{e.tipo}</td>
                    <td style={{ padding: 8 }}>{e.fecha ? new Date(e.fecha).toLocaleDateString() : '-'}</td>
                    <td style={{ padding: 8 }}>{e.semanaGestacional ?? '-'}</td>
                    <td style={{ padding: 8 }}>{e.observaciones}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {historia.medicamentos.length > 0 && (
          <div style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
            <h3 style={{ color: '#1a365d', marginBottom: 8 }}>Medicamentos</h3>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#edf2f7' }}>
                  <th style={{ padding: 8, textAlign: 'left' }}>Nombre</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Dosis</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Via</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Frecuencia</th>
                  <th style={{ padding: 8, textAlign: 'left' }}>Duracion</th>
                </tr>
              </thead>
              <tbody>
                {historia.medicamentos.map((m, i) => (
                  <tr key={i} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: 8 }}>{m.nombre}</td>
                    <td style={{ padding: 8 }}>{m.dosis}</td>
                    <td style={{ padding: 8 }}>{m.via}</td>
                    <td style={{ padding: 8 }}>{m.frecuencia}</td>
                    <td style={{ padding: 8 }}>{m.duracion}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {historia.notasGenerales && (
          <div style={{ background: '#fffff0', padding: 16, borderRadius: 8, border: '1px solid #ecc94b' }}>
            <h3 style={{ marginBottom: 8 }}>{t('historias.notasGenerales')}</h3>
            <p style={{ whiteSpace: 'pre-wrap' }}>{historia.notasGenerales}</p>
          </div>
        )}

        <div style={{ marginTop: 24 }}>
          <button onClick={() => navigate('/historias')}
            style={{ padding: '8px 16px', background: '#e2e8f0', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
            {t('common.volver')}
          </button>
        </div>
      </div>
    </>
  )
}
