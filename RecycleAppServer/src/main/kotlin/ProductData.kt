package com.example

import kotlinx.serialization.Serializable

@Serializable
data class ProductData(
    val packaging:String?=null
)
