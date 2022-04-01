package com.video.call

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.res.TypedArrayUtils.getText
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    val requestcode =1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!ispermission()){
            askpermission()
        }
        Firebase.initialize(this)
        loginbtn.setOnClickListener{
            val username = EditText.AUTOFILL_TYPE_TEXT
            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra("username",username)
            startActivity(intent)
        }
    }

    private fun askpermission() {
        ActivityCompat.requestPermissions(this, permission,requestcode)
    }

    private fun ispermission(): Boolean{

        permission.forEach {
            if(ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }

        return true
    }
}