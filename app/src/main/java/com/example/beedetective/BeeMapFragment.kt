package com.example.beedetective

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.util.*

private const val TAG = "MAP_FRAGMENT"

class BeeMapFragment : Fragment() {
    private lateinit var addBeeButton: FloatingActionButton
    private var locationPermissionGranted = true
    private var movedMapToUsersLocation = false
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var map: GoogleMap? = null


    private val BeeReportViewModel: BeeReportViewModel by lazy {
        ViewModelProvider(requireActivity()).get(BeeReportViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        Log.d(TAG, "Google map ready")
        map = googleMap
    }

    private fun updateMap() {


        if (locationPermissionGranted) {
            if (!movedMapToUsersLocation) {
                moveMapToUserLocation()
            }
        }

    }

    private fun setAddBeeButtonEnabled(isEnabled: Boolean) {
        addBeeButton.isClickable = isEnabled
        addBeeButton.isEnabled = isEnabled

        if (isEnabled) {
            addBeeButton.backgroundTintList = AppCompatResources.getColorStateList(
                requireActivity(),
                android.R.color.holo_orange_light
            )
        } else {
            addBeeButton.backgroundTintList = AppCompatResources.getColorStateList(
                requireActivity(),
                android.R.color.darker_gray
            )
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
            setAddTreeButtonEnabled(true)
            fusedLocationProvider =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        } else {
            val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

                    if (granted) {
                        Log.d(TAG, "User granted permission")
                        setAddTreeButtonEnabled(true)
                        locationPermissionGranted = true
                        fusedLocationProvider =
                            LocationServices.getFusedLocationProviderClient(requireActivity())
                    } else {
                        Log.d(TAG, "User did not grant permission")
                        setAddTreeButtonEnabled(false)
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

        addBeeButton = mainView.findViewById(R.id.add_photo)
        addBeeButton.setOnClickListener {

            // addBeeLocation()

        }
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)

        setAddTreeButtonEnabled(false)

        requestLocationPermission()

    return mainView
}
    companion object {
        @JvmStatic
        fun newInstance() = BeeMapFragment()
    }
    }

       /* @SuppressLint("MissingPermission")
        fun addBeeLocation() {


            if (map == null) {
                return
            }
            if (fusedLocationProvider == null) {
                return
            }
            if (!locationPermissionGranted) {
                showSnackbar(getString(R.string.grant_location_permission))
                return
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
        }

    }

}*/

   /*         fusedLocationProvider?.lastLocation?.addOnCompleteListener(requireActivity()) { locationRequestTask ->
                val location = locationRequestTask.result
                if (location != null) {
                    getBee { BeeInfo ->
                        val bee = BeeInfo(
                            dateSpotted = Date(),
                            location = GeoPoint(location.latitude, location.longitude)
                        )
                        BeeReportViewModel.addBee(bee)

                        moveMapToUserLocation()
                        showSnackbar(getString(R.string.added_bee_report))
                    }
                } else {
                    showSnackbar(getString(R.string.no_location))
                }
            }
*/

/*    companion object {
            @JvmStatic
            fun newInstance() = BeeMapFragment()
        }
   */