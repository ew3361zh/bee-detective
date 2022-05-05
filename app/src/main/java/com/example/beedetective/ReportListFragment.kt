package com.example.beedetective

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

class ReportListFragment: Fragment() {

    private val beeReportViewModel: BeeReportViewModel by lazy {
        val app = requireActivity().application as BeeReportApplication
        BeeReportViewModel.ReportViewModelFactory(app.beeReportRepository)
            .create(BeeReportViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val recyclerView = inflater.inflate(R.layout.fragment_report_list, container, false)

        if (recyclerView !is RecyclerView) {
            throw RuntimeException("TreeListFragment view should be Recycler View")
        }

        val trees = listOf<BeeReport>() // have some data before list arrives from firebase
        val adapter = ReportRecylerAdapter(trees) { tree, isFavorite ->
            treeViewModel.setIsFavorite(tree, isFavorite)
        } // okay to pass it an empty list
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // requireactivity associates container activity for both fragments
        treeViewModel.latestTrees.observe(requireActivity()) { treeList ->
            adapter.trees = treeList
            adapter.notifyDataSetChanged()
        }

        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReportListFragment()
    }

}