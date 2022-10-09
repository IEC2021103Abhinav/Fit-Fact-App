package com.androiddevs.Fitfact.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.constants.KEY_HEIGHT
import com.androiddevs.Fitfact.other.constants.KEY_NAME
import com.androiddevs.Fitfact.other.constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject
@AndroidEntryPoint
class SettingsFragment:Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()
        btnApplyChanges.setOnClickListener{
            val success=applyChangesToSharedPref()
            if(success){
                Snackbar.make(view,"Saved changes",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please fill out all the Fields",Snackbar.LENGTH_LONG).show()
            }
        }
    }
    private fun loadFieldsFromSharedPref(){
        val name=sharedPreferences.getString(KEY_NAME,"")
        val height=sharedPreferences.getFloat(KEY_HEIGHT,120f)
        val weight=sharedPreferences.getFloat(KEY_WEIGHT,80f)
        etName.setText(name)
        etHeight.setText(height.toString())
        etWeight.setText(weight.toString())

    }
    private fun applyChangesToSharedPref():Boolean{
//        if we leave any part then the changes are not apply
        val nameText=etName.text.toString()
        val weightText=etWeight.text.toString()
        val heightText=etHeight.text.toString()
        if(nameText.isEmpty()||weightText.isEmpty()||heightText.isEmpty())
        {
            return false
        }
//        if user write correctly then we have to saved the shared preferences
        sharedPreferences.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weightText.toFloat())
            .putFloat(KEY_HEIGHT,heightText.toFloat())
            .apply()
        val toolbarText="Let's go $nameText"
        requireActivity().tvToolbarTitle.text=toolbarText
        return true

    }
}