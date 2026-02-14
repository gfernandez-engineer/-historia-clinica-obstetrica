export interface User {
  id: string
  email: string
  nombre: string
  apellido: string
  rol: 'OBSTETRA' | 'AUDITOR' | 'ADMIN'
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  nombre: string
  apellido: string
  rol: 'OBSTETRA' | 'AUDITOR' | 'ADMIN'
}

export interface Paciente {
  id: string
  dni: string
  nombre: string
  apellido: string
  fechaNacimiento: string
  telefono: string
  direccion: string
  createdAt: string
  updatedAt: string
}

export interface PacienteRequest {
  dni: string
  nombre: string
  apellido: string
  fechaNacimiento: string
  telefono?: string
  direccion?: string
}

export type EstadoHistoria = 'BORRADOR' | 'EN_REVISION' | 'FINALIZADA' | 'ANULADA'
export type TipoSeccion = 'DATOS_INGRESO' | 'ANTECEDENTES' | 'TRABAJO_PARTO' | 'PARTO' | 'RECIEN_NACIDO' | 'PUERPERIO' | 'MEDICAMENTOS' | 'EVOLUCION'

export interface SeccionClinica {
  id?: string
  tipo: TipoSeccion
  contenido: string
  origen: 'MANUAL' | 'VOZ_WEB_SPEECH_API' | 'VOZ_GOOGLE_SPEECH_TO_TEXT'
  orden: number
}

export interface EventoObstetrico {
  id?: string
  tipo: string
  fecha: string
  semanaGestacional?: number
  observaciones?: string
}

export interface Medicamento {
  id?: string
  nombre: string
  dosis: string
  via: string
  frecuencia: string
  duracion: string
}

export interface HistoriaClinica {
  id: string
  pacienteId: string
  version: number
  estado: EstadoHistoria
  notasGenerales: string
  secciones: SeccionClinica[]
  eventos: EventoObstetrico[]
  medicamentos: Medicamento[]
  createdAt: string
  updatedAt: string
}

export interface HistoriaClinicaRequest {
  pacienteId: string
  notasGenerales?: string
  secciones?: SeccionClinica[]
  eventos?: EventoObstetrico[]
  medicamentos?: Medicamento[]
}

export interface ExportJob {
  id: string
  historiaClinicaId: string
  formato: 'PDF'
  estado: 'PENDIENTE' | 'PROCESANDO' | 'COMPLETADO' | 'ERROR'
  archivoUrl?: string
  errorMensaje?: string
  createdAt: string
  completedAt?: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}
