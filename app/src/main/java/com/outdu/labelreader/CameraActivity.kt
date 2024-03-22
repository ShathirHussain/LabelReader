package com.outdu.labelreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FilenameFilter


open class CameraActivity : AppCompatActivity(){

    private var currentPhotoPath: String? = null
    private lateinit var ivInputImage : ImageView
    private lateinit var runbutton : Button
    private var curPredictionImage: Bitmap? = null
    private var basePredictionImage: Bitmap? = null
    private var imageCapture: ImageCapture? = null
    private var imageBitmap: Bitmap? = null
    private val cameraRequestCode = 200;
    private val videoRequestCode = 200;

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
        if(requestCode == 200) {
            if (resultCode == Activity.RESULT_OK && data != null) {
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

    override fun onPause() {
        super.onPause()
        ivInputImage.setImageBitmap(basePredictionImage)
    }

}