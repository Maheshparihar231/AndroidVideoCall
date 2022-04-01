package com.video.call

import android.webkit.JavascriptInterface

class javascriptinterface(val callActivity: CallActivity, s: String){
    @JavascriptInterface
    public fun onpeerconnected(){

        callActivity.onpeerconnected()
    }
}