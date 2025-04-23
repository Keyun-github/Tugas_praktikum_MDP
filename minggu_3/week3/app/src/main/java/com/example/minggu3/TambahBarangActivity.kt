package com.example.minggu3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TambahBarangActivity : AppCompatActivity() {

    private lateinit var etNamaBarang: EditText
    private lateinit var etJumlahStok: EditText
    private lateinit var etDeskripsiBarang: EditText
    private lateinit var cbBarangPenting: CheckBox
    private lateinit var btnTambahBarang: Button
    private lateinit var btnKembali: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_barang)

        etNamaBarang = findViewById(R.id.etNamaBarang)
        etJumlahStok = findViewById(R.id.etJumlahStok)
        etDeskripsiBarang = findViewById(R.id.etDeskripsiBarang)
        cbBarangPenting = findViewById(R.id.cbBarangPenting)
        btnTambahBarang = findViewById(R.id.btnTambahBarang)
        btnKembali = findViewById(R.id.btnKembali)

        btnTambahBarang.setOnClickListener {
            val namaBarang = etNamaBarang.text.toString().trim()
            val jumlahStokStr = etJumlahStok.text.toString().trim()
            val deskripsiBarang = etDeskripsiBarang.text.toString().trim()
            val barangPenting = cbBarangPenting.isChecked

            if (namaBarang.length < 3) {
                showToast("Nama Barang harus minimal 3 huruf")
                return@setOnClickListener
            }

            val jumlahStok = jumlahStokStr.toIntOrNull()
            if (jumlahStok == null || jumlahStok < 1) {
                showToast("Jumlah Stok harus minimal 1")
                return@setOnClickListener
            }

            if (deskripsiBarang.length < 3) {
                showToast("Deskripsi Barang harus minimal 3 huruf")
                return@setOnClickListener
            }

            val intent = Intent().apply {
                putExtra("namaBarang", namaBarang)
                putExtra("jumlahStok", jumlahStok)
                putExtra("deskripsiBarang", deskripsiBarang)
                putExtra("barangPenting", barangPenting)
            }
            setResult(Activity.RESULT_OK, intent)
            showToast("Barang berhasil ditambahkan!")
            finish()
        }

        btnKembali.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
