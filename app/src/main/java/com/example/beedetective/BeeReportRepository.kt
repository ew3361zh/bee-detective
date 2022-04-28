package com.example.beedetective

// From CLARA:

// a list of things a beeReportRepository needs to be able to do
// in this app, FirebaseBeeReportRepository implements this interface,
// but a class that connects to a database, or an API, could implement this interface,
// and all we'd need to do is swap out the initialization  in TreeApplication

interface BeeReportRepository {
    fun addReport(beeReport: BeeReport)
    fun deleteReport(beeReport: BeeReport)
    fun observeReports(function: (List<BeeReport>) -> Unit)
    fun stopObservingReports()
    // any other functions we need to do with a beeReport?
}