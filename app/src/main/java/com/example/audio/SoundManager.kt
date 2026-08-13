package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager {
    var isMuted: Boolean = false

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playStartGame() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val frequencies = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val noteDurationMs = 60
            
            for (freq in frequencies) {
                playTone(freq, noteDurationMs, sampleRate, volume = 0.4f, type = WaveType.SINE)
            }
        }
    }

    fun playMove() {
        if (isMuted) return
        scope.launch {
            // Calm, soft warm wood tap (360Hz pure sine, low volume, smooth envelope)
            playTone(360.0, 22, 22050, volume = 0.08f, type = WaveType.SINE)
        }
    }

    fun playButtonClick() {
        if (isMuted) return
        scope.launch {
            // Tactile crisp click
            playTone(580.0, 20, 22050, volume = 0.2f, type = WaveType.SINE)
        }
    }

    fun playSettingToggle() {
        if (isMuted) return
        scope.launch {
            // Soft ascending toggle chime
            val sampleRate = 22050
            playTone(480.0, 20, sampleRate, volume = 0.2f, type = WaveType.SINE)
            playTone(680.0, 20, sampleRate, volume = 0.2f, type = WaveType.SINE)
        }
    }

    fun playLaserTick() {
        if (isMuted) return
        scope.launch {
            // Soft 20ms tick
            playTone(1200.0, 20, 22050, volume = 0.25f, type = WaveType.SINE)
        }
    }

    fun playLaserStrike() {
        if (isMuted) return
        scope.launch {
            // Laser pitch glide down
            playGlide(startFreq = 1800.0, endFreq = 350.0, durationMs = 160, sampleRate = 22050, volume = 0.5f)
        }
    }

    fun playVictory() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            val frequencies = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (freq in frequencies) {
                playTone(freq, 90, sampleRate, volume = 0.5f, type = WaveType.SINE)
            }
        }
    }

    fun playEliminated() {
        if (isMuted) return
        scope.launch {
            playTone(180.0, 180, 22050, volume = 0.4f, type = WaveType.SAWTOOTH)
        }
    }

    fun playKeyPickup() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            playTone(987.77, 45, sampleRate, volume = 0.35f, type = WaveType.SINE)
            playTone(1318.51, 65, sampleRate, volume = 0.35f, type = WaveType.SINE)
        }
    }

    fun playKeyEscape() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            playTone(659.25, 40, sampleRate, volume = 0.35f, type = WaveType.SINE)
            playTone(880.0, 40, sampleRate, volume = 0.35f, type = WaveType.SINE)
            playTone(1174.66, 70, sampleRate, volume = 0.35f, type = WaveType.SINE)
        }
    }

    fun playCheesePickup() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            playTone(1046.50, 50, sampleRate, volume = 0.35f, type = WaveType.SINE)
            playTone(1318.51, 50, sampleRate, volume = 0.35f, type = WaveType.SINE)
            playTone(1567.98, 80, sampleRate, volume = 0.35f, type = WaveType.SINE)
        }
    }

    fun playTrapSnap() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            playTone(1200.0, 30, sampleRate, volume = 0.45f, type = WaveType.SAWTOOTH)
            playTone(200.0, 90, sampleRate, volume = 0.45f, type = WaveType.SQUARE)
        }
    }

    fun playCaged() {
        if (isMuted) return
        scope.launch {
            val sampleRate = 22050
            playTone(250.0, 50, sampleRate, volume = 0.4f, type = WaveType.SAWTOOTH)
            playTone(180.0, 100, sampleRate, volume = 0.4f, type = WaveType.SAWTOOTH)
        }
    }

    private enum class WaveType { SINE, SQUARE, SAWTOOTH }

    private fun playTone(
        frequency: Double,
        durationMs: Int,
        sampleRate: Int,
        volume: Float,
        type: WaveType
    ) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val sample = ShortArray(numSamples)

            val attackSamples = (sampleRate * 0.003).toInt().coerceAtMost(numSamples / 2) // 3ms soft attack
            val decaySamples = (sampleRate * 0.008).toInt().coerceAtMost(numSamples / 2)  // 8ms soft decay

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * frequency * time
                
                val rawVal = when (type) {
                    WaveType.SINE -> sin(angle)
                    WaveType.SQUARE -> if (sin(angle) >= 0) 0.6 else -0.6
                    WaveType.SAWTOOTH -> (2.0 * (time * frequency - Math.floor(time * frequency + 0.5)))
                }

                val envelope = when {
                    i < attackSamples -> i.toDouble() / attackSamples
                    i > numSamples - decaySamples -> (numSamples - i).toDouble() / decaySamples
                    else -> 1.0
                }

                val valSample = (rawVal * Short.MAX_VALUE * volume * envelope).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                sample[i] = valSample.toShort()
            }

            val bufferSize = numSamples * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(sample, 0, numSamples)
            audioTrack.play()

            Thread.sleep(durationMs.toLong() + 15)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore potential audio system glitches
        }
    }

    private fun playGlide(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        sampleRate: Int,
        volume: Float
    ) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val sample = ShortArray(numSamples)

            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                phase += 2.0 * Math.PI * currentFreq / sampleRate

                val rawVal = if (sin(phase) >= 0) 0.7 else -0.7
                val fadeEnvelope = (1.0 - progress)
                val valSample = (rawVal * Short.MAX_VALUE * volume * fadeEnvelope).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                sample[i] = valSample.toShort()
            }

            val bufferSize = numSamples * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(sample, 0, numSamples)
            audioTrack.play()

            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio glitches
        }
    }
}
