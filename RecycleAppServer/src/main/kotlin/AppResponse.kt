package com.example

import kotlinx.serialization.Serializable

@Serializable
data class AppResponse(
    val barcode:String,
    val isFound:Boolean,
    val recycleInstruct:String
)
