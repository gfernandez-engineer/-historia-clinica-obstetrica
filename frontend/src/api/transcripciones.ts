import apiClient from './client'

interface TranscripcionResponse {
  id: string
  historiaClinicaId: string
  textoOriginal: string
  textoNormalizado: string
  estado: string
  terminosMedicos: { termino: string; codigoCie10: string; descripcion: string }[]
  createdAt: string
}

export const transcripcionesApi = {
  procesarTexto: (data: { historiaClinicaId: string; texto: string; origen: string }) =>
    apiClient.post<TranscripcionResponse>('/transcripciones/texto', data),

  listarPorHistoria: (historiaClinicaId: string) =>
    apiClient.get<TranscripcionResponse[]>(`/transcripciones/historia/${historiaClinicaId}`),
}
