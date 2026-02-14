import { useTranslation } from 'react-i18next'
import { useSpeechRecognition } from '@/hooks/useSpeechRecognition'

interface Props {
  onResult: (text: string) => void
}

export function VoiceRecorder({ onResult }: Props) {
  const { t } = useTranslation()
  const { isRecording, interimTranscript, isSupported, startRecording, stopRecording } =
    useSpeechRecognition(onResult)

  if (!isSupported) {
    return (
      <div style={{ padding: 12, background: '#fed7d7', borderRadius: 4, color: '#c53030' }}>
        {t('voz.noSoportado')}
      </div>
    )
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {isRecording ? (
          <button
            type="button"
            onClick={stopRecording}
            style={{
              padding: '10px 20px',
              background: '#e53e3e',
              color: 'white',
              border: 'none',
              borderRadius: 4,
              cursor: 'pointer',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: 8,
            }}
          >
            <span style={{
              width: 12, height: 12, borderRadius: '50%',
              background: 'white', display: 'inline-block',
              animation: 'pulse 1s infinite',
            }} />
            {t('voz.detener')}
          </button>
        ) : (
          <button
            type="button"
            onClick={startRecording}
            style={{
              padding: '10px 20px',
              background: '#38a169',
              color: 'white',
              border: 'none',
              borderRadius: 4,
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            {t('voz.grabar')}
          </button>
        )}

        {isRecording && (
          <span style={{ color: '#e53e3e', fontWeight: 600, fontSize: 14 }}>
            {t('voz.grabando')}
          </span>
        )}
      </div>

      {interimTranscript && (
        <div style={{
          marginTop: 8, padding: 8, background: '#f0fff4',
          border: '1px dashed #38a169', borderRadius: 4,
          color: '#276749', fontSize: 14, fontStyle: 'italic',
        }}>
          {interimTranscript}
        </div>
      )}

      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.3; }
        }
      `}</style>
    </div>
  )
}
