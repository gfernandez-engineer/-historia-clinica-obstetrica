import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { historiasApi } from '@/api/historias'
import { VoiceRecorder } from '@/components/VoiceRecorder'
import type { SeccionClinica, TipoSeccion, HistoriaClinicaRequest } from '@/types'

const SECCIONES_CLAP: TipoSeccion[] = [
  'DATOS_INGRESO', 'ANTECEDENTES', 'TRABAJO_PARTO', 'PARTO',
  'RECIEN_NACIDO', 'PUERPERIO', 'MEDICAMENTOS', 'EVOLUCION',
]

export function HistoriaFormPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { id } = useParams()
  const isEditing = Boolean(id)

  const [pacienteId, setPacienteId] = useState('')
  const [notasGenerales, setNotasGenerales] = useState('')
  const [secciones, setSecciones] = useState<SeccionClinica[]>(
    SECCIONES_CLAP.map((tipo, i) => ({
      tipo,
      contenido: '',
      origen: 'MANUAL' as const,
      orden: i + 1,
    }))
  )
  const [activeSection, setActiveSection] = useState(0)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (id) {
      historiasApi.obtener(id).then(({ data }) => {
        setPacienteId(data.pacienteId)
        setNotasGenerales(data.notasGenerales || '')
        if (data.secciones.length > 0) {
          setSecciones(data.secciones)
        }
      })
    }
  }, [id])

  const updateSeccion = (index: number, contenido: string) => {
    setSecciones((prev) =>
      prev.map((s, i) => i === index ? { ...s, contenido } : s)
    )
  }

  const handleVoiceResult = (text: string) => {
    setSecciones((prev) =>
      prev.map((s, i) =>
        i === activeSection
          ? { ...s, contenido: s.contenido ? s.contenido + ' ' + text : text, origen: 'VOZ_WEB_SPEECH_API' }
          : s
      )
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    const request: HistoriaClinicaRequest = {
      pacienteId,
      notasGenerales,
      secciones: secciones.filter((s) => s.contenido.trim()),
    }

    try {
      if (isEditing && id) {
        await historiasApi.actualizar(id, request)
      } else {
        await historiasApi.crear(request)
      }
      navigate('/historias')
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } }
      setError(axiosErr.response?.data?.message || t('common.error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Helmet>
        <title>{isEditing ? t('historias.editar') : t('historias.nueva')} - {t('app.title')}</title>
      </Helmet>
      <div style={{ maxWidth: 900, margin: '0 auto' }}>
        <h1 style={{ marginBottom: 24 }}>{isEditing ? t('historias.editar') : t('historias.nueva')}</h1>

        {error && (
          <div style={{ background: '#fed7d7', color: '#c53030', padding: 12, borderRadius: 4, marginBottom: 16 }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {!isEditing && (
            <div style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>
                {t('historias.paciente')} (ID)
              </label>
              <input
                type="text" value={pacienteId} onChange={(e) => setPacienteId(e.target.value)} required
                placeholder="UUID del paciente"
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
          )}

          <div style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
            <VoiceRecorder onResult={handleVoiceResult} />
            <p style={{ fontSize: 12, color: '#718096', marginTop: 8 }}>
              La transcripcion se agregara a la seccion activa: <strong>{t(`secciones.${secciones[activeSection].tipo}`)}</strong>
            </p>
          </div>

          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
            {secciones.map((s, i) => (
              <button
                key={s.tipo} type="button"
                onClick={() => setActiveSection(i)}
                style={{
                  padding: '6px 12px', borderRadius: 4, border: 'none', cursor: 'pointer',
                  background: activeSection === i ? '#2b6cb0' : '#e2e8f0',
                  color: activeSection === i ? 'white' : '#4a5568',
                  fontWeight: activeSection === i ? 600 : 400,
                }}
              >
                {t(`secciones.${s.tipo}`)}
              </button>
            ))}
          </div>

          {secciones.map((s, i) => (
            <div key={s.tipo}
              style={{
                display: activeSection === i ? 'block' : 'none',
                background: 'white', padding: 16, borderRadius: 8, marginBottom: 16,
                boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
              }}>
              <label style={{ display: 'block', marginBottom: 8, fontWeight: 600 }}>
                {t(`secciones.${s.tipo}`)}
                {s.origen !== 'MANUAL' && (
                  <span style={{ fontSize: 11, color: '#38a169', marginLeft: 8 }}>({s.origen})</span>
                )}
              </label>
              <textarea
                value={s.contenido}
                onChange={(e) => updateSeccion(i, e.target.value)}
                rows={6}
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, resize: 'vertical', boxSizing: 'border-box' }}
              />
            </div>
          ))}

          <div style={{ background: 'white', padding: 16, borderRadius: 8, marginBottom: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{t('historias.notasGenerales')}</label>
            <textarea
              value={notasGenerales} onChange={(e) => setNotasGenerales(e.target.value)}
              rows={3}
              style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, resize: 'vertical', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ display: 'flex', gap: 12 }}>
            <button type="submit" disabled={loading}
              style={{ flex: 1, padding: 12, background: '#2b6cb0', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}>
              {loading ? t('common.cargando') : t('historias.guardar')}
            </button>
            <button type="button" onClick={() => navigate('/historias')}
              style={{ flex: 1, padding: 12, background: '#e2e8f0', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}>
              {t('common.cancelar')}
            </button>
          </div>
        </form>
      </div>
    </>
  )
}
