package com.mashiro.filament.utils

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.view.PixelCopy
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    fun screenShot(surfaceView: SurfaceView){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val width = surfaceView.width
            val height = surfaceView.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
            PixelCopy.request(surfaceView, bitmap, @RequiresApi(Build.VERSION_CODES.N) object : PixelCopy.OnPixelCopyFinishedListener{
                override fun onPixelCopyFinished(copyResult: Int) {
                    if (copyResult == PixelCopy.SUCCESS){
                        saveBitmapToGallery(bitmap, surfaceView.context.cacheDir.absolutePath+"/test.jpg")
                    }
                }
            }, Handler())
        }

    }

    fun saveBitmapToGallery(bitmap: Bitmap, imagePath: String): Boolean {
        if(bitmap.isRecycled){
            return false
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
        } catch (e: Exception) {
            return false
        } finally {
            fileOutputStream?.close()
        }
        return true
    }
}