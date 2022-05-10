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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.lang.RuntimeException
import java.util.*
/*private const val TAG = "MAP_FRAGMENT"

class BeeMapFragment : Fragment() {
    private val treeViewModel: BeeReportViewModel by lazy {
        ViewModelProvider(requireActivity()).get(BeeReportViewModel::class.java)
    }*/

   /* override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val recyclerView = inflater.inflate(R.layout.fragment_bee_map, container, false)

        if (recyclerView !is RecyclerView) {
            throw RuntimeException("MapView view should be Recycler View")
        }
        val trees = listOf<BeeReport>()
        val adapter = BeeRecyclerViewAdapter(beereport)
        recyclerView.layoutManager = LinearLayoutManager(context)



        treeViewModel.latestReports.observe(requireActivity()) { BeeReportList ->
            adapter.bees = BeeReportList
            adapter.notifyDataSetChanged()
        }

        return recyclerView
    }
}
*/
/*
private const val TAG = "MAP_FRAGMENT"

class BeeMapFragment : Fragment() {

    private var locationPermissionGranted = true
    private var movedMapToUsersLocation = false
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var map: GoogleMap? = null


    private val BeeReportViewModel: BeeReportViewModel by lazy {
        ViewModelProvider(requireActivity()).get(BeeReportViewModel::class.java)
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

            fusedLocationProvider =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        } else {
            val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

                    if (granted) {
                        Log.d(TAG, "User granted permission")
                        locationPermissionGranted = true
                        fusedLocationProvider =
                            LocationServices.getFusedLocationProviderClient(requireActivity())
                    } else {
                        Log.d(TAG, "User did not grant permission")
                        locationPermissionGranted = false
                        showSnackbar(getString(R.string.give_permission))
                    }

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

    companion object {
        @JvmStatic
        fun newInstance() = BeeMapFragment()
    }
}

*/
