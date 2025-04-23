package com.example.week2_tugas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HighScore : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var highScoreList: MutableList<Pair<String, Int>>
    private lateinit var btnBack: Button
    private lateinit var tvHighScore: TextView

    // Nama untuk SharedPreferences
    private val PREFS_NAME = "MyPrefs"
    // Key untuk menyimpan High Score
    private val HIGH_SCORE_KEY = "highScore"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)

        listView = findViewById(R.id.listView)
        highScoreList = loadHighScores().toMutableList()
        btnBack = findViewById(R.id.btnBack)
        tvHighScore = findViewById(R.id.tvHighScore)

        // Urutkan daftar berdasarkan skor tertinggi
        highScoreList.sortByDescending { it.second }

        // Buat adapter untuk ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, highScoreList.map { "${it.first} - ${it.second}" })
        listView.adapter = adapter

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    // Fungsi untuk memuat High Scores dari SharedPreferences
    private fun loadHighScores(): List<Pair<String, Int>> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allEntries = prefs.all
        val highScoreList = mutableListOf<Pair<String, Int>>()

        for ((key, value) in allEntries) {
            if (key != HIGH_SCORE_KEY && value is Int) {
                val name = key
                val score = value
                highScoreList.add(Pair(name, score))
            }
        }

        return highScoreList
    }
}