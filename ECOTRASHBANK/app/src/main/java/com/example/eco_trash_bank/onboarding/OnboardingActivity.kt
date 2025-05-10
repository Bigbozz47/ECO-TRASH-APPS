package com.example.eco_trash_bank.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.eco_trash_bank.R
import com.example.eco_trash_bank.databinding.ActivityOnboardingBinding
import com.example.eco_trash_bank.main.MainActivity
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // siapkan data halaman
        val items = listOf(
            OnboardingItem(
                imageRes = R.drawable.onboarding1,
                title    = "Selamat Datang",
                description = "Bersama-sama kita menciptakan lingkungan yang lebih bersih dan lebih hijau dengan mengelola limbah secara efisien."
            ),
            OnboardingItem(
                imageRes = R.drawable.onboarding2,
                title    = "Penyerahan Sampah dengan Mudah",
                description = "Meminta penjemputan sampah dari rumah Anda atau mengantar sampah langsung ke tempat pengumpulan terdekat"
            ),
            OnboardingItem(
                imageRes = R.drawable.onboarding3,
                title    = "Tukarkan Poin dengan Hadiah",
                description = "Kumpulkan poin dari setiap pengantaran sampah dan tukarkan dengan berbagai hadiah menarik."
            )
        )

        adapter = OnboardingAdapter(items)
        binding.viewPager.adapter = adapter

        // indicator dots attach ke ViewPager2
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        // pakai transformer
        binding.viewPager.setPageTransformer(FadeSlidePageTransformer())

        // tombol Skip (sekarang btnSkip)
        binding.btnSkip.setOnClickListener { launchMain() }

        // tombol Back
        binding.btnBack.setOnClickListener {
            if (binding.viewPager.currentItem > 0)
                binding.viewPager.currentItem--
        }

        // tombol Next / Mulai
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < adapter.itemCount - 1) {
                binding.viewPager.currentItem++
            } else {
                launchMain()
            }
        }

        // listen page perubahan untuk update tombol
        binding.viewPager.registerOnPageChangeCallback(object:
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // hide Back di halaman pertama
                binding.btnBack.visibility = if (position == 0) View.GONE else View.VISIBLE

                // ubah teks Next ke "Mulai" di halaman terakhir
                binding.btnNext.text = if (position == adapter.itemCount - 1)
                    "Mulai" else "Selanjutnya"
            }
        })
    }

    private fun launchMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
