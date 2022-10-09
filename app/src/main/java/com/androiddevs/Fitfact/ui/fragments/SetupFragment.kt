package com.androiddevs.Fitfact.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.Fitfact.other.constants.KEY_HEIGHT
import com.androiddevs.Fitfact.other.constants.KEY_NAME
import com.androiddevs.Fitfact.other.constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {
    @Inject
    lateinit var sharedPref:SharedPreferences
//    if the app id first launch,(first launch matlab ek baar entry ho chuki hai) we have to go for setupfragment
//    if not then directly go to run fragment
    @set:Inject
    var isFirstAppOpen=true
//    we can not take a boolean as a late init var,so we can not use directly inject here

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isFirstAppOpen) {
//            fisrt time nahi khul raha hai app ,then go to run frag in this situation ,the setup frag is go to backstack ,if we click
//            on back then we reached to again on setup frag ,which we do not want
//            for this we have to remove setupfrag from backstack,by navOptions

            val navOptions=NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
//            true we also pop the latest frag which we pass
        }
        tvContinue.setOnClickListener{
            val success=writePersonalDataToSharedPref()
//            when we click on start we have to check whether the user enter all info or not
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
//                require view is used  for giving notification if the data is written or not like toast
            }

        }
    }
    private fun writePersonalDataToSharedPref():Boolean{
//        if we didn't enter any thing in the name ,weight etc then it tells the user enter some values here
        val name=etName.text.toString()
        val weight=etWeight.text.toString()
        val height=etHeight.text.toString()
        if(name.isEmpty()||weight.isEmpty()||height.isEmpty()){
            return false
//            then show snackBar
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putFloat(KEY_HEIGHT,height.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolbarText="Let's go,$name!"
        requireActivity().tvToolbarTitle.text=toolbarText
        return true
//        apply() write all those key values in our shared pref object
//            this means that it is not our first launch of our app
//        by this whole func we are saving all the key value in shared pref

    }
}