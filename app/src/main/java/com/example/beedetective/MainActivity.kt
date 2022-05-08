package com.example.beedetective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

    val CURRENT_FRAGMENT_BUNDLE_KEY = "current fragment bundle key"
    var currentFragmentTag = "REPORT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentFragmentTag = savedInstanceState?.getString(CURRENT_FRAGMENT_BUNDLE_KEY) ?: "REPORT"

        showFragment(currentFragmentTag)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.report -> {
                    showFragment("REPORT")
                    true
                }
                R.id.live_Feed -> {
                    showFragment("LIST")
                    true
                }
                // TODO activate map option once that fragment is working
                else -> {
                    false
                }
            }
        }

    }

    private fun showFragment(tag: String) {

        currentFragmentTag = tag

        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            val transaction = supportFragmentManager.beginTransaction()
            when (tag) {
                "REPORT" -> transaction.replace(R.id.fragmentContainerView, BeeReportFragment.newInstance(), "REPORT")
                "LIST" -> transaction.replace(R.id.fragmentContainerView, ReportListFragment.newInstance(), "LIST")
            }
            transaction.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_BUNDLE_KEY, currentFragmentTag)
    }
}