package com.video.call

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_call.*
import java.util.*

class CallActivity : AppCompatActivity() {
    var username = ""
    var frirndusername = ""
    var ispeerconnected = false
    var firebaseref = Firebase.database.getReference("users")
    var isaudio = true
    var isvideo = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        username = intent.getStringExtra("username")!!

        callbtn.setOnClickListener {
            frirndusername = friendnameedit.text.toString()
            sendcallrequest()
        }
        toggleaudiobtn.setOnClickListener{
            isaudio = !isaudio
            calljavascriptfunction("javascript:toggleAudio(\"${isaudio}\")")
            toggleaudiobtn.setImageResource(if(isaudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }
        togglevidiobtn.setOnClickListener{
            isvideo = !isvideo
            calljavascriptfunction("javascript:toggleVideo(\"${isvideo}\")")
            togglevidiobtn.setImageResource(if(isvideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }
        setupWebView()
    }

    private fun sendcallrequest() {
        if (!ispeerconnected){
            Toast.makeText(this, "You are not connected",Toast.LENGTH_LONG).show()
            return
        }
        firebaseref.child(frirndusername).child("incoming").setValue(username)
        firebaseref.child(frirndusername).child("isavailable").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value.toString() == "true"){
                    listenforconId()
                }
            }
        })
    }

    private fun listenforconId() {
        firebaseref.child(frirndusername).child("connId")addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                switchtocontrols()
                calljavascriptfunction("javascript:startCall{\"${snapshot.value}\"}")
            }
        })
    }

    private fun setupWebView() {
        webview.webChromeClient = object: WebChromeClient(){
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
        webview.settings.javaScriptEnabled = true
        webview.settings.mediaPlaybackRequiresUserGesture = true
        webview.addJavascriptInterface(javascriptinterface(this,)"Android")
        loadvideocall()
    }

    private fun loadvideocall() {
        val filepath = "file:android_asset/call.html"
        webview.loadUrl(filepath)

        webview.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                intializpeer()
            }
        }
    }
    var uniqueid = ""

    private fun intializpeer() {

        uniqueid = getuniqueid()

        calljavascriptfunction("javascript:init(\"${uniqueid}\")")
        firebaseref.child(username).child("incoming").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}

            override fun onCancelled(error: DatabaseError) {
                oncallrequest(snapshot.value as? String)
            }

        })
    }

    private fun oncallrequest(caller: String?) {
        if(caller==null) return

        calllayout.visibility = View.VISIBLE
        incommingcalltext.text = "$caller id calling.."
        acception.setOnClickListener {
            firebaseref.child(username).child("connId").setValue{uniqueid}
            firebaseref.child(username).child("isAvailable").setValue(true)

            calllayout.visibility = View.GONE
            switchtocontrols()
        }
        rejection.setOnClickListener{
            firebaseref.child(username).child("incoming").setValue(null)
            calllayout.visibility = View.GONE
        }
    }

    private fun switchtocontrols() {
        inputlayout.visibility = View.GONE
        callcontrollayout.visibility = View.VISIBLE
    }

    private fun getuniqueid(): String{
        return UUID.randomUUID().toString()
    }
    private fun calljavascriptfunction(fuctionString: String){
        webview.post { webview.evaluateJavascript(fuctionString, null) }
    }
    fun onpeerconnected() {
        ispeerconnected = true
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        firebaseref.child(username).setValue(null)
        webview.loadUrl("about:blank")
        super.onDestroy()
    }
}