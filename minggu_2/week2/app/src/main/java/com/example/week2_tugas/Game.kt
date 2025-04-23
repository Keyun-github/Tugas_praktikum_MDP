package com.example.week2_tugas

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.semantics.text

class Game : AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvScore: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var btnSubmit: Button
    private lateinit var btnExit: Button
    private lateinit var etInput: EditText

    private var answerWord = getRandomWord()  // Kata yang harus ditebak
    private var currentRow = 0
    private val maxAttempts = 5
    private val letterBlocks = mutableListOf<TextView>()
    private var score = 0

    // Nama untuk SharedPreferences
    private val PREFS_NAME = "MyPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvUsername = findViewById(R.id.tvUsername)
        tvUsername.text = intent.getStringExtra("name")
        tvScore = findViewById(R.id.tvScore)
        gridLayout = findViewById(R.id.gridLayout)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnExit = findViewById(R.id.btnExit)
        etInput = findViewById(R.id.etInput)

        setupGameBoard()
        updateScore()

        btnSubmit.setOnClickListener {
            checkGuess()
        }

        btnExit.setOnClickListener {
            saveHighScore()
            val init = Intent(this, MainActivity::class.java)
            startActivity(init)
        }
    }

    private fun setupGameBoard() {
        gridLayout.removeAllViews()
        letterBlocks.clear()
        currentRow = 0

        for (row in 0 until maxAttempts) {
            for (col in 0 until 5) {
                val letterBlock = TextView(this).apply {
                    text = ""
                    textSize = 24f
                    gravity = Gravity.CENTER
                    width = 120
                    height = 120
                    setBackgroundResource(R.drawable.grid_border)
                    setTextColor(Color.BLACK)
                    isEnabled = false // Menonaktifkan klik pada tombol grid
                }
                gridLayout.addView(letterBlock)
                letterBlocks.add(letterBlock)
            }
        }
    }

    private fun checkGuess() {
        if (currentRow >= maxAttempts) {
            Toast.makeText(this, "Kesempatan habis!", Toast.LENGTH_SHORT).show()
            return
        }

        val guess = etInput.text.toString().trim().uppercase()
        if (guess.length != 5) {
            Toast.makeText(this, "Harus 5 huruf!", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear the EditText after getting the input
        etInput.text.clear()

        val handler = Handler(Looper.getMainLooper())
        var correctCount = 0

        for (i in guess.indices) {
            val letter = guess[i]
            val blockIndex = currentRow * 5 + i
            val block = letterBlocks[blockIndex]

            handler.postDelayed({
                when {
                    letter == answerWord[i] -> {
                        block.setBackgroundColor(Color.GREEN)
                        correctCount++
                    }
                    letter in answerWord -> block.setBackgroundColor(Color.YELLOW)
                    else -> block.setBackgroundColor(Color.RED)
                }
                block.text = letter.toString()
            }, (i * 300).toLong())
        }

        handler.postDelayed({
            if (correctCount == 5) {
                showWinState()
            } else {
                currentRow++
                if (currentRow >= maxAttempts) {
                    showLoseState()
                }
            }
        }, 1500)
    }

    private fun showWinState() {
        Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show()
        btnSubmit.text = "CONTINUE"
        btnSubmit.setBackgroundColor(Color.GREEN)
        btnSubmit.setOnClickListener {
            resetGame()
        }

        // Tambah skor
        val points = when (currentRow) {
            0 -> 20
            1 -> 18
            2 -> 16
            3 -> 14
            4 -> 12
            else -> 0
        }
        score += points
        updateScore()
    }

    private fun showLoseState() {
        Toast.makeText(this, "You Lose!", Toast.LENGTH_LONG).show()
        btnSubmit.isEnabled = false
    }

    private fun resetGame() {
        answerWord = getRandomWord()
        setupGameBoard()
        btnSubmit.text = "SUBMIT"
        btnSubmit.setBackgroundColor(Color.LTGRAY)
        btnSubmit.setOnClickListener {
            checkGuess()
        }
    }

    private fun updateScore() {
        tvScore.text = "Score: $score"
    }

    private fun saveHighScore() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = tvUsername.text.toString()
        val editor = prefs.edit()
        editor.putInt(name, score)
        editor.apply()
    }

    private fun getRandomWord(): String {
        val wordList = listOf(
            "TABLE", "WATER", "LIGHT", "HOUSE", "MONEY", "PLANT", "MUSIC", "FRUIT", "RIVER", "TRAIN",
            "CLOUD", "EARTH", "STONE", "PAPER", "HEART", "MOVIE", "CHAIR", "BREAD", "COLOR", "PHONE",
            "BEACH", "NIGHT", "MONTH", "CLOCK", "DRESS", "SHOES", "SMILE", "SOUND", "TASTE", "TOUCH",
            "SMELL", "VOICE", "DREAM", "WORLD", "POWER", "ANGEL", "QUEEN", "TIGER", "HORSE", "SNAKE",
            "MOUSE", "EAGLE", "OCEAN", "STARS", "STORM", "FLAME", "FROST", "SPICE", "JUICE", "CANDY"
        )

        return wordList.shuffled().first()
    }
}