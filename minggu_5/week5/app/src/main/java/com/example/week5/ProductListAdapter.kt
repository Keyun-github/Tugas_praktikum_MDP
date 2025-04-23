package com.example.week5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ProductListAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit // Lambda for click handling
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    // Updates the list and notifies the adapter
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener { onItemClick(product) } // Set click listener
    }

    override fun getItemCount(): Int = products.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvRetailerName: TextView = itemView.findViewById(R.id.tvRetailerName)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStock)

        fun bind(product: Product) {
            tvProductName.text = product.name
            tvProductPrice.text = formatCurrency(product.price)
            tvRetailerName.text = product.retailerName
            tvStock.text = "Stock: ${product.stock}"
        }

        // Helper to format currency (duplicate from ViewModel, consider putting in a Util class)
        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }
}