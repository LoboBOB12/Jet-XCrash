package com.jetxcrashking.game

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout

class MainActivity : AppCompatActivity() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var ship: ImageView
    private lateinit var counter: TextView
    private var score = 0
    private var obstacleCounter = 0
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var isRunning = true
    private val projectiles: MutableList<ImageView> = mutableListOf()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.main_layout)
        ship = findViewById(R.id.ship)
        counter = findViewById(R.id.counter)

        ship.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    ship.x = event.rawX - ship.width / 2
                    ship.y = event.rawY - ship.height / 2
                }
                MotionEvent.ACTION_DOWN -> {
                    fireProjectile()
                }
            }
            true
        }

        runnable = Runnable { createObstacles() }
        handler.postDelayed(runnable, 200)
    }

    private fun createObstacles() {
        if (!isRunning) return

        if (obstacleCounter % 4 == 0) {
            val obstacle = ImageView(this)
            obstacle.setImageResource(getRandomObstacle())
            obstacle.x = (0..(mainLayout.width - obstacle.width)).random().toFloat()
            obstacle.y = 0f
            mainLayout.addView(obstacle)

            val obstacleSpeed = (3..7).random()

            val moveRunnable = object : Runnable {
                override fun run() {
                    if (!isRunning) return

                    obstacle.y += obstacleSpeed

                    if (obstacle.y + obstacle.height >= mainLayout.height) {
                        mainLayout.removeView(obstacle)
                        return
                    }

                    for (projectile in projectiles) {
                        if (obstacle.isCollision(projectile) && projectile.tag != true) {
                            mainLayout.removeView(obstacle)
                            mainLayout.removeView(projectile)
                            projectile.tag = true
                            score += 100
                            counter.text = score.toString()
                            if (score >= 1500) {
                                score = 0
                                showBigWinPopup()
                            }
                        }
                    }

                    handler.postDelayed(this, 20)
                }
            }

            handler.post(moveRunnable)
        }

        obstacleCounter++
        handler.postDelayed(runnable, 500)
    }

    private fun ImageView.isCollision(view: View): Boolean {
        val thisX = this.x.toInt()
        val thisY = this.y.toInt()
        val thisWidth = this.width
        val thisHeight = this.height

        val viewX = view.x.toInt()
        val viewY = view.y.toInt()
        val viewWidth = view.width
        val viewHeight = view.height

        return thisX < viewX + viewWidth &&
                thisX + thisWidth > viewX &&
                thisY < viewY + viewHeight &&
                thisY + thisHeight > viewY
    }

    private fun getRandomObstacle(): Int {
        val obstacles = listOf(
            R.drawable.item1,
            R.drawable.item2,
            R.drawable.item3,
            R.drawable.item4
        )
        return obstacles.random()
    }

    private fun fireProjectile() {
        val projectile = ImageView(this)
        projectile.setImageResource(R.drawable.shoot)
        val shipX = ship.x + ship.width / 2 - projectile.width / 2
        val shipY = ship.y
        projectile.x = shipX
        projectile.y = shipY
        mainLayout.addView(projectile)
        projectiles.add(projectile)

        val projectileSpeed = 10

        val moveRunnable = object : Runnable {
            override fun run() {
                if (!isRunning) return

                projectile.y -= projectileSpeed

                if (projectile.y + projectile.height <= 0) {
                    mainLayout.removeView(projectile)
                    projectiles.remove(projectile)
                    handler.removeCallbacks(this)
                    return
                }

                handler.postDelayed(this, 20)
            }
        }

        handler.post(moveRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(runnable)
        for (projectile in projectiles) {
            mainLayout.removeView(projectile)
        }
        projectiles.clear()
    }

    private fun showBigWinPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_big_win)
        dialog.show()

        val handler = Handler()
        handler.postDelayed({
            dialog.dismiss()
        }, 2000)
    }
}