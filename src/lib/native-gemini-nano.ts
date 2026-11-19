import { registerPlugin } from '@capacitor/core'

export type GeminiNanoModelChoice =
  | 'NANO_4_FULL_PREVIEW'
  | 'NANO_4_FAST_PREVIEW'
  | 'STABLE'

export const DEFAULT_GEMINI_NANO_MODEL: GeminiNanoModelChoice =
  'NANO_4_FULL_PREVIEW'

export type GeminiNanoStatus =
  | 'AVAILABLE'
  | 'DOWNLOADABLE'
  | 'DOWNLOADING'
  | 'UNAVAILABLE'

export interface GeminiNanoStatusResult {
  status: GeminiNanoStatus
  source: 'ON_DEVICE'
  modelChoice?: GeminiNanoModelChoice
  modelName?: string
}

export interface GeminiNanoDownloadResult {
  status: GeminiNanoStatus
  progress: number
}

export interface GeminiNanoInferenceResult {
  text: string
  source: 'ON_DEVICE'
  modelChoice?: GeminiNanoModelChoice
}

export interface GeminiNanoDownloadProgress {
  phase: 'started' | 'downloading' | 'completed' | 'failed'
  totalBytes?: number
  downloadedBytes?: number
  message?: string
}

interface GeminiNanoPlugin {
  getStatus(options?: { modelChoice?: GeminiNanoModelChoice }): Promise<GeminiNanoStatusResult>
  downloadModel(options?: { modelChoice?: GeminiNanoModelChoice }): Promise<GeminiNanoDownloadResult>
  generate(options: {
    prompt: string
    modelChoice?: GeminiNanoModelChoice
  }): Promise<GeminiNanoInferenceResult>
  addListener(
    eventName: 'downloadProgress',
    listener: (event: GeminiNanoDownloadProgress) => void,
  ): Promise<{ remove: () => Promise<void> }>
}

export const GeminiNano = registerPlugin<GeminiNanoPlugin>('GeminiNano')
