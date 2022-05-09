package com.example.beedetective

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.util.*

data class BeeReport (val dateReported: Date? = null, // maybe not allow null?
                                                      // todo check what happens in list fragment if eliminate report date in firebase
                      val location: GeoPoint? = null, // also not allow null?
                      val userNotes: String? = null, // anything user wants to say about report, definitely okay if null
                      val photoName: String? = null,
                      // TODO implement getting/using UUID - app uniquely identifies user and ties to reports

                      // regular field in a beeReport object - code will be able to get/set it
                      // ignored by firebase for getting/setting
                      @get:Exclude @set:Exclude var documentReference: DocumentReference? = null
                      )
