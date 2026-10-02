package com.nfcwallet.app.nfc.utils

import android.media.AudioManager
import android.media.ToneGenerator

// Native Android sound effects generator for NFC card detection and success feedback
object SoundEffectsManager {

    // Plays short, crisp beep when an NFC card is scanned by the phone
    fun playCardReadBeep() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
        } catch (e: Exception) {
            // Ignore if audio stream is unavailable
        }
    }

    // Plays double-beep acknowledgment tone when a card is saved
    fun playSuccessBeep() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 220)
        } catch (e: Exception) {
            // Ignore if audio stream is unavailable
        }
    }
}
