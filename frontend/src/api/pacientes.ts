import apiClient from './client'
import type { Paciente, PacienteRequest, Page } from '@/types'

export const pacientesApi = {
  listar: (params?: { search?: string; page?: number; size?: number }) =>
    apiClient.get<Page<Paciente>>('/pacientes', { params }),

  obtener: (id: string) =>
    apiClient.get<Paciente>(`/pacientes/${id}`),

  crear: (data: PacienteRequest) =>
    apiClient.post<Paciente>('/pacientes', data),

  actualizar: (id: string, data: PacienteRequest) =>
    apiClient.put<Paciente>(`/pacientes/${id}`, data),
}
