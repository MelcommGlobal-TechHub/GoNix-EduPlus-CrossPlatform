import { registerPlugin } from '@capacitor/core'

export type GeminiNanoStatus =
  | 'AVAILABLE'
  | 'DOWNLOADABLE'
  | 'DOWNLOADING'
  | 'UNAVAILABLE'

export interface GeminiNanoStatusResult {
  status: GeminiNanoStatus
  source: 'ON_DEVICE'
}

export interface GeminiNanoDownloadResult {
  status: GeminiNanoStatus
  progress: number
}

export interface GeminiNanoInferenceResult {
  text: string
  source: 'ON_DEVICE'
}

export interface GeminiNanoDownloadProgress {
  phase: 'started' | 'downloading' | 'completed' | 'failed'
  totalBytes?: number
  downloadedBytes?: number
  message?: string
}

interface GeminiNanoPlugin {
  getStatus(): Promise<GeminiNanoStatusResult>
  downloadModel(): Promise<GeminiNanoDownloadResult>
  generate(options: { prompt: string }): Promise<GeminiNanoInferenceResult>
  addListener(
    eventName: 'downloadProgress',
    listener: (event: GeminiNanoDownloadProgress) => void,
  ): Promise<{ remove: () => Promise<void> }>
}

export const GeminiNano = registerPlugin<GeminiNanoPlugin>('GeminiNano')
