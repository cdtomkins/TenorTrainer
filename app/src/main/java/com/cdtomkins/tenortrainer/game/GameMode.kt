package com.cdtomkins.tenortrainer.game

/**
 * A playable game mode. To add a mode, implement this interface and register it in [ModeRegistry].
 */
interface GameMode {
    /** Stable identifier used to persist high scores. Don't change it once released. */
    val id: String
    val title: String
    val description: String

    /** True when a lower score ranks higher, e.g. time taken. */
    val lowerIsBetter: Boolean

    fun newSession(): GameSession
}

/** One run of a [GameMode]. The UI drives it through [onTap] and reads its state. */
interface GameSession {
    /** What the player is asked to do right now, e.g. "Tap C#". */
    val prompt: String

    /** Progress text, e.g. "3 / 30". */
    val progress: String
    val isFinished: Boolean

    /** Wrong taps so far. */
    val misses: Int

    /** Elapsed time in milliseconds at [nowMs]; zero before the first tap. */
    fun elapsedMs(nowMs: Long): Long

    fun onTap(x: Float, y: Float, nowMs: Long): TapResult

    /** Result once [isFinished] is true, otherwise null. */
    fun result(): RunResult?
}

enum class TapResult { HIT, MISS }

data class RunResult(
    val score: Double,
    val elapsedMs: Long,
    val misses: Int,
    val targets: Int,
)
