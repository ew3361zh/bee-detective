package com.example.beedetective

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.util.*

data class BeeReport (
                   // TODO implement getting/using UUID
                    // val user: UUID, // better choice for tying a user to a report?
                      val dateReported: Date? = null, // maybe not allow null?
                      val location: GeoPoint? = null, // also not allow null?
                      val userNotes: String? = null, // anything user wants to say about report
                      // something for photo - imageFileName, photoUri

                      // regular field in a beeReport object - code will be able to get/set it
                      // ignored by firebase for getting/setting
                      @get:Exclude @set:Exclude var documentReference: DocumentReference? = null
                      )
