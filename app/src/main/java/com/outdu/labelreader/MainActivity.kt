package com.outdu.labelreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    lateinit var credentials: Map<String, String>

    public fun checkLogin(view: View){
        var emailid =  findViewById<EditText>(R.id.emailEntry);
        var password = findViewById<EditText>(R.id.passwordEntry)

        var usernameVal = emailid.text.toString();
        var passwordVal = password.text.toString();
        if(credentials.containsKey(usernameVal))
        {
            if(credentials[usernameVal] == passwordVal)
            {
                Toast.makeText(this, "Hi ${emailid.text.toString()}", Toast.LENGTH_SHORT).show();
                navigateToCameraActivity();
            }
            else
            {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();

            }
        }
        else{
            Toast.makeText(this, "User is invalid", Toast.LENGTH_SHORT).show();
        }

    }

    private fun navigateToCameraActivity()
    {
        val intent = Intent(this, CameraActivity::class.java);
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        credentials = mapOf(
            "admin" to "password",
            "user1" to "password1",
            "user2" to "password2",
            "user3" to "password3"
        )

    }



}