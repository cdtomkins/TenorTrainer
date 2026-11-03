package com.cdtomkins.tenortrainer

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.cdtomkins.tenortrainer.data.HighScore
import com.cdtomkins.tenortrainer.data.HighScoreStore
import com.cdtomkins.tenortrainer.game.GameMode
import com.cdtomkins.tenortrainer.game.GameSession
import com.cdtomkins.tenortrainer.game.ModeRegistry
import com.cdtomkins.tenortrainer.game.TapResult
import com.cdtomkins.tenortrainer.game.TenorPan
import java.util.Locale

class PlayActivity : Activity() {
    private lateinit var mode: GameMode
    private lateinit var session: GameSession
    private lateinit var store: HighScoreStore
    private lateinit var promptView: TextView
    private lateinit var progressView: TextView
    private lateinit var timerView: TextView
    private lateinit var statusView: TextView
    private lateinit var panView: PanView

    private val handler = Handler(Looper.getMainLooper())
    private val sounds = Sounds()
    private var ticking = false
    private val ticker = object : Runnable {
        override fun run() {
            updateStatus()
            if (!session.isFinished) {
                handler.postDelayed(this, TICK_MS)
            } else {
                ticking = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modeId = intent.getStringExtra(EXTRA_MODE_ID)
        mode = modeId?.let { ModeRegistry.find(it) } ?: run {
            finish()
            return
        }
        session = mode.newSession()
        store = HighScoreStore(this)

        promptView = TextView(this).apply {
            textSize = 36f
            gravity = Gravity.CENTER_HORIZONTAL
        }
        progressView = TextView(this).apply { textSize = 16f }
        timerView = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.END
        }
        statusView = TextView(this).apply {
            textSize = 14f
            alpha = 0.8f
            gravity = Gravity.CENTER_HORIZONTAL
        }
        panView = PanView(this).apply {
            onTap = { x, y -> handleTap(x, y) }
            coveredNotes = TenorPan.notes
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
            addView(progressView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(timerView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }

        val quitButton = Button(this).apply {
            text = getString(R.string.quit_to_menu)
            setOnClickListener { finish() }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(promptView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(12)
            })
            addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(panView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(statusView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(16), dp(8), dp(16), dp(8))
            })
            addView(quitButton, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(16), 0, dp(16), dp(16))
            })
        })

        updateStatus()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        sounds.release()
        super.onDestroy()
    }

    private fun handleTap(x: Float, y: Float) {
        if (session.isFinished) return
        val result = session.onTap(x, y, SystemClock.elapsedRealtime())
        panView.showFeedback(x, y, result == TapResult.HIT)
        if (result == TapResult.HIT) sounds.playHit() else sounds.playMiss()

        if (session.isFinished) {
            showResult()
        } else if (!ticking && session.elapsedMs(SystemClock.elapsedRealtime()) > 0) {
            // The clock starts on the first tap.
            ticking = true
            handler.post(ticker)
        }
        updateStatus()
    }

    private fun updateStatus() {
        promptView.text = if (session.isFinished) getString(R.string.done) else session.prompt
        progressView.text = session.progress
        timerView.text = formatSeconds(session.elapsedMs(SystemClock.elapsedRealtime()))
        statusView.text = "Wrong taps: ${session.misses}"
    }

    private fun showResult() {
        val result = session.result() ?: return
        val entry = HighScore(
            score = result.score,
            elapsedMs = result.elapsedMs,
            misses = result.misses,
            targets = result.targets,
            timestamp = System.currentTimeMillis(),
        )
        val rank = store.submit(mode.id, mode.lowerIsBetter, entry)

        val message = buildString {
            append("Score: ${formatScore(result.score)}\n")
            append("Time: ${formatSeconds(result.elapsedMs)}\n")
            append("Wrong taps: ${result.misses}")
            if (rank != null) {
                append("\n\nNew high score! Rank #$rank")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Round complete")
            .setMessage(message)
            .setPositiveButton("High Scores") { _, _ ->
                startActivity(Intent(this, HighScoresActivity::class.java))
                finish()
            }
            .setNegativeButton("Main menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    companion object {
        const val EXTRA_MODE_ID = "mode_id"
        private const val TICK_MS = 100L

        fun formatSeconds(ms: Long): String = String.format(Locale.US, "%.2f s", ms / 1000.0)
        fun formatScore(score: Double): String = String.format(Locale.US, "%.2f", score)
    }
}
