package com.example.beedetective

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Bee-Report-Fragment"

class BeeReportFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var takePictureFab: FloatingActionButton
    private lateinit var submitFab: FloatingActionButton
    private lateinit var dateEditButton: ImageButton
    private lateinit var locationEditButton: ImageButton
    private lateinit var beeImageView: ImageView
    private lateinit var reportProgressBar: ProgressBar

    
    private var imageUri: Uri? = null
    private var imageFileName: String? = null
    private var newImagePath: String? = null
    private var visibleImagePath: String? = null
    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            checkImage(result)
        }

    private val storage = Firebase.storage

    private val NEW_BEE_IMAGE_PATH_KEY = "new bee image path key"
    private val VISABLE_BEE_IMAGE_PATH_KEY = "current visible bee image path key"

    // TODO move to data class?
    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    var currentCalendar = Calendar.getInstance()
    private val currentDateFormat = SimpleDateFormat("yy-MM-dd, hh:mm aa", Locale.getDefault())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        newImagePath = savedInstanceState?.getString(NEW_BEE_IMAGE_PATH_KEY)
        visibleImagePath = savedInstanceState?.getString(VISABLE_BEE_IMAGE_PATH_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bee_report, container, false)
//        createImageFile()

        //TODO add progress bar, upload to firebase.
        dateEditButton = view.findViewById(R.id.edit_date_button)
        dateTextView = view.findViewById(R.id.date_textView)
        takePictureFab = view.findViewById(R.id.picture_fab)
        beeImageView = view.findViewById(R.id.bee_image_holder)
        submitFab = view.findViewById(R.id.submit_fab)
        reportProgressBar = view.findViewById(R.id.reportProgressBar)

        dateTextView.text = currentDateFormat.format(currentCalendar.time)

        dateEditButton.setOnClickListener {
            pickDate()
        }

        takePictureFab.setOnClickListener {
            takePicture()
        }

        submitFab.setOnClickListener {
            uploadImage()
        }

        return view
    }




    private fun createImageFile(): Pair<File?, String?>{
        try {
            val uniqueId = UUID.randomUUID().toString()
            imageFileName = "BEE_$uniqueId"
            val filesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(imageFileName, ".jpg", filesDir)
            val imagePath = imageFile.absolutePath

            return imageFile to imagePath

        } catch (ex: IOException) {
            return null to null
        }
    }

    private fun updatePhotoView(imageView: ImageView, imagePath: String) {
        Picasso.get()
            .load(File(imagePath))
            .error(android.R.drawable.stat_notify_error)
            .fit()
            .centerCrop()
            .into(imageView, object:
            Callback {
            override fun onSuccess() {
                Log.d("REPORT", "loaded image $imagePath")
            }

            override fun onError(e: Exception?) {
                Log.e("REPORT", "Error loading image $imagePath", e)
            }
        })

    }


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

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val (imageFile, imageFilePath) = createImageFile()
        if (imageFile != null) {
            // This is a refrence to the image file.
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

    private fun uploadImage() {
        if (imageUri != null && imageFileName != null) {

            reportProgressBar.visibility = View.VISIBLE

            val imageStorageRootReference = storage.reference
            val imageCollectionReference = imageStorageRootReference.child("images")
            val imageFileReference = imageCollectionReference.child(imageFileName!!)

            imageFileReference.putFile(imageUri!!).addOnCompleteListener {
                Snackbar.make(requireView(), "Bee report uploaded.", Snackbar.LENGTH_SHORT).show()
                reportProgressBar.visibility = View.GONE
            }
                .addOnFailureListener { error ->
                    Snackbar.make(requireView(), "Error uploading image", Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, "error uploading bee report $imageFileName", error)
                    reportProgressBar.visibility = View.GONE
                }
        } else {
            Snackbar.make(requireView(), "Take a picture first!", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NEW_BEE_IMAGE_PATH_KEY, newImagePath)
        outState.putString(VISABLE_BEE_IMAGE_PATH_KEY, visibleImagePath)
    }

    private fun pickDate() {
        currentCalender()

        DatePickerDialog(requireActivity(), this, year, month, day).show()
    }

    private fun currentCalender() {

        year = currentCalendar.get(Calendar.YEAR)
        month = currentCalendar.get(Calendar.MONTH)
        day = currentCalendar.get(Calendar.DAY_OF_MONTH)
        hour = currentCalendar.get(Calendar.HOUR)
        minute = currentCalendar.get(Calendar.MINUTE)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        TimePickerDialog(requireActivity(), this, hour, minute, false).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        savedHour = hour
        savedMinute = minute

        currentCalendar.set(savedYear, savedMonth, savedDay, savedHour, savedMinute)
        dateTextView.text = currentDateFormat.format(currentCalendar.time)

    }

    companion object {

        @JvmStatic
        fun newInstance() = BeeReportFragment()

    }


}