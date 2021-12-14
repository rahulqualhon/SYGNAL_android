package com.android.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.android.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    internal var handler = Handler(Looper.myLooper()!!)

    internal var runnable: Runnable = Runnable {
        //CheckAppUpdate.with(this@SplashActivity).appHasUpdateVersion(object : BaseActivity.ActionListener {
           // override fun onActionResult() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
          //  }
        //})
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(runnable, 3000)
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
