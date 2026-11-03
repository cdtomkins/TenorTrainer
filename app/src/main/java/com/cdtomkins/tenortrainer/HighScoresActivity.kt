package com.cdtomkins.tenortrainer

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cdtomkins.tenortrainer.data.HighScoreStore
import com.cdtomkins.tenortrainer.game.ModeRegistry
import java.text.DateFormat
import java.util.Date

class HighScoresActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = HighScoreStore(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        content.addView(Button(this).apply {
            text = getString(R.string.back)
            setOnClickListener { finish() }
        }, matchWidth(top = 0))

        content.addView(Button(this).apply {
            text = getString(R.string.clear_scores)
            setOnClickListener {
                AlertDialog.Builder(this@HighScoresActivity)
                    .setMessage(R.string.clear_scores_confirm)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.clear) { _, _ ->
                        store.clearAll()
                        recreate()
                    }
                    .show()
            }
        }, matchWidth(top = 8))

        ModeRegistry.modes.forEach { mode ->
            content.addView(TextView(this).apply {
                text = mode.title
                textSize = 20f
            }, matchWidth(top = 16))

            val scores = store.load(mode.id, mode.lowerIsBetter)
            if (scores.isEmpty()) {
                content.addView(TextView(this).apply {
                    text = getString(R.string.no_scores)
                    alpha = 0.7f
                }, matchWidth(top = 4))
            }
            scores.forEachIndexed { index, s ->
                content.addView(TextView(this).apply {
                    text = "#${index + 1}  ${PlayActivity.formatScore(s.score)}" +
                        "  (${PlayActivity.formatSeconds(s.elapsedMs)}, ${s.misses} wrong)\n" +
                        dateFormat.format(Date(s.timestamp))
                    textSize = 15f
                }, matchWidth(top = 6))
            }
        }

        setTitle(R.string.high_scores)
        setContentView(ScrollView(this).apply {
            fitsSystemWindows = true
            addView(content)
        })
    }

    private fun matchWidth(top: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { topMargin = dp(top) }
}
