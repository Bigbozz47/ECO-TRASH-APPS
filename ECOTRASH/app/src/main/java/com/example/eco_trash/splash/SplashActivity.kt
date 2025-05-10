package com.example.eco_trash.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eco_trash.databinding.ActivitySplashBinding
import com.example.eco_trash.main.MainActivity
import com.example.eco_trash.onboarding.OnboardingActivity


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigasi saat tombol Mulai diklik
        binding.btnMulai.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }

    }
}