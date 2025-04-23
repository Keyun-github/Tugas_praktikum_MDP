package com.example.week5

// Add specific import if needed, e.g. for Log
// import android.util.Log

class ProductRepository private constructor() {

    private val products = mutableListOf<Product>()

    init {
        // Add some dummy products from different retailers for testing
        products.add(Product(name = "Barang 1", price = 50000.0, description = "Lorem ipsum dolor sit amet...", stock = 100, retailerName = "ISTTS Store", retailerEmail = "retailer@example.com"))
        products.add(Product(name = "Barang 2", price = 100000.0, description = "Consectetur adipisicing elit...", stock = 50, retailerName = "ISTTS Store", retailerEmail = "retailer@example.com"))
        products.add(Product(name = "Barang 3", price = 150000.0, description = "Quisquam consequuntur similique sit...", stock = 200, retailerName = "Toko Lain", retailerEmail = "another@example.com"))
        products.add(Product(name = "Produk Keren", price = 75000.0, description = "Deskripsi produk keren...", stock = 10, retailerName = "Toko Lain", retailerEmail = "another@example.com"))
        // Add more products as needed
    }

    fun getAllProducts(): List<Product> {
        return products.toList() // Return a copy for customers
    }

    // --- RETAILER METHODS ---
    fun getProductsByRetailer(email: String): List<Product> {
        return products.filter { it.retailerEmail == email }.toList()
    }

    fun addProduct(product: Product): Boolean {
        // Optional: Add check if product with same name from same retailer exists?
        products.add(product)
        // Log.d("ProductRepository", "Product added: ${product.name} by ${product.retailerEmail}")
        return true // Assuming success for now
    }

    fun updateProduct(updatedProduct: Product): Boolean {
        val index = products.indexOfFirst { it.id == updatedProduct.id && it.retailerEmail == updatedProduct.retailerEmail } // Ensure retailer owns it
        return if (index != -1) {
            products[index] = updatedProduct
            // Log.d("ProductRepository", "Product updated: ${updatedProduct.name}")
            true
        } else {
            // Log.d("ProductRepository", "Product update failed: ID ${updatedProduct.id} not found or ownership mismatch.")
            false
        }
    }
    // --- END RETAILER METHODS ---

    fun findProductById(id: String): Product? {
        return products.find { it.id == id }
    }

    // Function for Customer purchase
    fun decreaseStock(productId: String, quantity: Int): Boolean {
        val product = findProductById(productId)
        return if (product != null && product.stock >= quantity) {
            product.stock -= quantity
            true
        } else {
            false
        }
    }

    // Search function for Customer (all products)
    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) {
            return getAllProducts()
        }
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.retailerName.contains(query, ignoreCase = true)
        }
    }

    // Search function for Retailer (own products)
    fun searchOwnProducts(retailerEmail: String, query: String): List<Product> {
        val ownProducts = getProductsByRetailer(retailerEmail)
        if (query.isBlank()) {
            return ownProducts
        }
        return ownProducts.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            // No need to search retailer name here
        }
    }


    companion object {
        @Volatile
        private var instance: ProductRepository? = null

        fun getInstance(): ProductRepository {
            return instance ?: synchronized(this) {
                instance ?: ProductRepository().also { instance = it }
            }
        }
    }
}