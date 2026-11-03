package com.cdtomkins.tenortrainer

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Synthesised feedback sounds: a short ding for a hit and a click for a miss. */
class Sounds {
    private val ding = track(generateDing())
    private val click = track(generateClick())

    fun playHit() = play(ding)

    fun playMiss() = play(click)

    fun release() {
        ding.release()
        click.release()
    }

    private fun play(track: AudioTrack) {
        track.stop()
        track.setPlaybackHeadPosition(0)
        track.play()
    }

    private fun track(samples: ShortArray): AudioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .also { it.write(samples, 0, samples.size) }

    private fun generateDing(): ShortArray {
        val n = (SAMPLE_RATE * 0.35).toInt()
        return ShortArray(n) { i ->
            val t = i / SAMPLE_RATE.toDouble()
            val envelope = exp(-t * 9.0)
            val tone = sin(2 * PI * 1318.5 * t) + 0.3 * sin(2 * PI * 2637.0 * t)
            (tone * envelope * AMPLITUDE / 1.3).toInt().toShort()
        }
    }

    private fun generateClick(): ShortArray {
        val n = (SAMPLE_RATE * 0.03).toInt()
        val noise = Random(CLICK_SEED)
        return ShortArray(n) { i ->
            val envelope = exp(-i / (SAMPLE_RATE * 0.004))
            (noise.nextDouble(-1.0, 1.0) * envelope * AMPLITUDE).toInt().toShort()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44100
        const val AMPLITUDE = 0.5 * Short.MAX_VALUE
        const val CLICK_SEED = 1
    }
}
