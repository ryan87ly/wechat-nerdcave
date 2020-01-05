package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.product.Product

interface ProductStoreService: LifeCycle {
    suspend fun fetchAll(): List<Product>
    suspend fun fetchById(id: String): Product?
    suspend fun createProduct(product: Product)
    suspend fun deleteById(id: String): Boolean
}