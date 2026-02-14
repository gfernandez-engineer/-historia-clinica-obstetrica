import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { pacientesApi } from '@/api/pacientes'
import type { PacienteRequest } from '@/types'

export function PacienteFormPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { id } = useParams()
  const isEditing = Boolean(id)

  const [form, setForm] = useState<PacienteRequest>({
    dni: '', nombre: '', apellido: '', fechaNacimiento: '', telefono: '', direccion: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (id) {
      pacientesApi.obtener(id).then(({ data }) => {
        setForm({
          dni: data.dni,
          nombre: data.nombre,
          apellido: data.apellido,
          fechaNacimiento: data.fechaNacimiento,
          telefono: data.telefono || '',
          direccion: data.direccion || '',
        })
      })
    }
  }, [id])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (isEditing && id) {
        await pacientesApi.actualizar(id, form)
      } else {
        await pacientesApi.crear(form)
      }
      navigate('/pacientes')
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
      <Helmet><title>{isEditing ? t('pacientes.editar') : t('pacientes.nuevo')} - {t('app.title')}</title></Helmet>
      <div style={{ maxWidth: 600, margin: '0 auto' }}>
        <h1 style={{ marginBottom: 24 }}>{isEditing ? t('pacientes.editar') : t('pacientes.nuevo')}</h1>

        {error && (
          <div style={{ background: '#fed7d7', color: '#c53030', padding: 12, borderRadius: 4, marginBottom: 16 }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ background: 'white', padding: 24, borderRadius: 8, boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
          {[
            { field: 'dni', label: t('pacientes.dni'), type: 'text' },
            { field: 'nombre', label: t('pacientes.nombre'), type: 'text' },
            { field: 'apellido', label: t('pacientes.apellido'), type: 'text' },
            { field: 'fechaNacimiento', label: t('pacientes.fechaNacimiento'), type: 'date' },
            { field: 'telefono', label: t('pacientes.telefono'), type: 'text' },
            { field: 'direccion', label: t('pacientes.direccion'), type: 'text' },
          ].map(({ field, label, type }) => (
            <div key={field} style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>{label}</label>
              <input
                type={type}
                value={form[field as keyof PacienteRequest] || ''}
                onChange={(e) => update(field, e.target.value)}
                required={['dni', 'nombre', 'apellido', 'fechaNacimiento'].includes(field)}
                style={{ width: '100%', padding: 8, border: '1px solid #e2e8f0', borderRadius: 4, boxSizing: 'border-box' }}
              />
            </div>
          ))}

          <div style={{ display: 'flex', gap: 12 }}>
            <button type="submit" disabled={loading}
              style={{ flex: 1, padding: 10, background: '#2b6cb0', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}>
              {loading ? t('common.cargando') : t('pacientes.guardar')}
            </button>
            <button type="button" onClick={() => navigate('/pacientes')}
              style={{ flex: 1, padding: 10, background: '#e2e8f0', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}>
              {t('pacientes.cancelar')}
            </button>
          </div>
        </form>
      </div>
    </>
  )
}
