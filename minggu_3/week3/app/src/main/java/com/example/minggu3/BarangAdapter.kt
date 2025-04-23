package com.example.minggu3

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class BarangAdapter(
    private var barangList: MutableList<Barang>,
    private val onEdit: (Barang) -> Unit,
    private val onDelete: (Barang) -> Unit,
    private val onFavToggle: (Barang) -> Unit, // Tambahkan callback ini
    private val onClick: (Barang) -> Unit
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    inner class BarangViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvStok: TextView = view.findViewById(R.id.tvStok)
        val btnFav: MaterialButton = view.findViewById(R.id.btnFav)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
        val barangRow: LinearLayout = view.findViewById(R.id.barangRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        holder.tvNama.text = barang.nama
        holder.tvStok.text = "Stok: ${barang.stok}"

        holder.tvNama.setTextColor(if (barang.isPenting) Color.RED else Color.BLACK)

        // Set Icon untuk btnFav berdasarkan isPenting
        val iconRes = if (barang.isPenting) R.drawable.ic_star_border else R.drawable.ic_star_kosong
        holder.btnFav.icon = ContextCompat.getDrawable(holder.itemView.context, iconRes)

        holder.btnEdit.setOnClickListener { onEdit(barang) }
        holder.btnDelete.setOnClickListener { onDelete(barang) }

        // Tambahkan OnClickListener ke btnFav
        holder.btnFav.setOnClickListener { onFavToggle(barang) }
        holder.barangRow.setOnClickListener{
            onClick.invoke(barang)
        }
    }

    override fun getItemCount(): Int = barangList.size

    fun updateList(newList: List<Barang>) {
        barangList.clear()
        barangList.addAll(newList)
        notifyDataSetChanged()
    }
}