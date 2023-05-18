package com.arnyminerz.pocketchips.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.arnyminerz.pocketchips.R

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        findViewById<Button>(R.id.launch_client).setOnClickListener {
            startActivity(Intent(this, DemoClientActivity::class.java))
        }
        findViewById<Button>(R.id.launch_host).setOnClickListener {
            startActivity(Intent(this, DemoHostActivity::class.java))
        }
    }
}