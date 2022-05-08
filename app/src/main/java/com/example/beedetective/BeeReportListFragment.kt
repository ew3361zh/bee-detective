package com.example.beedetective

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

private const val TAG = "ReportListFRAG"

class ReportListFragment: Fragment() {

    private lateinit var beeReportViewModel: BeeReportViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val application = requireActivity().application as BeeReportApplication

        beeReportViewModel = BeeReportViewModel.ReportViewModelFactory(application.beeReportRepository)
            .create(BeeReportViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val recyclerView = inflater.inflate(R.layout.fragment_bee_report_feed, container,
            false)

        if (recyclerView !is RecyclerView) {
            throw RuntimeException("ReportListFragment view should be Recycler View")
        }

        val reports = listOf<BeeReport>() // have some data before list arrives from firebase
        val adapter = ReportRecyclerAdapter(reports)  // okay to pass it an empty list

        Log.d(TAG, "Adapter has been made $adapter")
        // requireactivity() associates container activity for both fragments
        beeReportViewModel.latestReports.observe(requireActivity()) { reportList ->
            adapter.reports = reportList
            for (report in reportList) {
                Log.d(TAG, "List Frag getting a report usernote ${report.userNotes}")
            }
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReportListFragment()
    }

}