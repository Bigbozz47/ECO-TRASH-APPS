package com.example.nasabahbanksampah.ui.inputsampah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class TrackingPengangkutFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private val dummyUserLocation = LatLng(-6.200000, 106.816666) // Jakarta
    private val dummyDriverLocation = LatLng(-6.202000, 106.818000) // Dekat Jakarta

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tracking_pengangkut, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}