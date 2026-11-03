package com.cdtomkins.tenortrainer.game

import kotlin.random.Random

/**
 * Tenor Time Attack: tap [notesPerRound] random notes as fast as possible.
 * The score is the elapsed time in seconds plus [penaltySeconds] for each wrong tap.
 */
class TimeAttackMode(
    private val notes: List<PanNote>,
    private val notesPerRound: Int,
    private val penaltySeconds: Double,
    private val random: Random = Random.Default,
) : GameMode {
    override val id = "time_attack"
    override val title = "Tenor Time Attack"
    override val description =
        "Tap $notesPerRound random notes as fast as you can. Each wrong tap adds a ${penaltySeconds.toInt()} s penalty."
    override val lowerIsBetter = true

    override fun newSession(): GameSession =
        TimeAttackSession(notes, notesPerRound, penaltySeconds, random)
}

class TimeAttackSession(
    private val notes: List<PanNote>,
    private val total: Int,
    private val penaltySeconds: Double,
    private val random: Random,
) : GameSession {
    init {
        require(notes.isNotEmpty()) { "No notes to play" }
        require(total > 0) { "Round must have at least one note" }
    }

    // Notes are drawn from shuffled batches, so every note appears once per batch.
    private val deck = ArrayDeque<PanNote>()
    private var lastPicked: PanNote? = null
    private var current: PanNote = nextNote()

    private var completed = 0
    private var missCount = 0
    private var startMs = -1L
    private var endMs = -1L

    override val prompt: String get() = "Tap ${current.promptText}"
    override val progress: String get() = "$completed / $total"
    override val isFinished: Boolean get() = completed >= total
    override val misses: Int get() = missCount

    override fun elapsedMs(nowMs: Long): Long = when {
        startMs < 0 -> 0L
        endMs >= 0 -> endMs - startMs
        else -> nowMs - startMs
    }

    override fun onTap(x: Float, y: Float, nowMs: Long): TapResult {
        if (isFinished) return TapResult.MISS
        if (startMs < 0) startMs = nowMs

        // Taps resolve to the nearest note, so near-misses between zones still count for the closest one.
        val target = notes.minBy { it.edgeDistance(x, y) }
        if (target != current) {
            missCount++
            return TapResult.MISS
        }

        completed++
        if (isFinished) {
            endMs = nowMs
        } else {
            current = nextNote()
        }
        return TapResult.HIT
    }

    override fun result(): RunResult? {
        if (!isFinished) return null
        val elapsed = elapsedMs(endMs)
        return RunResult(
            score = elapsed / 1000.0 + missCount * penaltySeconds,
            elapsedMs = elapsed,
            misses = missCount,
            targets = total,
        )
    }

    private fun nextNote(): PanNote {
        if (deck.isEmpty()) {
            val batch = notes.shuffled(random).toMutableList()
            val last = lastPicked
            // Avoid the same note twice in a row across batch boundaries.
            if (last != null && batch.size > 1 && batch.first() == last) {
                val swapWith = batch.indexOfFirst { it != last }
                val first = batch[0]
                batch[0] = batch[swapWith]
                batch[swapWith] = first
            }
            deck.addAll(batch)
        }
        return deck.removeFirst().also { lastPicked = it }
    }
}
