import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useSpeechRecognition } from '@/hooks/useSpeechRecognition'

describe('useSpeechRecognition', () => {
  it('debe detectar que Web Speech API no esta soportada en jsdom', () => {
    const { result } = renderHook(() => useSpeechRecognition())
    expect(result.current.isSupported).toBe(false)
  })

  it('debe iniciar sin grabar', () => {
    const { result } = renderHook(() => useSpeechRecognition())
    expect(result.current.isRecording).toBe(false)
    expect(result.current.transcript).toBe('')
    expect(result.current.interimTranscript).toBe('')
  })

  it('startRecording no debe fallar si API no soportada', () => {
    const { result } = renderHook(() => useSpeechRecognition())
    act(() => {
      result.current.startRecording()
    })
    expect(result.current.isRecording).toBe(false)
  })

  it('stopRecording no debe fallar si no hay grabacion activa', () => {
    const { result } = renderHook(() => useSpeechRecognition())
    act(() => {
      result.current.stopRecording()
    })
    expect(result.current.isRecording).toBe(false)
  })
})
