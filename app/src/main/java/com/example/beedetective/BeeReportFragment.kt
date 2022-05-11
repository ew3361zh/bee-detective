package com.example.beedetective

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.*
import com.google.firebase.storage.ktx.storage
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Bee-Report-Fragment"
private const val LTAG = "Bee-Location-Service"

class BeeReportFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // Links to BeeReportViewModel
    private val beeReportViewModel: BeeReportViewModel by lazy {
        val app = requireActivity().application as BeeReportApplication
        // allows instantiates of the viewModel to other fragments
        BeeReportViewModel.ReportViewModelFactory(app.beeReportRepository)
            .create(BeeReportViewModel::class.java)
    }

    private lateinit var dateTextView: TextView
    private lateinit var userNotesTextView: EditText
    private lateinit var takePictureFab: FloatingActionButton
    private lateinit var submitFab: FloatingActionButton
    private lateinit var dateEditButton: ImageButton

    private lateinit var beeImageView: ImageView
    private lateinit var reportProgressBar: ProgressBar

    // need to ask app if its been granted permission to user location
    private var locationPermissionGranted = false

    // get user's location - requires adding dependency and Gradle sync after importing this
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var imageUri: Uri? = null
    private var imageFileName: String? = null
    private var newImagePath: String? = null
    private var visibleImagePath: String? = null
    // asks to launch a camera app.
    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            checkImage(result)
        }

    // Links to firebase Storage and holds image paths for saved instance state.
    private val storage = Firebase.storage
    private val NEW_BEE_IMAGE_PATH_KEY = "new bee image path key"
    private val VISABLE_BEE_IMAGE_PATH_KEY = "current visible bee image path key"

    // Holds default int values to be read by the Calendar and if the application cannot access
    // the system calendar it should still run.
    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    // Holds modified int values passed by the user through the alert dialogs.
    // Future modification would move date info to the view model.
    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0


    var currentCalendar = Calendar.getInstance()
    // Sets the date format for the Date information to be displayed for the user.
    private val currentDateFormat = SimpleDateFormat("yy-MM-dd, hh:mm aa", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // reads the image paths from the saved instance state passed when the life cycle is interrupted.
        newImagePath = savedInstanceState?.getString(NEW_BEE_IMAGE_PATH_KEY)
        visibleImagePath = savedInstanceState?.getString(VISABLE_BEE_IMAGE_PATH_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bee_report, container, false)
        // Wires up buttons and views.
        dateEditButton = view.findViewById(R.id.edit_date_button)
        dateTextView = view.findViewById(R.id.date_textView)
        takePictureFab = view.findViewById(R.id.picture_fab)
        beeImageView = view.findViewById(R.id.bee_image_holder)
        submitFab = view.findViewById(R.id.submit_fab)
        reportProgressBar = view.findViewById(R.id.reportProgressBar)

        userNotesTextView = view.findViewById(R.id.usernotes_textView)
        // Gets the current date formats and displays it.
        dateTextView.text = currentDateFormat.format(currentCalendar.time)
        
        dateEditButton.setOnClickListener {
            // Calls the pickDate function which is an alert dialog.
            pickDate()
        }
        
        takePictureFab.setOnClickListener {
            // Calls the take picture function to launch the camera intent.
            takePicture()

        }

        submitFab.setOnClickListener {
            // Calls the upload image and report that passes BeeReport info to fireBase.
            uploadImageAndReport()
            // Sets progress bar to visable when a report is being uploaded.
            reportProgressBar.visibility = View.VISIBLE
        }

        // Asks for users location.
        requestLocationPermission()

        return view
    }

    private fun createImageFile(): Pair<File?, String?>{
        try {
            // Sets a unique identifier calling a random UUID and turning it into a string.
            val uniqueId = UUID.randomUUID().toString()
            // Concats the unique identifier unto our BEE_ string to create a unique image name.
            imageFileName = "BEE_$uniqueId"
            // Gets a link to the local device storage to store the image on the device.
            val filesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            // Holds image name and storage location
            val imageFile = File.createTempFile(imageFileName, ".jpg", filesDir)
            // joins the file name and location as a image path.
            val imagePath = imageFile.absolutePath

            return imageFile to imagePath

        } catch (ex: IOException) {
            return null to null
        }
    }

    private fun updatePhotoView(imageView: ImageView, imagePath: String) {
        // Calls Picasso to find our Image from the path we set and scales and loads it to our screen
        // for the user to preview.
        Picasso.get()
            .load(File(imagePath))
            .error(android.R.drawable.stat_notify_error)
            .fit()
            .centerCrop()
            .into(imageView, object:
                Callback {
                override fun onSuccess() { Log.d("REPORT", "loaded image $imagePath") }
                override fun onError(e: Exception?) { Log.e("REPORT", "Error loading image $imagePath", e) }
            })

    }

    // Only sets to false and disables the report button if the users location is not available.
    private fun setSubmitReportButtonEnabled(isEnabled: Boolean) {
        submitFab.isClickable = isEnabled
        submitFab.isEnabled = isEnabled

        if (isEnabled) {
            submitFab.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.holo_orange_light)
        } else {
            submitFab.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.darker_gray)
        }

    }


    // Checks the camera activity result to see if a picture was taken, if not we log a report.
    private fun checkImage(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                visibleImagePath =  newImagePath
                updatePhotoView(beeImageView, visibleImagePath!!)
            }
            RESULT_CANCELED -> {
                Log.d("REPORT", "Result cancelled, no picture taken.")
            }
        }
    }

    // Calls our camera intent
    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Takes the image path and image name which our returned by our CreateImageFile function.
        val (imageFile, imageFilePath) = createImageFile()
        // Checks to make sure there is an image.
        if (imageFile != null) {
            // This is a reference to the image file.
            newImagePath = imageFilePath
            imageUri = FileProvider.getUriForFile(requireActivity().applicationContext,
                "com.example.beedetective.fileprovider",
                imageFile) // Creates a URI for the new image file that was created.

            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            // We now create a file to write the image to and store the location of where that file is on the device
            // and then provide that information as an extra to the native camera app.
            cameraActivityLauncher.launch(takePictureIntent)
        }

    }


    @SuppressLint("MissingPermission")
    private fun uploadImageAndReport() {

        // starting with needing user location
        if (fusedLocationProvider == null) {
            Log.d(LTAG, "Fused location provider is null")
            return
        }
        if(!locationPermissionGranted) {
            showSnackbar(getString(R.string.grant_location_permission))
            Log.d(LTAG, "Location permission not granted")
            return
        }

        class CT: CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                Log.d(TAG, "cancellation requested")
                return this
            }

            override fun isCancellationRequested(): Boolean {
                Log.d(TAG, "cancellation requested")
                return false;  // same
            }
        }

        fusedLocationProvider?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, CT())?.addOnCompleteListener(requireActivity()){ locationRequestTask ->
            val location = locationRequestTask.result
            Log.d(LTAG, "Location variable is $location")
            // If location is available we create a new BeeReport Data object to store.
            if(location !=null) {
                    val beeReport = BeeReport(
                        dateReported = currentCalendar.time,
                        location = com.google.firebase.firestore.GeoPoint(
                            location.latitude,
                            location.longitude
                        ),
                        userNotes = userNotesTextView.text.toString(),
                        photoName = imageFileName
                    )
                // uses our addReport method to upload the collected bee information to Firebase.
                    beeReportViewModel.addReport(beeReport)
                // Calls our uploadImage function to upload the image to firebase storage.
                    uploadImage()
                // clear user notes from just-uploaded report
                    userNotesTextView.setText("")
                    if (!imageFileName.isNullOrEmpty()) {
                        Log.d(TAG, "${storage.getReference(imageFileName!!)} exists")
                    } else {
                        Log.e(TAG, "imageFileName is null")
                    }
                    showSnackbar(getString(R.string.added_bee_report))

            } else {
                showSnackbar(getString(R.string.no_location))
            }
        }
    }


    private fun uploadImage() {
        // Checks to see if there is an image path and image file to upload.
        if (imageUri != null && imageFileName != null) {


            // Grabs a refrence to firebase storage.
            val imageStorageRootReference = storage.reference
            val imageCollectionReference = imageStorageRootReference.child("images")
            val imageFileReference = imageCollectionReference.child(imageFileName!!)
            // Uploads the file path and image to Storage
            imageFileReference.putFile(imageUri!!).addOnCompleteListener {
                Snackbar.make(requireView(), "Bee report uploaded.", Snackbar.LENGTH_SHORT).show()
                // Once the image is uploaded the hides progress bar.
                reportProgressBar.visibility = View.GONE
                beeImageView.setImageResource(R.drawable.blue_sky_harry_cooke)
            }
                // Handles error result of failed upload.
                .addOnFailureListener { error ->

                    Snackbar.make(requireView(), "Error uploading image", Snackbar.LENGTH_LONG)
                        .show()

                    Log.e(TAG, "error uploading bee report $imageFileName", error)
                    reportProgressBar.visibility = View.GONE
                }
        } else {
            // If no image a we still need to reset the reportProgressBar visibility.
            reportProgressBar.visibility = View.GONE
        }
    }

    // Holds references to the image path in case of rotation.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NEW_BEE_IMAGE_PATH_KEY, newImagePath)
        outState.putString(VISABLE_BEE_IMAGE_PATH_KEY, visibleImagePath)
    }

    // Requests location permission in the report fragment since it is the default fragment launched.
    private fun requestLocationPermission() {
        // has user already granted permission?
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // if location permission already granted, turn on add tree button, and initialize locaiton provider
            locationPermissionGranted = true
            Log.d(TAG, "permission already granted")
            setSubmitReportButtonEnabled(true)
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
        } else {
            // need to ask for permission
            val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                // this creates the launcher to ask for permission to use location
                if (granted) {
                    Log.d(TAG, "User granted permission")
                    locationPermissionGranted = true
                    setSubmitReportButtonEnabled(true)
                    fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
                } else {
                    Log.d(TAG, "User did not grant permission")
                    setSubmitReportButtonEnabled(false)
                    locationPermissionGranted = false
                    showSnackbar(getString(R.string.give_permission))
                }

            }
            // launching specific permission request
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    // This launches a DatePickerDialog with the current date values.
    private fun pickDate() {
        currentCalender()
        DatePickerDialog(requireActivity(), this, year, month, day).show()
    }

    private fun currentCalender() {
        // Sets our currentDate Values.
        year = currentCalendar.get(Calendar.YEAR)
        month = currentCalendar.get(Calendar.MONTH)
        day = currentCalendar.get(Calendar.DAY_OF_MONTH)
        hour = currentCalendar.get(Calendar.HOUR)
        minute = currentCalendar.get(Calendar.MINUTE)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Reads the new values picked in the alert dialog
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year
        // Launches a second dialog once the first is completed
        TimePickerDialog(requireActivity(), this, hour, minute, false).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        // Reads in a new hour and minute.
        savedHour = hour
        savedMinute = minute
        // Sets the new saved date data to be displayed by the report fragment.
        currentCalendar.set(savedYear, savedMonth, savedDay, savedHour, savedMinute)
        dateTextView.text = currentDateFormat.format(currentCalendar.time)

    }

    // instantiates new object.
    companion object {
        fun newInstance() = BeeReportFragment()
    }


}