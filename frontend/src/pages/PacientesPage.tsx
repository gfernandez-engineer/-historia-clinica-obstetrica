import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Helmet } from 'react-helmet-async'
import { pacientesApi } from '@/api/pacientes'
import type { Paciente } from '@/types'

export function PacientesPage() {
  const { t } = useTranslation()
  const [pacientes, setPacientes] = useState<Paciente[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadPacientes()
  }, [search])

  const loadPacientes = async () => {
    setLoading(true)
    try {
      const { data } = await pacientesApi.listar({ search: search || undefined, page: 0, size: 20 })
      setPacientes(data.content)
    } catch {
      setPacientes([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Helmet><title>{t('pacientes.title')} - {t('app.title')}</title></Helmet>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h1>{t('pacientes.title')}</h1>
          <Link
            to="/pacientes/nuevo"
            style={{ background: '#2b6cb0', color: 'white', padding: '8px 16px', borderRadius: 4, textDecoration: 'none' }}
          >
            {t('pacientes.nuevo')}
          </Link>
        </div>

        <input
          type="text"
          placeholder={t('pacientes.buscar')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ width: '100%', padding: 10, border: '1px solid #e2e8f0', borderRadius: 4, marginBottom: 16, boxSizing: 'border-box' }}
        />

        {loading ? (
          <p>{t('common.cargando')}</p>
        ) : pacientes.length === 0 ? (
          <p style={{ color: '#718096' }}>{t('common.sinResultados')}</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: 8 }}>
            <thead>
              <tr style={{ background: '#edf2f7' }}>
                <th style={thStyle}>{t('pacientes.dni')}</th>
                <th style={thStyle}>{t('pacientes.nombre')}</th>
                <th style={thStyle}>{t('pacientes.apellido')}</th>
                <th style={thStyle}>{t('pacientes.telefono')}</th>
                <th style={thStyle}>{t('pacientes.acciones')}</th>
              </tr>
            </thead>
            <tbody>
              {pacientes.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                  <td style={tdStyle}>{p.dni}</td>
                  <td style={tdStyle}>{p.nombre}</td>
                  <td style={tdStyle}>{p.apellido}</td>
                  <td style={tdStyle}>{p.telefono}</td>
                  <td style={tdStyle}>
                    <Link to={`/pacientes/${p.id}`} style={{ color: '#2b6cb0', marginRight: 12 }}>
                      {t('pacientes.editar')}
                    </Link>
                    <Link to={`/historias?pacienteId=${p.id}`} style={{ color: '#38a169' }}>
                      {t('pacientes.verHistorias')}
                    </Link>
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
