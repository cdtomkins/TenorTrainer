package com.cdtomkins.tenortrainer.game

/** All modes shown in the main menu. Add new modes here. */
object ModeRegistry {
    val modes: List<GameMode> = listOf(
        TimeAttackMode(
            notes = TenorPan.notes,
            notesPerRound = 30,
            penaltySeconds = 10.0,
        ),
    )

    fun find(id: String): GameMode? = modes.firstOrNull { it.id == id }
}
