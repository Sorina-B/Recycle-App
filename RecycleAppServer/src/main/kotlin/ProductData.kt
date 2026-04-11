package com.example

import kotlinx.serialization.Serializable

@Serializable
data class ProductData(
    val product_name: String?=null,
    val packaging:String?=null,
    val packaging_text: String?=null
)
