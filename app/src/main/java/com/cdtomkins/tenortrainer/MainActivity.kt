package com.cdtomkins.tenortrainer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cdtomkins.tenortrainer.game.ModeRegistry

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(24))
        }

        content.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 28f
            gravity = Gravity.CENTER_HORIZONTAL
        }, matchWidthWithMargin(bottom = 24))

        ModeRegistry.modes.forEach { mode ->
            content.addView(Button(this).apply {
                text = mode.title
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, PlayActivity::class.java)
                            .putExtra(PlayActivity.EXTRA_MODE_ID, mode.id)
                    )
                }
            }, matchWidth())

            content.addView(TextView(this).apply {
                text = mode.description
                textSize = 14f
                alpha = 0.7f
            }, matchWidthWithMargin(bottom = 16))
        }

        content.addView(Button(this).apply {
            text = getString(R.string.high_scores)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HighScoresActivity::class.java))
            }
        }, matchWidth())

        content.addView(Button(this).apply {
            text = getString(R.string.quit)
            setOnClickListener { finishAffinity() }
        }, matchWidthWithMargin(bottom = 0))

        val root = FrameLayout(this).apply {
            addView(
                ImageView(this@MainActivity).apply {
                    setImageResource(R.drawable.tenor_pan)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                },
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            addView(
                View(this@MainActivity).apply { setBackgroundColor(SCRIM_COLOR) },
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            addView(
                ScrollView(this@MainActivity).apply {
                    fitsSystemWindows = true
                    addView(content)
                },
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
        setContentView(root)
    }

    private companion object {
        /** Darkens the pan photo behind the menu so the text stays readable. */
        const val SCRIM_COLOR = 0xB3000000.toInt()
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )

    private fun matchWidthWithMargin(bottom: Int) = matchWidth().apply {
        bottomMargin = dp(bottom)
        topMargin = dp(4)
    }
}
