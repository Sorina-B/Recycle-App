package com.example

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseResponse(
    val status:Int =0,
    val product: ProductData?=null
)
