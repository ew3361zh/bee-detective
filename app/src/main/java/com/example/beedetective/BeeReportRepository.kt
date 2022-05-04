package com.example.beedetective

// a list of things a beeReportRepository needs to be able to do
// in this app, FirebaseBeeReportRepository implements this interface,

interface BeeReportRepository {
    fun addReport(beeReport: BeeReport)
    fun deleteReport(beeReport: BeeReport)
    fun observeReports(function: (List<BeeReport>) -> Unit)
    fun stopObservingReports()
    // any other functions we need to do with a beeReport?
}