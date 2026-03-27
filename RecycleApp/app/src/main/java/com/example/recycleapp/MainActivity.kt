package com.example.recycleapp

import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recycleapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private val CameraPermission=android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted->
        if(isGranted){
            startScanner()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.scanBttn.setOnClickListener {
            requestCameraAndStartScanner()
        }

    }

    private fun requestCameraAndStartScanner(){
        if(isPermissionGranted(CameraPermission)){
            startScanner()
        }else{
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(CameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }

            else-> {
                requestPermissionLauncher.launch(CameraPermission)
            }
        }
    }

    private fun startScanner() {
        ScannerActivity.startScanner(this) { barcodes ->
            barcodes.forEach { barcode ->
                val scannedNumber = barcode.rawValue
                if (scannedNumber != null) {
                    fetchProductInfo(scannedNumber)
                }
            }
        }
    }

    private fun fetchProductInfo(barcode: String){
        Toast.makeText(this, "Scanning barcodeȘ $barcode", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try{
                val serverIp="192.168.100.4"
                val port=8080
                val socket=Socket(serverIp,port)
                val wirter=PrintWriter(socket.getOutputStream(),true)
                val reader=BufferedReader(InputStreamReader(socket.getInputStream()))

                wirter.println("SCAN|$barcode")
                val response=reader.readLine()

                socket.close()

                withContext(Dispatchers.Main){
                    handleServerResponse(response)

                }
            }catch (e: Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()

                }
            }
        }

    }

    private fun handleServerResponse(response: String) {
        if(response==null) {
            Toast.makeText(this, "No response from server", Toast.LENGTH_SHORT).show()
            return
        }
        val parts=response.split("|")
        val status=parts.getOrNull(0)?:""
        val payload=parts.getOrNull(1)?:""

        if(status=="FOUND"){
            Toast.makeText(this, "Recycle instructions : $payload", Toast.LENGTH_SHORT).show()
            //("Show recycle instructions")
        }else{
            Toast.makeText(this, "Error: $payload", Toast.LENGTH_SHORT).show()
        }
    }
}