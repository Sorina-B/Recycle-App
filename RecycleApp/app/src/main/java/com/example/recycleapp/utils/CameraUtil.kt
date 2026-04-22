package com.example.recycleapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat


fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

inline fun Context.cameraPermissionRequest(crossinline positive: ()->Unit){
    AlertDialog.Builder(this)
        .setTitle("Camera Permission Required")
        .setMessage("Without accessing the camera it is not possible to scan the barcode.")
        .setPositiveButton("Allow Camera"){_, _ -> positive.invoke()}
        .setNegativeButton("Cancel"){dialog, _ -> dialog.dismiss()}.show()

}

fun Context.openPermissionSetting(){
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also{intent ->
        val uri=Uri.fromParts("package",packageName,null)
        intent.data=uri
        startActivity(intent)
    }
}