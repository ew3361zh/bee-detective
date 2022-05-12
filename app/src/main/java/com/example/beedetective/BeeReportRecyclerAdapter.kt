package com.example.beedetective
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Callback
import java.lang.Exception
import kotlin.math.absoluteValue

private const val TAG = "RECYCLER_ADAPTER"

// You can use integer values,
//const val ONLY_PHOTO = 0
//const val ONLY_TEXT = 1
//const val PHOTO_AND_TEXT = 2

// or an enum,
enum class ReportType(val value: Int) {
    ONLY_PHOTO(0),
    ONLY_TEXT(1),
    PHOTO_AND_TEXT(2)
}

class ReportRecyclerAdapter(var reports: List<BeeReport>):
    RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder>() {


    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(report: BeeReport) {
            // handling different amounts of data from different reports being displayed differently
            // in fragment_bee_report_feed
            view.findViewById<TextView>(R.id.date_spotted).text = "${report.dateReported}"

            if (!report.userNotes.isNullOrBlank()) {
                view.findViewById<TextView>(R.id.usernotes).text = report.userNotes
            }

            // url link to photo in firebase storage
            if (report.photoName != null) {
                val photoHttpPathReference =
                    "https://firebasestorage.googleapis.com/v0/b/beedetective-af1e2.appspot.com/o/images%2F" +
                            report.photoName.toString() + "?alt=media&token=c0d00bf1-f1f2-467a-82cf-2ce522068cdb"
                Log.d(TAG, "photo path is $photoHttpPathReference")
                Picasso.get()
                    .load(photoHttpPathReference)
                    .error(android.R.drawable.stat_notify_error) // displayed if issue with loading image
                    .fit()
                    .centerCrop()
                    .into(view.findViewById<ImageView>(R.id.bee_photo), object: Callback {
                        override fun onSuccess() {
                            Log.d(TAG, "successfully loaded $photoHttpPathReference")
                        }

                        override fun onError(e: Exception?) {
                            Log.e(TAG, "error loading $photoHttpPathReference", e)
                        }
                    })
                // Add the error listener to log a message if the photo can't be loaded,
                // may help determine the cause of images that don't load
            }
        }
    }

    // a set of three different report feed items based on the data they contain - accounts
    // for no notes but with photo, no photo but with notes, and with both notes and photo
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = when (viewType) {
            ReportType.PHOTO_AND_TEXT.value -> LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bee_report_feed_item, parent, false)
            ReportType.ONLY_PHOTO.value -> LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bee_report_no_notes_feed_item, parent, false)
            // best to be specific about each option, instead of this being the default
            ReportType.ONLY_TEXT.value -> LayoutInflater.from(parent.context) //TODO adjust no photo layout. maybe include a icon or logo.
                .inflate(R.layout.fargment_bee_report_no_photo_feed_item, parent, false)
            // else is necessary if you use Int values. It wouldn't be necessary if the enum is used directly
            // but since you are working with the int viewType you would use the else.

            else -> throw IllegalStateException("Invalid view type $viewType")

        }
        return ViewHolder(view)
    }

    // getting data for item in the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    // not used but a required function for a recycler adapter
    override fun getItemCount(): Int {
        return reports.size
    }

    // Determines what view we should display in the recycler.
    override fun getItemViewType(position: Int): Int {
        val report = reports[position]

        return when {
            report.photoName == null -> ReportType.ONLY_TEXT.value // i.e. user has not taken a photo
            report.userNotes.isNullOrBlank() -> ReportType.ONLY_PHOTO.value // user has not added notes
            else -> ReportType.PHOTO_AND_TEXT.value // user has added both notes and photo
        }
    }


}