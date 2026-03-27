
package  com.example.recycleapp

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
//functia verifica daca utilizatorul a dat deja permisiunea de folosire a camerei

inline fun Context.cameraPermissionRequest(crossinline positive: ()->Unit){ //trimitem ca parametru o functie(crossinline) - positive este numele functiei, ()-tipul returnat, unit echivlent cu null
    AlertDialog.Builder(this)
        .setTitle("Camera Permission Required")
        .setMessage("Without accessing the camera it is not possible to scan the barcode.")
        .setPositiveButton("Allow Camera"){_, _ -> positive.invoke()} //daca este apasat butonul ok, vom activa functia data(openPermissionSettings) ca parametru
        .setNegativeButton("Cancel"){dialog, _ -> dialog.dismiss()}.show()

}

fun Context.openPermissionSetting(){
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also{intent ->
        val uri=Uri.fromParts("package",packageName,null)
        intent.data=uri
        startActivity(intent)
    }
    //if the permission is denied, you need to open the setting for it to be permitted
}