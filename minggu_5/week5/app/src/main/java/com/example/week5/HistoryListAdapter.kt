package com.example.week5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder>() {

    // Date formatter
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun updateHistory(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged() // Use DiffUtil for better performance later
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(transactions[position], dateFormat)
    }

    override fun getItemCount(): Int = transactions.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHistoryProductName: TextView = itemView.findViewById(R.id.tvHistoryProductName)
        private val tvHistoryRetailerName: TextView = itemView.findViewById(R.id.tvHistoryRetailerName)
        private val tvHistoryQuantity: TextView = itemView.findViewById(R.id.tvHistoryQuantity)
        private val tvHistoryTotalPrice: TextView = itemView.findViewById(R.id.tvHistoryTotalPrice)
        private val tvHistoryTimestamp: TextView = itemView.findViewById(R.id.tvHistoryTimestamp)

        fun bind(transaction: Transaction, dateFormat: SimpleDateFormat) {
            tvHistoryProductName.text = transaction.productName
            tvHistoryRetailerName.text = transaction.retailerName
            tvHistoryQuantity.text = transaction.quantity.toString()
            tvHistoryTotalPrice.text = formatCurrency(transaction.totalPrice)
            tvHistoryTimestamp.text = dateFormat.format(transaction.timestamp)
        }

        // Helper to format currency
        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }
}