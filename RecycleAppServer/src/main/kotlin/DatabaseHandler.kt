package com.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class DatabaseHandler(private val client: HttpClient) {
    suspend fun getProductInfo(barcode:String): DatabaseResponse?{
        return try{
            val url="https://world.openfoodfacts.org/api/v2/product/$barcode.json"
            client.get(url).body<DatabaseResponse>()
        }catch(e: Exception){
            println("Database error: ${e.message}")
            null
        }
    }
}