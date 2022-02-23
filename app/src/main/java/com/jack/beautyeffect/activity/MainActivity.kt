package com.jack.beautyeffect.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jack.beautyeffect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val cameraBtn = binding.cameraBtn

        cameraBtn.setOnClickListener {
            Intent(this, CameraActivity::class.java)
                .apply {
                    startActivity(this)
                }
        }


    }
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}