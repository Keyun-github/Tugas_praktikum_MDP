package com.example.minggu3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditBarangActivity : AppCompatActivity() {

    private lateinit var etNamaBarang: EditText
    private lateinit var etJumlahStok: EditText
    private lateinit var etDeskripsiBarang: EditText
    private lateinit var btnUbahBarang: Button
    private lateinit var btnKembali: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_barang)

        etNamaBarang = findViewById(R.id.etNamaBarang)
        etJumlahStok = findViewById(R.id.etJumlahStok)
        etDeskripsiBarang = findViewById(R.id.etDeskripsiBarang)
        btnUbahBarang = findViewById(R.id.btnUbahBarang)
        btnKembali = findViewById(R.id.btnKembali)

        val idBarang = intent.getIntExtra("BARANG_ID", -1)
        val namaBarang = intent.getStringExtra("namaBarang") ?: ""
        val jumlahStok = intent.getIntExtra("jumlahStok", 0)
        val deskripsiBarang = intent.getStringExtra("deskripsiBarang") ?: ""

        etNamaBarang.setText(namaBarang)
        etJumlahStok.setText(jumlahStok.toString())
        etDeskripsiBarang.setText(deskripsiBarang)

        btnUbahBarang.setOnClickListener {
            val nama = etNamaBarang.text.toString().trim()
            val stok = etJumlahStok.text.toString().trim()
            val deskripsi = etDeskripsiBarang.text.toString().trim()

            if (nama.isEmpty() || stok.isEmpty() || deskripsi.isEmpty()) {
                Toast.makeText(this, "Semua input harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stokInt = stok.toIntOrNull()
            if (stokInt == null || stokInt <= 0) {
                Toast.makeText(this, "Jumlah stok harus angka dan lebih dari 0!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("BARANG_ID", idBarang)
                putExtra("namaBarang", nama)
                putExtra("jumlahStok", stokInt)
                putExtra("deskripsiBarang", deskripsi)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnKembali.setOnClickListener { finish() }
    }
}
