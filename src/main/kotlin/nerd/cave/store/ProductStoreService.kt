package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.product.Product

interface ProductStoreService: LifeCycle {
    suspend fun fetchAll(): List<Product>
    suspend fun fetchById(id: String): Product?
    suspend fun createProduct(product: Product)
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteById(id: String): Boolean
}