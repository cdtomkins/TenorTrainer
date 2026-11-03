package com.cdtomkins.tenortrainer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HighScore(
    val score: Double,
    val elapsedMs: Long,
    val misses: Int,
    val targets: Int,
    val timestamp: Long,
)

/** Persists the top [MAX_PER_MODE] scores for each mode in SharedPreferences. */
class HighScoreStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(modeId: String, lowerIsBetter: Boolean): List<HighScore> {
        val raw = prefs.getString(key(modeId), null) ?: return emptyList()
        val array = JSONArray(raw)
        val scores = (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            HighScore(
                score = o.getDouble("score"),
                elapsedMs = o.getLong("elapsedMs"),
                misses = o.getInt("misses"),
                targets = o.getInt("targets"),
                timestamp = o.getLong("timestamp"),
            )
        }
        return scores.sortedWith(comparator(lowerIsBetter))
    }

    /** Adds [entry] and returns its 1-based rank, or null if it didn't make the table. */
    fun submit(modeId: String, lowerIsBetter: Boolean, entry: HighScore): Int? {
        val all = (load(modeId, lowerIsBetter) + entry).sortedWith(comparator(lowerIsBetter))
        val kept = all.take(MAX_PER_MODE)
        save(modeId, kept)
        val rank = kept.indexOf(entry)
        return if (rank >= 0) rank + 1 else null
    }

    /** Removes every saved score. The preferences file holds only high scores, so clearing it is enough. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun save(modeId: String, scores: List<HighScore>) {
        val array = JSONArray()
        scores.forEach { s ->
            array.put(
                JSONObject()
                    .put("score", s.score)
                    .put("elapsedMs", s.elapsedMs)
                    .put("misses", s.misses)
                    .put("targets", s.targets)
                    .put("timestamp", s.timestamp)
            )
        }
        prefs.edit().putString(key(modeId), array.toString()).apply()
    }

    private fun comparator(lowerIsBetter: Boolean): Comparator<HighScore> =
        if (lowerIsBetter) compareBy { it.score } else compareByDescending { it.score }

    private fun key(modeId: String) = "scores_$modeId"

    companion object {
        const val MAX_PER_MODE = 10
        private const val PREFS_NAME = "high_scores"
    }
}
