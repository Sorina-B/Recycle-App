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
                    val packaging = databaseResponse.product.packaging ?: "Unknown packaging"
                    sendChannel.writeStringUtf8("FOUND|$packaging\n")
                } else {
                    sendChannel.writeStringUtf8("ERROR|Product not found\n")
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