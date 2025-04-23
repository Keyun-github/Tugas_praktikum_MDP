package com.example.week2_tugas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAbout = findViewById<Button>(R.id.btnAbout)
        btnAbout.setOnClickListener {
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        btnPlay.setOnClickListener{
            val intent = Intent(this, InputName::class.java)
            startActivity(intent)
        }

        val btnHS = findViewById<Button>(R.id.btnHS)
        btnHS.setOnClickListener{
            val intent = Intent(this, HighScore::class.java)
            startActivity(intent)
        }
    }
}