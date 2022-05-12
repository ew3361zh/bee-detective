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


    // Connects to the ViewModel using a factory class so that we can instantiate it in multiple fragments.
    private val beeReportViewModel: BeeReportViewModel by lazy {
        val app = requireActivity().application as BeeReportApplication
        BeeReportViewModel.ReportViewModelFactory(app.beeReportRepository)
            .create(BeeReportViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->

        // Holds the Map object.
        map = googleMap
        // Possibly unneeded just commented out for now to be sure.
//        googleMap.setOnInfoWindowClickListener { marker ->
//            val markerForBee = marker.tag as BeeReport
//
//        }
        // When the map is ready we update the map with the markers.
        updateMap()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bee_map, container, false)

        // Sets the mapFragment with the Map Object when it is ready.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        // Checks for location permissions in case they are not enabled already from the report fragment.
        requestLocationPermission()

        // Grabs the latest reports from the BeeViewModel.
        beeReportViewModel.latestReports.observe(requireActivity()) { recentBees ->
            beeSightingsList = recentBees
            markBees()
        }

        return view
    }

    private fun markBees() {
        // If we do not have a map object we return so that there is not a crash.
        if (map == null) {return}


        for (marker in beeMarkers) {
            marker.remove()
        }

        // Gets the location data from the BeeReport objects in our list.
        for (bee in beeSightingsList) {
            bee.location?.let { geoPoint ->
                // val isUsers = could be used to mark user submitted reports.

                // Using the location we draw a small bee.
                val beeIcon = R.drawable.bee_marker_ver2
                // Set Option to the marker. this makes it so you will see a user note if available and the
                // date sighted.
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

    // Handles checking location permissions.
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationPermissionGranted = true
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
            updateMap()
        } else {
            // Launches an intent to get location permissions if they are disabled.
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
        // Mark bees and checks location permisions.
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
            // Unlike the reportFragment we use lastLocation here because we are less concerned
            // we a users location in this fragment.
            fusedLocationProvider?.lastLocation?.addOnCompleteListener { getLocationTask ->
                val location = getLocationTask.result
                if (location != null) {
                    val center = LatLng(location.latitude, location.longitude)

                    val zoomLevel = 15f
                    // Moves the camera to their last location if available.
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

        // Instantiates new fragment object.
        @JvmStatic
        fun newInstance() = BeeMapFragment()

    }
}