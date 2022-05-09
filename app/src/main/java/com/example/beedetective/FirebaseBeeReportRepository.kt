package com.example.beedetective

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

private const val TAG = "FB_REPORT_REPOSITORY"

class FirebaseBeeReportRepository(db: FirebaseFirestore): BeeReportRepository {

    // Firebase-specific code. No references to any other parts of the project.
    private val reportCollectionReference = db.collection("reports")

    // keep a reference to the listener so can stop listening when not needed.
    private var reportCollectionListener: ListenerRegistration? = null

    override fun addReport(beeReport: BeeReport) {
        reportCollectionReference.add(beeReport)
            .addOnSuccessListener { reportDocumentReference ->
                Log.d(TAG, "Added report document $reportDocumentReference")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error adding report $beeReport", error)
            }

    }

    // TODO determine if we will allow deleted reports by users (maybe just from feed and not from db)
    // --> could add boolean field to report object "deletedByUser" and not display those in the feed
    override fun deleteReport(beeReport: BeeReport) {
        beeReport.documentReference?.delete()
    }

    override fun observeReports(notifyObserver: (List<BeeReport>) -> Unit) {
        reportCollectionListener = reportCollectionReference
            .orderBy("dateReported", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting latest reports", error)
                }
                if (snapshot != null) {
                    // Simplest way - convert the snapshot to bee report objects.
                    // However, we want to store the report references so we'll need to loop and
                    // convert, and add the document references
                    Log.d(TAG, "Snapshot is ${snapshot.size()}")
                    val reports = mutableListOf<BeeReport>()
                    for (reportDocument in snapshot) {
                        val report = reportDocument.toObject(BeeReport::class.java)
                        report.documentReference = reportDocument.reference
                        // report should have photoName to eventually pull photo from Firebase Storage
                        reports.add(report)
                        Log.d(TAG, "Report from firebase: $report")
                    }
                    Log.d(TAG, "Reports from firebase: $reports")
                    notifyObserver(reports) // notifyReports is callback function name of this override function observeReports
                }
            }
    }

    override fun stopObservingReports() {
        Log.d(TAG, "Removing listener")
        reportCollectionListener?.remove()
    }
}