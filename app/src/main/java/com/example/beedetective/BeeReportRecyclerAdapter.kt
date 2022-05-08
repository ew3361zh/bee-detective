package com.example.beedetective
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

private const val TAG = "RECYCLER_ADAPTER"

class ReportRecyclerAdapter(var reports: List<BeeReport>):
    RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder>() {


    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(report: BeeReport) {
            view.findViewById<TextView>(R.id.date_spotted).text = "${report.dateReported}"
//            Log.d(TAG, "date report is ${report.dateReported}")
            view.findViewById<TextView>(R.id.usernotes).text = report.userNotes
//            Log.d(TAG, "usernotes are ${report.userNotes}")
            // url link to photo in firebase storage
            val photoPathReference = "gs://beedetective-af1e2.appspot.com/images/" + report.photoName.toString()
            Log.d(TAG, "photo path is $photoPathReference")
            Picasso.get()
                .load(photoPathReference)
                .error(android.R.drawable.stat_notify_error) // displayed if issue with loading image
                .fit()
                .centerCrop()
                .into(view.findViewById<ImageView>(R.id.bee_photo))

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bee_report_feed_item, parent, false)
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


}