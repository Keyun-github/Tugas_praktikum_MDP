package com.example.week5

// Simple in-memory storage for products
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
        return products.toList() // Return a copy
    }

    fun findProductById(id: String): Product? {
        return products.find { it.id == id }
    }

    // Function for Retailer to add products (needed later)
    fun addProduct(product: Product) {
        products.add(product)
    }

    // Function for Retailer to update products (needed later)
    fun updateProduct(updatedProduct: Product): Boolean {
        val index = products.indexOfFirst { it.id == updatedProduct.id }
        return if (index != -1) {
            products[index] = updatedProduct
            true
        } else {
            false
        }
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

    // Search function (LIKE simulation)
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