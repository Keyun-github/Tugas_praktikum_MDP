package com.example.minggu3

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val tvNamaBarang: TextView = findViewById(R.id.tvNamaBarang)
        val tvStokBarang: TextView = findViewById(R.id.tvStokBarang)
        val tvDeskripsi: TextView = findViewById(R.id.tvDeskripsi)
        val btnKembali: Button = findViewById(R.id.btnKembali)

        val namaBarang = intent.getStringExtra("BARANG_NAMA") ?: "Tidak diketahui"
        val stokBarang = intent.getIntExtra("BARANG_STOK", 0)

        tvNamaBarang.text = namaBarang
        tvStokBarang.text = "Stok: $stokBarang"
        tvDeskripsi.text = "$namaBarang tersedia dengan jumlah stok $stokBarang."

        btnKembali.setOnClickListener {
            finish()
        }
    }
}
