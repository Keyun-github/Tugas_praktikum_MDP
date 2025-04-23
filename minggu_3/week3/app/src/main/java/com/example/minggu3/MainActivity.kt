package com.example.minggu3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangAdapter
    private lateinit var etSearch: EditText
    private lateinit var btnTambahBarang: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnLihatPenting: Button
    private lateinit var btnCari: Button

    private var barangList = mutableListOf(
        Barang(1, "Barang 1", 10, false),
        Barang(2, "Barang 2", 20, true),
        Barang(3, "Barang 3", 30, false),
        Barang(4, "Barang 4", 40, true),
        Barang(5, "Barang 5", 50, false)
    )

    private var displayedList = barangList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvBarang)
        etSearch = findViewById(R.id.etSearch)
        btnTambahBarang = findViewById(R.id.btnTambahBarang)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnLihatPenting = findViewById(R.id.btnLihatPenting)
        btnCari = findViewById(R.id.btnCari)

        // Inisialisasi Adapter dengan onFavToggle
        adapter = BarangAdapter(
            displayedList,
            onEdit = { editBarang(it) },
            onDelete = { hapusBarang(it) },
            onFavToggle = { toggleFavBarang(it) }, // Implementasikan ini
            onClick = {barang: Barang -> viewDetail(barang)}
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupSpinnerFilter()

        btnTambahBarang.setOnClickListener {
            val intent = Intent(this, TambahBarangActivity::class.java)
            startActivityForResult(intent, REQUEST_TAMBAH_BARANG)
        }

        btnLihatPenting.setOnClickListener {
            displayedList = barangList.filter { it.isPenting }.toMutableList()
            adapter.updateList(displayedList)
        }

        btnCari.setOnClickListener {
            val keyword = etSearch.text.toString()
            cariBarang(keyword)
        }
    }

    private fun viewDetail(barang: Barang){
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("BARANG_NAMA", barang.nama)
        intent.putExtra("BARANG_STOK", barang.stok)
        startActivity(intent)
    }

    private fun setupSpinnerFilter() {
        val filterOptions = arrayOf("Tidak ada filter", "Abjad A-Z", "Abjad Z-A", "Stok Terendah", "Stok Tertinggi")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter(filterOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cariBarang(keyword: String) {
        displayedList = barangList.filter { it.nama.contains(keyword, ignoreCase = true) }.toMutableList()
        adapter.updateList(displayedList)
    }

    private fun applyFilter(filter: String) {
        displayedList = when (filter) {
            "Abjad A-Z" -> displayedList.sortedBy { it.nama }.toMutableList()
            "Abjad Z-A" -> displayedList.sortedByDescending { it.nama }.toMutableList()
            "Stok Terendah" -> displayedList.sortedBy { it.stok }.toMutableList()
            "Stok Tertinggi" -> displayedList.sortedByDescending { it.stok }.toMutableList()
            else -> barangList.toMutableList()
        }
        adapter.updateList(displayedList)
    }

    private fun editBarang(barang: Barang) {
        val intent = Intent(this, EditBarangActivity::class.java).apply {
            putExtra("BARANG_ID", barang.id)
            putExtra("namaBarang", barang.nama)
            putExtra("jumlahStok", barang.stok)
            putExtra("barangPenting", barang.isPenting)
        }
        startActivityForResult(intent, REQUEST_EDIT_BARANG)
    }

    private fun hapusBarang(barang: Barang) {
        barangList.removeIf { it.id == barang.id }
        displayedList = barangList.toMutableList()
        adapter.updateList(displayedList)
        Toast.makeText(this, "Barang ${barang.nama} dihapus", Toast.LENGTH_SHORT).show()
    }

    // Implementasikan fungsi toggleFavBarang
    private fun toggleFavBarang(barang: Barang) {
        barang.isPenting = !barang.isPenting
        adapter.updateList(displayedList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_TAMBAH_BARANG -> {
                    val namaBarang = data.getStringExtra("namaBarang") ?: return
                    val jumlahStok = data.getIntExtra("jumlahStok", 0)
                    val isPenting = data.getBooleanExtra("barangPenting", false)
                    val newBarang = Barang(
                        id = barangList.size + 1,
                        nama = namaBarang,
                        stok = jumlahStok,
                        isPenting = isPenting
                    )

                    barangList.add(newBarang)
                    displayedList = barangList.toMutableList()
                    adapter.updateList(displayedList)
                }

                REQUEST_EDIT_BARANG -> {
                    val id = data.getIntExtra("BARANG_ID", -1)
                    val namaBarang = data.getStringExtra("namaBarang") ?: return
                    val jumlahStok = data.getIntExtra("jumlahStok", 0)

                    val barang = barangList.find { it.id == id }
                    if (barang != null) {
                        barang.nama = namaBarang
                        barang.stok = jumlahStok
                        displayedList = barangList.toMutableList()
                        adapter.updateList(displayedList)
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_TAMBAH_BARANG = 1
        const val REQUEST_EDIT_BARANG = 2
    }
}