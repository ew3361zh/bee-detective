package com.example.beedetective

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// beeReportRepository is an interface with all the functions implemented for Firebase in FirebaseBeeReportRepository

private const val TAG = "REPORT_VIEW_MODEL"

class BeeReportViewModel(private val beeReportRepository: BeeReportRepository): ViewModel() {

    // Provide methods that Fragments, Activities can call
    // and pass requests onto the BeeReportRepository.
    // No Firebase-related code, or any code that specifically refers
    // to any data store.

    val latestReports = MutableLiveData<List<BeeReport>>()

    init {
        beeReportRepository.observeReports { reports ->
            latestReports.postValue(reports)
            for (report in reports) {
                Log.d(TAG, "Report note is ${report.userNotes}")
            }
        }
    }

    fun addReport(beeReport: BeeReport) {
        beeReportRepository.addReport(beeReport)
    }

    fun deleteReport(beeReport: BeeReport) {
        beeReportRepository.deleteReport(beeReport)
    // TODO we want to allow user to delete report from feed but not from db
    //  would be ideal if we could tag a deleted report from user in the db though
    }

    override fun onCleared() {
        super.onCleared()
        beeReportRepository.stopObservingReports()
        Log.d(TAG, "Removing listener")
    }

    class ReportViewModelFactory(private val beeReportRepository: BeeReportRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BeeReportViewModel::class.java)) {
                return BeeReportViewModel(beeReportRepository) as T
            }
            throw IllegalArgumentException("$modelClass needs to be a BeeReportRepository")
        }
    }




}