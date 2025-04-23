package com.example.week5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RetailerHistoryAdapter(
    private var historyItems: List<RetailerHistoryItem> // Use the combined data class
) : RecyclerView.Adapter<RetailerHistoryAdapter.RetailerHistoryViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun updateHistory(newHistoryItems: List<RetailerHistoryItem>) {
        historyItems = newHistoryItems
        notifyDataSetChanged() // Use DiffUtil later
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetailerHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_retailer_history, parent, false)
        return RetailerHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RetailerHistoryViewHolder, position: Int) {
        holder.bind(historyItems[position], dateFormat)
    }

    override fun getItemCount(): Int = historyItems.size

    class RetailerHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvRetailerHistoryProductName)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvRetailerHistoryCustomerName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvRetailerHistoryQuantity)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvRetailerHistoryTotalPrice)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvRetailerHistoryTimestamp)

        fun bind(historyItem: RetailerHistoryItem, dateFormat: SimpleDateFormat) {
            val transaction = historyItem.transaction
            tvProductName.text = transaction.productName
            tvCustomerName.text = historyItem.customerName ?: "N/A" // Show customer name
            tvQuantity.text = transaction.quantity.toString()
            tvTotalPrice.text = formatCurrency(transaction.totalPrice)
            tvTimestamp.text = dateFormat.format(transaction.timestamp)
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }
}