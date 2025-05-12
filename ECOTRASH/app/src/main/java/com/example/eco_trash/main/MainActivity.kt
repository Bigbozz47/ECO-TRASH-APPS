package com.example.eco_trash.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eco_trash.R
import com.example.eco_trash.databinding.ActivityMainBinding

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
                R.id.navigation_home,
                R.id.navigation_history,
                R.id.navigation_profile
            )
        )


        binding.navView.setupWithNavController(navController)

        // Navigasi ke BantuanFragment saat FAB ditekan
        binding.fab.setOnClickListener {
            navController.navigate(bantuanFragmentId)
        }

        // Listener untuk perubahan fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == bantuanFragmentId) {
                // Sembunyikan FAB & BottomNavigationView, tampilkan back button
                binding.fab.visibility = View.GONE
                binding.navView.visibility = View.GONE
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                // Tampilkan FAB & BottomNavigationView, sembunyikan back button
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