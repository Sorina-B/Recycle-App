package com.example.recycleapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recycleapp.databinding.ActivityScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityScannerBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        cameraSelector=CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture= ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {processCameraProvider = cameraProviderFuture.get()
                bindCameraPreview()
                bindInputAnalyser()
            }, ContextCompat.getMainExecutor(this)
    )

    }
    private fun bindCameraPreview(){
        cameraPreview= Preview.Builder()
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        processCameraProvider.unbindAll()
        processCameraProvider.bindToLifecycle(this,cameraSelector,cameraPreview)
    }

    private fun bindInputAnalyser(){
        val barcodeScanner: BarcodeScanner= BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
        imageAnalysis= ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor=Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor){imageProxy ->
            processImageProxy(barcodeScanner,imageProxy)
        }

        processCameraProvider.bindToLifecycle(this,cameraSelector,imageAnalysis)

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy){
        val inputImag= InputImage.fromMediaImage(imageProxy.image!!,imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(inputImag)
            .addOnSuccessListener {  barcodes ->
                if(barcodes.isNotEmpty()){
                    onScan?.invoke(barcodes)
                    onScan=null
                    finish()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }.addOnCompleteListener {
                imageProxy.close()
            }
    }
    companion object{
        private var onScan: ((barcodes:List<Barcode>)-> Unit)?=null
        fun startScanner(context: Context,onScan:(barcodes:List<Barcode>)-> Unit){
            this.onScan=onScan
            Intent(context, ScannerActivity::class.java).also{
                context.startActivity(it)
            }
        }//Intent - starts a new activity with the blueprint of ScannerActivity class,used from jumping from MainActivity to ScannerActivity

        //creem un obiect de tip Intent si also ne ajuta sa creem  o functie lambda fara sa mai instantiem, unde it este obiectul creat de tip Intent
    }
}



