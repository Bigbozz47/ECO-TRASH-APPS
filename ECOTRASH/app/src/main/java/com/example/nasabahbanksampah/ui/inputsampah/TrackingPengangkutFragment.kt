package com.example.nasabahbanksampah.ui.inputsampah

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentTrackingPengangkutBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TrackingPengangkutFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var _binding: FragmentTrackingPengangkutBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private val dummyUserLocation = LatLng(-6.200000, 106.816666) // Jakarta
    private val dummyDriverLocation = LatLng(-6.202000, 106.818000) // Dekat Jakarta

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tracking_pengangkut, container, false)

        // Binding for user profile
        _binding = FragmentTrackingPengangkutBinding.inflate(inflater, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Fetch user profile data
        fetchUserProfile()

        return view
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        Log.d("DEBUG_TOKEN", "Token: $token")

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                Log.d("DEBUG_FIRESTORE_BODY", bodyString ?: "null")

                if (!response.isSuccessful || bodyString == null) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val json = JSONObject(bodyString)
                val username = json.optString("username", "Pengguna")
                val role = json.optString("role", "nasabah")

                activity?.runOnUiThread {
                    binding.userName.text = username
                    binding.userStatus.text = "• ${role.replaceFirstChar { it.uppercase() }}"
                }
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Tambah marker user
        googleMap.addMarker(MarkerOptions().position(dummyUserLocation).title("Lokasi Anda"))

        // Tambah marker pengangkut
        googleMap.addMarker(
            MarkerOptions()
                .position(dummyDriverLocation)
                .title("Pengangkut Sampah")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        // Zoom ke tengah
        val bounds = LatLngBounds.builder()
            .include(dummyUserLocation)
            .include(dummyDriverLocation)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}