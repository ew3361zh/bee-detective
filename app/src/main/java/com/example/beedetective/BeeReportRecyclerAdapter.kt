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

private const val TAG = "RECYCLER_ADAPTER"

class ReportRecyclerAdapter(var reports: List<BeeReport>):
    RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder>() {


    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(report: BeeReport) {
            view.findViewById<TextView>(R.id.date_spotted).text = "${report.dateReported}"
            // todo user notes can be blank, should we add default value for note?

            if (!report.userNotes.isNullOrBlank()) {
                view.findViewById<TextView>(R.id.usernotes).text = report.userNotes
            }

            // url link to photo in firebase storage
            if (report.photoName != null) {
                val photoHttpPathReference =
                    "https://firebasestorage.googleapis.com/v0/b/beedetective-af1e2.appspot.com/o/images%2F" +
                            report.photoName.toString() + "?alt=media&token=c0d00bf1-f1f2-467a-82cf-2ce522068cdb"
                Log.d(TAG, "photo path is $photoHttpPathReference")
                // todo what if user doesn't take photo or photo can't load - maybe have default bee photo inserted
                // todo ideally would only get reports with photos but maybe important to have location data
                //  which we could use to launch a map intent?
                Picasso.get()
                    .load(photoHttpPathReference)
                    .error(android.R.drawable.stat_notify_error) // displayed if issue with loading image
                    .fit()
                    .centerCrop()
                    .into(view.findViewById<ImageView>(R.id.bee_photo))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            2 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bee_report_feed_item, parent, false)
            1 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bee_report_no_notes_feed_item, parent, false)
            else -> LayoutInflater.from(parent.context) //TODO adjust no photo layout. maybe include a icon or logo.
                .inflate(R.layout.fargment_bee_report_no_photo_feed_item, parent, false)
        }
        return ViewHolder(view)
    }

    // getting data for item in the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    // Determines what view we should display in the recycler.
    override fun getItemViewType(position: Int): Int {
        val report = reports[position]

        return when {
            report.photoName == null -> 0
            report.userNotes.isNullOrBlank() -> 1
            else -> 2
        }
    }


}