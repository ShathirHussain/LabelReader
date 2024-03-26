package com.outdu.labelreader

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.intel.openvino.IECore
import java.io.File
import java.io.FileOutputStream
import java.io.IOError
import java.io.IOException
import java.io.OutputStream
import kotlin.system.exitProcess


open class CameraActivity : AppCompatActivity(){

    private var currentPhotoPath: String? = null
    private lateinit var ivInputImage : ImageView
    private lateinit var runbutton : Button
    private var curPredictionImage: Bitmap? = null
    private var basePredictionImage: Bitmap? = null
    private var imageCapture: ImageCapture? = null
    private var imageBitmap: Bitmap? = null
    private val cameraRequestCode = 200;
    private val videoRequestCode = 201;
    private var modelDir = "";
    private val pluginsXml = "plugins.xml"
    private val detModelXml = "det_model.xml"
    private val detModelBin = "det_model.bin"
    private val recModelXml = "rec_model.xml"
    private val recModelBin = "rec_model.bin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        ivInputImage = findViewById(R.id.iv_input_image)
        val imagePath = R.drawable.demoimage
        basePredictionImage = BitmapFactory.decodeResource(resources, imagePath)
        curPredictionImage = basePredictionImage

        ivInputImage.setImageBitmap(curPredictionImage)

//        modelDir = this.getExternalFilesDir()?.getAbsolutePath() ?: "";
        Toast.makeText(this,modelDir,Toast.LENGTH_LONG).show()
        if(ContextCompat.checkSelfPermission(this@CameraActivity,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@CameraActivity,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this@CameraActivity,
                    arrayOf(Manifest.permission.CAMERA), 1)
            } else {
                ActivityCompat.requestPermissions(this@CameraActivity,
                    arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
    }

    private fun copyFiles() {
        val fileNames = arrayOf<String>(
            detModelXml,
            detModelBin,
            recModelXml,
            recModelBin,
            pluginsXml,
        )
        for (fileName in fileNames) {
            val outputFilePath = "$modelDir/$fileName"
            val outputFile = File(outputFilePath)
            if (!outputFile.exists()) {
                try {
                    val inputStream = applicationContext.assets.open(fileName)
                    val outputStream: OutputStream = FileOutputStream(outputFilePath)
                    val buffer = ByteArray(5120)
                    var length = inputStream.read(buffer)
                    while (length > 0) {
                        outputStream.write(buffer, 0, length)
                        length = inputStream.read(buffer)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                } catch (e: Exception) {
                    Log.e("CopyError", "Copying model has failed.")
                    exitProcess(1)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@CameraActivity,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun capturePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, cameraRequestCode)
    }

    private fun openVideo(){
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(videoIntent, videoRequestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == cameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imageBitmap = data.extras?.get("data") as Bitmap

//                imageBitmap = Utils.rotateBitmap(imageBitmap)
                curPredictionImage = imageBitmap

                ivInputImage.setImageBitmap(curPredictionImage)
            }
        }

        if(requestCode == videoRequestCode){
            if(resultCode == Activity.RESULT_OK && data != null)
            {
                imageBitmap = data.extras?.get("data") as Bitmap
                curPredictionImage = imageBitmap
                ivInputImage.setImageBitmap(curPredictionImage)
            }
        }

    }


    fun btn_reset_img_click(view: View) {
        curPredictionImage = basePredictionImage
        ivInputImage.setImageBitmap(curPredictionImage)
    }

    fun startCapture(view: View) {
        capturePhoto()
    }

    override fun onResume() {
        super.onResume()
    }


    fun predictImage(view: View) {
        curPredictionImage?.let { runModel(it) }
    }


    private fun runModel(image: Bitmap) {
        try {
//            System.loadLibrary("opencv_java4")
            Toast.makeText(this, IECore.NATIVE_LIBRARY_NAME, Toast.LENGTH_LONG).show()
            System.loadLibrary(IECore.NATIVE_LIBRARY_NAME)

        } catch (e: UnsatisfiedLinkError) {
            Toast.makeText(this, "Failed to load ov libraries", Toast.LENGTH_SHORT).show()
            Log.e(
                "UnsatisfiedLinkError",
                "Failed to load native OpenVINO libraries\n$e"
            )
            exitProcess(1)
        }
//        copyFiles();

        try {
            // Get AssetManager instance
            val assetManager = assets

            // Read pluginsXml from assets
            val pluginsXmlInputStream = assetManager.open("plugins.xml")
            val pluginsXmlContent = pluginsXmlInputStream.bufferedReader().use { it.readText() }
            pluginsXmlInputStream.close()

            // Read detModelXml from assets
            val detModelXmlInputStream = assetManager.open("det_model.xml")
            val detModelXmlContent = detModelXmlInputStream.bufferedReader().use { it.readText() }
            detModelXmlInputStream.close()

            // Use the XML content with your existing code
            val core = IECore("$pluginsXmlContent")
            val net = core.ReadNetwork("$detModelXmlContent")
            val inputsInfo = net.inputsInfo
            val inputName = ArrayList<String>(inputsInfo.keys)[0]
            Toast.makeText(this, inputName, Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
            // Handle IO Exception
        }
    }

    private fun getAssetFilePath(context: Context, fileName: String): String {
        return context.assets.open(fileName).use { file ->
            val outFile = File(context.filesDir, fileName)
            outFile.outputStream().use { outFileStream ->
                file.copyTo(outFileStream)
            }
            outFile.absolutePath
        }
    }
}