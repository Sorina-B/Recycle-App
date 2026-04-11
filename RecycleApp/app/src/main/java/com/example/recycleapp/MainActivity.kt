package com.example.recycleapp

import android.content.Intent
import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recycleapp.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import com.example.recycleapp.utils.cameraPermissionRequest
import com.example.recycleapp.utils.isPermissionGranted
import com.example.recycleapp.utils.openPermissionSetting
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private val CameraPermission=android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth
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

        firebaseDatabase=FirebaseDatabase.getInstance().reference
        auth=Firebase.auth
        signInAnonymously()

        binding.scanBttn.setOnClickListener {
            requestCameraAndStartScanner()
        }
        binding.historyBttn.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }

    private fun signInAnonymously() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        println("User ID: $uid")
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }else{
            println("User already signed in")
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
        Toast.makeText(this, "Scanning barcode: $barcode", Toast.LENGTH_SHORT).show()
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

    private fun handleServerResponse(response: String?) {
        if(response==null) {
            Toast.makeText(this, "No response from server", Toast.LENGTH_SHORT).show()
            return
        }
        val parts=response.split("|")
        val status=parts.getOrNull(0)?:""
        val productName=parts.getOrNull(1)?:"Unknown product"
        val instructions=parts.getOrNull(2)?:"No instructions available"

        if(status=="FOUND"){
            saveScannedProduct(productName,instructions)
            val intent=Intent(this,InstructionsActivity::class.java)
            intent.putExtra("productName",productName)
            intent.putExtra("instructions",instructions)
            startActivity(intent)
        }else{
            Toast.makeText(this, "Error: $productName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveScannedProduct(productName: String, instructions: String) {
        val uid=auth.currentUser?.uid?:return
        val timestamp=System.currentTimeMillis()
        val historyRef=firebaseDatabase.child("users").child(uid).child("history")
        val scanData=mapOf(
            "productName" to productName,
            "instructions" to instructions,
            "timestamp" to timestamp
        )
        historyRef.push().setValue(scanData).addOnCompleteListener {task->
            if(!task.isSuccessful){
                Toast.makeText(this, "Error saving scan history", Toast.LENGTH_SHORT).show()
            }
        }
    }
}