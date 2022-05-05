package com.example.beedetective
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportRecylerAdapter(var reports: List<BeeReport>): RecylerView.Adapter<ReportRecylerAdapter.ViewHolder>() {


    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(report: BeeReport) {
            view.findViewById<TextView>(R.id.tree_name).text = tree.name
            view.findViewById<TextView>(R.id.date_spotted).text = "${tree.dateSpotted}"
            // this is where we would find a piece of the tree fragment list item and set up a listener for it

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_tree_list_item, parent, false)
        return ViewHolder(view)
    }

    // getting data for item in the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tree = trees[position]
        holder.bind(tree)
    }

    override fun getItemCount(): Int {
        return trees.size
    }


}