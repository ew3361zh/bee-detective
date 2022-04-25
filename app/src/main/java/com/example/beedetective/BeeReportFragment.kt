package com.example.beedetective

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class BeeReportFragment : Fragment() {

    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var takePictureFab: FloatingActionButton
    private lateinit var submitFab: FloatingActionButton
    private lateinit var dateEditButton: ImageButton
    private lateinit var locationEditButton: ImageButton
    
    private lateinit var imagePath: Uri
    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            checkImage(result)
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bee_report, container, false)
//        createImageFile()

        dateTextView = view.findViewById(R.id.date_textView)
        takePictureFab = view.findViewById(R.id.picture_fab)

        val currentDate = Calendar.getInstance().time
        val currentDateFormat = SimpleDateFormat("yy-MM-dd, hh:mm aa", Locale.getDefault())
        dateTextView.text = currentDateFormat.format(currentDate)

        takePictureFab.apply {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            setOnClickListener {
                cameraActivityLauncher.launch(takePictureIntent)
            }

        }

        return view
    }

//    private fun createImageFile() {
//            val uniqueId = UUID.randomUUID().toString()
//            val imageFileName = "BEE_$uniqueId"
//            val filesDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val imageFile = File.createTempFile(imageFileName, ".jpg", filesDir)
//            imagePath = FileProvider.getUriForFile(requireActivity(),
//                "com.example.beedetective.fileprovider",
//                imageFile)
//    }

    private fun updatePhotoView() {
        TODO("Not yet implemented")
    }

    private fun checkImage(result: ActivityResult?) {
        TODO("Not yet implemented")
    }

    companion object {

        @JvmStatic
        fun newInstance() = BeeReportFragment()

    }
}