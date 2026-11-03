package com.cdtomkins.tenortrainer.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TimeAttackTest {
    private val notes = listOf(
        PanNote("A", Register.LOW, 0f, 0f, 10f),
        PanNote("B", Register.MIDDLE, 100f, 0f, 10f),
        PanNote("C", Register.LOW, 200f, 0f, 10f),
        PanNote("C", Register.HIGH, 300f, 0f, 10f),
    )

    private fun newSession(total: Int = 5, penalty: Double = 10.0) =
        TimeAttackSession(notes, total, penalty, Random(42))

    /** Taps the centre of the current note and returns the note's prompt text. */
    private fun TimeAttackSession.hitCurrent(now: Long): String {
        val target = prompt.removePrefix("Tap ")
        val note = notes.first { it.promptText == target }
        assertEquals(TapResult.HIT, onTap(note.x, note.y, now))
        return target
    }

    private fun TimeAttackSession.missCurrent(now: Long) {
        // Tap far away from every note.
        assertEquals(TapResult.MISS, onTap(-1000f, -1000f, now))
    }

    @Test
    fun tapOutsideTargetCountsWhenItIsNearest() {
        val s = newSession(total = 5)
        val target = notes.first { it.promptText == s.prompt.removePrefix("Tap ") }
        // 15 px from the centre is outside the 10 px radius, but no other note is closer.
        assertEquals(TapResult.HIT, s.onTap(target.x + 15f, target.y, 1000L))
    }

    @Test
    fun tapNearestToAnotherNoteIsMiss() {
        val s = newSession(total = 5)
        val target = notes.first { it.promptText == s.prompt.removePrefix("Tap ") }
        val other = notes.first { it != target }
        assertEquals(TapResult.MISS, s.onTap(other.x, other.y, 1000L))
    }

    @Test
    fun finishesAfterExactlyTotalHits() {
        val s = newSession(total = 5)
        repeat(4) { s.hitCurrent(1000L + it) }
        assertFalse(s.isFinished)
        s.hitCurrent(5000L)
        assertTrue(s.isFinished)
        assertEquals("5 / 5", s.progress)
    }

    @Test
    fun neverRepeatsNoteConsecutively() {
        val s = newSession(total = 60)
        var previous: String? = null
        repeat(60) { i ->
            val name = s.hitCurrent(1000L + i)
            assertFalse("repeated $name", name == previous)
            previous = name
        }
    }

    @Test
    fun resultIsNullBeforeFinishing() {
        val s = newSession(total = 2)
        s.hitCurrent(1000L)
        assertNull(s.result())
    }

    @Test
    fun clockStartsOnFirstTap() {
        val s = newSession(total = 2)
        assertEquals(0L, s.elapsedMs(9999L))
        s.hitCurrent(1000L)
        assertEquals(500L, s.elapsedMs(1500L))
    }

    @Test
    fun missesAddPenaltyToScore() {
        val s = newSession(total = 2, penalty = 10.0)
        s.hitCurrent(1000L)
        s.missCurrent(1500L)
        s.missCurrent(1600L)
        s.hitCurrent(3000L)

        val result = s.result()!!
        assertEquals(2, result.misses)
        assertEquals(2000L, result.elapsedMs)
        assertEquals(2 + 2 * 10.0, result.score, 0.0001)
        assertEquals(2, result.targets)
    }

    @Test
    fun missesDoNotAdvanceRound() {
        val s = newSession(total = 3)
        val promptBefore = s.prompt
        s.missCurrent(1000L)
        assertEquals(promptBefore, s.prompt)
        assertEquals("0 / 3", s.progress)
    }

    @Test
    fun modeExposesIdAndSessions() {
        val mode = TimeAttackMode(notes, notesPerRound = 3, penaltySeconds = 10.0)
        assertEquals("time_attack", mode.id)
        assertTrue(mode.lowerIsBetter)
        assertEquals(0L, mode.newSession().elapsedMs(123L))
    }

    @Test
    fun promptNamesTheRegister() {
        val s = newSession(total = 1)
        assertTrue(s.prompt, s.prompt.matches(Regex("Tap (low|middle|high) [A-C]")))
    }

    @Test
    fun repeatedNameInWrongRegisterIsAMiss() {
        val s = TimeAttackSession(listOf(notes[2], notes[3]), total = 1, penaltySeconds = 10.0, random = Random(1))
        val target = notes.first { it.promptText == s.prompt.removePrefix("Tap ") }
        val other = notes.first { it.name == target.name && it.register != target.register }
        assertEquals(TapResult.MISS, s.onTap(other.x, other.y, 1000L))
        assertEquals(1, s.misses)
        assertEquals("0 / 1", s.progress)
    }
}
