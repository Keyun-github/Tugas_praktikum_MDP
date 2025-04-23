package com.example.week5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class RetailerProductListAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<RetailerProductListAdapter.RetailerProductViewHolder>() {

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // Use DiffUtil for better performance later
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetailerProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_retailer_product, parent, false)
        return RetailerProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: RetailerProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = products.size

    class RetailerProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvRetailerItemProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvRetailerItemProductPrice)
        private val tvStock: TextView = itemView.findViewById(R.id.tvRetailerItemStock)

        fun bind(product: Product) {
            tvProductName.text = product.name
            tvProductPrice.text = formatCurrency(product.price)
            tvStock.text = "Stock: ${product.stock}"
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }
}
