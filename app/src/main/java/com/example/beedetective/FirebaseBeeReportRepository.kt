package com.example.beedetective

import android.util.Log
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
                // Clara has a tree.documentReference = treeDocumentReference line here she doesn't use
                // also is part of the tree data class
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error adding report $beeReport", error)
            }

    }

    override fun deleteReport(beeReport: BeeReport) {
        beeReport.documentReference?.delete()
    }

    override fun observeReports(function: (List<BeeReport>) -> Unit) {
        reportCollectionListener = reportCollectionReference
            .orderBy("dateSpotted", Query.Direction.ASCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting latest trees", error)
                }
                if (snapshot != null) {
                    // FROM CLARA
                    // Simplest way - convert the snapshot to bee report objects.
                    // val reports = snapshot.toObjects(Report::class.java)

                    // However, we want to store the report references so we'll need to loop and
                    // convert, and add the document references

                    val reports = mutableListOf<BeeReport>()
                    for (reportDocument in snapshot) {
                        val report = reportDocument.toObject(BeeReport::class.java)
                        report.documentReference = reportDocument.reference
                        reports.add(report)
                    }
                    Log.d(TAG, "Reports from firebase: $reports")
//                    notifyObserver(reports) --> code from Clara not working here
                }
            }
    }

    override fun stopObservingReports() {
        Log.d(TAG, "Removing listener")
        reportCollectionListener?.remove()
    }
}