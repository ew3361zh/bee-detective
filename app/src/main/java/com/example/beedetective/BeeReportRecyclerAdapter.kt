package com.example.beedetective
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportRecyclerAdapter(var reports: List<BeeReport>):
    RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder>() {


    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(report: BeeReport) {
            view.findViewById<TextView>(R.id.date_spotted).text = "${report.dateReported}"
            view.findViewById<TextView>(R.id.usernotes).text = report.userNotes
            view.findViewById<ImageView>(R.id.bee_photo)
            // this is where we would find a piece of the tree fragment list item and set up a listener for it

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