package com.outdu.labelreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class Utils {
    companion object {
        fun copyFileFromAssets(appCtx: Context, srcPath: String, dstPath: String) {
            if (srcPath.isEmpty() || dstPath.isEmpty()) {
                return
            }
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                `is` = BufferedInputStream(appCtx.assets.open(srcPath))
                os = BufferedOutputStream(FileOutputStream(File(dstPath)))
                val buffer = ByteArray(1024)
                var length = 0
                while (`is`.read(buffer).also { length = it } != -1) {
                    os.write(buffer, 0, length)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    os!!.close()
                    `is`!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun copyDirectoryFromAssets(appCtx: Context, srcDir: String, dstDir: String) {
            if (srcDir.isEmpty() || dstDir.isEmpty()) {
                return
            }
            try {
                if (!File(dstDir).exists()) {
                    File(dstDir).mkdirs()
                }
                for (fileName in appCtx.assets.list(srcDir)!!) {
                    val srcSubPath = srcDir + File.separator + fileName
                    val dstSubPath = dstDir + File.separator + fileName
                    if (File(srcSubPath).isDirectory()) {
                        copyDirectoryFromAssets(appCtx, srcSubPath, dstSubPath)
                    } else {
                        copyFileFromAssets(appCtx, srcSubPath, dstSubPath)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> return bitmap
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1F, 1F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate(180F)
                    matrix.postScale(-1F, 1F)
                }

                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate(90F)
                    matrix.postScale(-1F, 1F)
                }

                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate(-90F)
                    matrix.postScale(-1F, 1F)
                }

                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
                else -> return bitmap
            }
            return try {
                val bmRotated = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
                )
                bitmap.recycle()
                bmRotated
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                null
            }
        }

    }
}


