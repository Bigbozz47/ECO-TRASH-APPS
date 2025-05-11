package com.example.nasabahbanksampah.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.nasabahbanksampah.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI
import com.example.nasabahbanksampah.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_beranda,
                R.id.navigation_riwayat,
                R.id.navigation_profil
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_beranda -> {
                    if (navController.currentDestination?.id != R.id.navigation_beranda) {
                        navController.navigate(R.id.navigation_beranda)
                    }
                    true
                }

                R.id.navigation_riwayat, R.id.navigation_profil -> {
                    if (navController.currentDestination?.id != item.itemId) {
                        NavigationUI.onNavDestinationSelected(item, navController)
                    }
                    true
                }

                else -> false
            }
        }
    }
}