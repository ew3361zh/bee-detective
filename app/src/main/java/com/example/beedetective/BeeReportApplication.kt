package com.example.beedetective

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BeeReportApplication: Application() {

    lateinit var beeReportRepository: BeeReportRepository

    override fun onCreate() {
        super.onCreate()

        // FROM CLARA:

        // initialize Firebase in onCreate, the Application
        // needs to be created and the context needs to exist,
        // or this call will fail since the application context
        // is needed to initialize Firebase.


        // If we wanted to replace the Firebase store with another data
        // store, such as a database, or an API, then create a new class that
        // implements BeeReportRepository and provides appropriate functions that
        // store, retrieve, delete etc. data
        // then replace these line to a call to initialize this BeeReportRepository

        val db = Firebase.firestore
        beeReportRepository = FirebaseBeeReportRepository(db)
    }
}