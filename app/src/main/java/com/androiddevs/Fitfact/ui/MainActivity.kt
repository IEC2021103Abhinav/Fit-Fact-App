package com.androiddevs.Fitfact.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.constants.ACTION_SHOW_TRACKING_FRAGMENTS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

//for inject something in any activity ,fragment we have to add @AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        navigateToTrackingFragmentIfNeeded(intent)
//        At first i have to say android that my main action bar is toolbar
        setSupportActionBar(toolbar)
//        set up our navigation view and connect with the  navigation components
        bottomNavigationView.setupWithNavController(navHostHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /*NO-OP*/ }
//        upper reselected func does nothing but it prevent from the reload of the previous Run data
//        after clicking Run
//        As here we have five fragments but we have set transitions for 3 only and other two are not
//        connected and we don't want they are in our navigation view
//        to solve this problem we have to addon
        navHostHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
//                this addOndestinationChangedListener triggerd only when our destination changed
//                with navigation components
//                here it will check,if the fragments have transition s then the bottom nav view is
//                visible otherwise invisible
                when (destination.id) {
                    R.id.settingsFragment, R.id.statisticsFragment, R.id.runFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }
//    if activity is not destroyed
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action==ACTION_SHOW_TRACKING_FRAGMENTS){
            navHostHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
//            now it could be happen our main activity is destroyed but our service is still running then intent sent
//            pending intent it maean our main activity will be relaunched and in that case it go inside of oncreate

        }
    }

}
