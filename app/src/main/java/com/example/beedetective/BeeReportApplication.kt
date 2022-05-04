package com.example.beedetective

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BeeReportApplication: Application() {

    lateinit var beeReportRepository: BeeReportRepository

    override fun onCreate() {
        super.onCreate()

        // initialize Firebase in onCreate, the Application
        // needs to be created and the context needs to exist,
        // or this call will fail since the application context
        // is needed to initialize Firebase.

        val db = Firebase.firestore
        beeReportRepository = FirebaseBeeReportRepository(db)
    }
}