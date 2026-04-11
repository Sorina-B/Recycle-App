package com.example

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


fun main(){
    runBlocking {
        val customJson=Json{ ignoreUnknownKeys=true}
        val httpClient= HttpClient(CIO){
            install(ContentNegotiation){
                json(customJson)
            }
        }
        val repository= DatabaseHandler(httpClient)

        val selectorManager= SelectorManager(Dispatchers.IO)
        val serverSocket= aSocket(selectorManager).tcp().bind(InetSocketAddress("0.0.0.0",8080))

        println("Custom protocol is running on port 8080")

        while(true){
            val socket=serverSocket.accept()
            println("App connected: ${socket.remoteAddress}")
            launch(Dispatchers.IO){
                handleClientConnection(socket,repository)
            }
        }

    }

}

suspend fun handleClientConnection(socket: Socket,repository: DatabaseHandler) {
    val receiveChannel = socket.openReadChannel()
    val sendChannel = socket.openWriteChannel(autoFlush = true)

    try {
        while (true) {
            val message = receiveChannel.readLine() ?: break
            println("Received from app: $message")

            val parts = message.split("|")
            val command = parts.getOrNull(0)?.uppercase() ?: ""
            val barcode = parts.getOrNull(1)?:""

            if (command == "SCAN") {
                val databaseResponse = repository.getProductInfo(barcode)
                if (databaseResponse != null && databaseResponse.status == 1 && databaseResponse.product != null) {
                    val rawMaterial=databaseResponse.product.packaging
                        ?:databaseResponse.product.packaging_text
                        ?:""
                    val productName=databaseResponse.product.product_name?:"Unknown Product"
                    val instructions =generateRecyclingInstructions(rawMaterial)
                    sendChannel.writeStringUtf8("FOUND|$productName|$instructions\n")
                } else {
                    sendChannel.writeStringUtf8("ERROR|Product not found|Try scanning another item.\n")
                }
            } else if (command == "QUIT") {
                sendChannel.writeStringUtf8("BYE|\n")
                break
            } else {
                sendChannel.writeStringUtf8("ERROR|Unknown command\n")
            }

        }
    }catch (e: Exception){
        println("Connection error: ${e.message}")
    }finally {
        socket.close()
        println("Android App disconnected")
    }
}

fun generateRecyclingInstructions(packagingString:String?):String{
    if(packagingString.isNullOrBlank()){
        return "No packaging data found for this item. Please check the label."
    }
    val lowerCasePck=packagingString.lowercase()
    val instructions=mutableListOf<String>()
    if(listOf("cardboard","paper","box","carton","hartie").any{it in lowerCasePck}){
        instructions.add("Carboard/Paper: Flatten and put in the blue recycling bin. Throw it in the BLUE BIN.")
    }
    if(listOf("pet","bottle","plastic 1","jug","plastic","bidon").any{it in lowerCasePck}){
        instructions.add("Hard Plastic/PET Bottles:Empty, rinse, and recycle. Keep caps on.Throw it in the YELLOW BIN.")
    }
    if(listOf("wrapper","film","bag","sachet","punga").any{it in lowerCasePck}){
        instructions.add("Soft Plastic/Wrappers: Throw in the YELLOW BIN.")
    }
    if(listOf("aluminum","can","tin","doza","metal","conserva").any{it in lowerCasePck}){
        instructions.add("Aluminum/Metal Can: Empty and recycle. Do not crush.Throw it in the YELLOW BIN.")
    }
    if(listOf("glass","jar","borcan","sticla").any{it in lowerCasePck}){
        instructions.add("Glass:Rinse and clean and put in the glass recycling bin. Remove lids.Throw it in the GREEN BIN.")
    }
    if(instructions.isEmpty()){
        return "Material identified as $packagingString. Please check your local guideline. "
    }
    return instructions.joinToString(" | ")
}