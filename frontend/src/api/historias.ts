import apiClient from './client'
import type { HistoriaClinica, HistoriaClinicaRequest, Page } from '@/types'

export const historiasApi = {
  listar: (params?: { page?: number; size?: number }) =>
    apiClient.get<Page<HistoriaClinica>>('/historias-clinicas', { params }),

  obtener: (id: string) =>
    apiClient.get<HistoriaClinica>(`/historias-clinicas/${id}`),

  crear: (data: HistoriaClinicaRequest) =>
    apiClient.post<HistoriaClinica>('/historias-clinicas', data),

  actualizar: (id: string, data: HistoriaClinicaRequest) =>
    apiClient.put<HistoriaClinica>(`/historias-clinicas/${id}`, data),

  finalizar: (id: string) =>
    apiClient.patch<HistoriaClinica>(`/historias-clinicas/${id}/finalizar`),

  revision: (id: string) =>
    apiClient.patch<HistoriaClinica>(`/historias-clinicas/${id}/revision`),

  anular: (id: string) =>
    apiClient.patch<HistoriaClinica>(`/historias-clinicas/${id}/anular`),
}
