package com.example.nasabahbanksampah.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val bantuanFragmentId = R.id.bantuanFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_beranda,
                R.id.navigation_riwayat,
                R.id.navigation_profil
            )
        )
        
        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_beranda,
                R.id.navigation_riwayat,
                R.id.navigation_profil -> {
                    if (navController.currentDestination?.id != item.itemId) {
                        NavigationUI.onNavDestinationSelected(item, navController)
                    }
                    true
                }
                else -> false
            }
        }

        // FAB navigasi ke BantuanFragment
        binding.fab.setOnClickListener {
            navController.navigate(bantuanFragmentId)
        }

        // Sembunyikan/tampilkan FAB dan BottomNav tergantung fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == bantuanFragmentId) {
                binding.fab.visibility = View.GONE
                binding.navView.visibility = View.GONE
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                binding.fab.visibility = View.VISIBLE
                binding.navView.visibility = View.VISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
