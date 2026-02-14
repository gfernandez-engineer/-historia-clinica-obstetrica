import apiClient from './client'
import type { ExportJob, Page } from '@/types'

export const exportacionesApi = {
  generar: (historiaClinicaId: string) =>
    apiClient.post<ExportJob>('/exportaciones', { historiaClinicaId, formato: 'PDF' }),

  obtener: (id: string) =>
    apiClient.get<ExportJob>(`/exportaciones/${id}`),

  listar: (params?: { page?: number; size?: number }) =>
    apiClient.get<Page<ExportJob>>('/exportaciones', { params }),

  descargar: (id: string) =>
    apiClient.get<Blob>(`/exportaciones/${id}/descargar`, { responseType: 'blob' }),
}
