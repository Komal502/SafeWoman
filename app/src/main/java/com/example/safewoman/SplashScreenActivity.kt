package com.example.safewoman

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        val SPLASH_DELAY: Long=3000  //3 seconds

        Handler().postDelayed({
            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent);
            finish()
        },SPLASH_DELAY)
   }
}
