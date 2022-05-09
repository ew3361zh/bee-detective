package com.example.beedetective

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


class BeeMapFragment : Fragment() {

    private var locationPermissionGranted = false // Checks for permission to access location

    private var mapStartingPointLaunched = false // Checks to see if we zoomed in on current location

    private var fusedLocationProvider: FusedLocationProviderClient? = null

    private var map: GoogleMap? = null //Holds an instance of our google map

    private val beeMarkers = mutableListOf<Marker>()

    private var beeSightingsList = listOf<BeeReport>()

//    private val beeReportViewModel: BeeReportViewModel by lazy {
//        ViewModelProvider(requireActivity()).get(BeeReportViewModel::class.java)
//    }
    private val beeReportViewModel: BeeReportViewModel by lazy {
        val app = requireActivity().application as BeeReportApplication
        BeeReportViewModel.ReportViewModelFactory(app.beeReportRepository)
            .create(BeeReportViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->

        map = googleMap

        googleMap.setOnInfoWindowClickListener { marker ->
            val markerForBee = marker.tag as BeeReport

        }
        updateMap()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bee_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)

        requestLocationPermission()

        beeReportViewModel.latestReports.observe(requireActivity()) { recentBees ->
            beeSightingsList = recentBees
            markBees()
        }

        return view
    }

    private fun markBees() {
        if (map == null) {return}

        for (marker in beeMarkers) {
            marker.remove()
        }

        for (bee in beeSightingsList) {
            bee.location?.let { geoPoint ->
                // val isUsers = could be used to mark user submitted reports.

                val beeIcon = R.drawable.bee_map_marker

                val markerOptions = MarkerOptions()
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .title(bee.userNotes)
                    .snippet("Sighted on ${bee.dateReported}")
                    .icon(BitmapDescriptorFactory.fromResource(beeIcon))

                map?.addMarker(markerOptions)?.also { marker ->
                    beeMarkers.add(marker)
                    marker.tag = bee
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationPermissionGranted = true
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
            updateMap()
        } else {
            val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) {
                        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
                    } else {
                        locationPermissionGranted = false
                        showSnackbar(getString(R.string.give_permission))
                    }

                    updateMap()
                }

            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun updateMap() {
        markBees()
        if(locationPermissionGranted) {
            if(!mapStartingPointLaunched){
                moveMapToUser()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveMapToUser() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            fusedLocationProvider?.lastLocation?.addOnCompleteListener { getLocationTask ->
                val location = getLocationTask.result
                if (location != null) {
                    val center = LatLng(location.latitude, location.longitude)

                    val zoomLevel = 15f

                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))
                } else {
                    showSnackbar(getString(R.string.no_location))
                }
            }
        }

    }

    private fun showSnackbar (message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    companion object {

        @JvmStatic
        fun newInstance() = BeeMapFragment()

    }
}