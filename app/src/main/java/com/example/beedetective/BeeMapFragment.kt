package com.example.beedetective

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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



private const val TAG = "BEE_MAP_FRAGMENT"

class BeeMapFragment : Fragment() {
    private var locationPermissionGranted = false
    private var movedMapToUsersLocation = false
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val beeMarker = mutableListOf<Marker>()
    private var map: GoogleMap? = null
    private var reportList = listOf<BeeReport>()

    private val BeeReportViewModel: BeeReportViewModel by lazy {
            ViewModelProvider(requireActivity()).get(BeeReportViewModel::class.java)
        }


        private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        Log.d(TAG, "Google map ready")
        map = googleMap
        updateMap()
    }

    private fun updateMap() {
        if (locationPermissionGranted) {
            if (!movedMapToUsersLocation) {
                moveMapToUserLocation()
            }

            viewReport()
        }
    }
    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            Log.d(TAG, "permission already granted")
            updateMap()
            fusedLocationProvider =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        } else {
            val requestLocationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->

                if (granted) {
                    Log.d(TAG, "User granted permission")
                    locationPermissionGranted = true
                    //TODO add the report lis here
                    fusedLocationProvider =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                } else {
                    Log.d(TAG, "User did not grant permission")
                    //TODO set the report list to false and if not location access granted do not show lsit
                    locationPermissionGranted = false
                    showSnackbar(getString(R.string.give_permission))
                }

                updateMap()
            }
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveMapToUserLocation() {
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
                    Log.d(TAG, "User's location $location")
                    val center = LatLng(location.latitude, location.longitude)
                    val zoomLevel = 8f
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))
                    movedMapToUsersLocation = true
                } else {
                    showSnackbar(getString(R.string.no_location))
                }
            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_bee_map, container, false)
        //TODO add clickable icon view
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        requestLocationPermission()
        BeeReportViewModel.latestReports.observe(requireActivity()) { latestReports ->
            reportList = latestReports
            viewReport()
    }

        return mainView
    }
    private fun viewReport() {
        if (map == null) {
            return
        }
        for (bees_report in reportList) {
            val iconId = R.drawable.bee_icon
            bees_report.location?.let { geoPoint ->
                val markerOptions = MarkerOptions()
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .snippet("Spotted on ${bees_report.dateReported}")
                    .icon(BitmapDescriptorFactory.fromResource(iconId))

            }
        }

    }
    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =BeeMapFragment()
            }
    }
