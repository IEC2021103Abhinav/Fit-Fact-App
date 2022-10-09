package com.androiddevs.Fitfact.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.adapter.RunAdapter
import com.androiddevs.Fitfact.database.Run
import com.androiddevs.Fitfact.other.SortType
import com.androiddevs.Fitfact.other.TrackingUtility
import com.androiddevs.Fitfact.other.constants.REQUEST_CODE_LOCATION_PERMISSION
import com.androiddevs.Fitfact.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks{
    private val viewModel: MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter
//    daggger manages the viewmodel factory for us
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        setupRecyclerView()
    when(viewModel.sortType){
        SortType.DATE->spFilter.setSelection(0)
//        0--index for Date in string.xml
        SortType.AVG_SPEED->spFilter.setSelection(1)
        SortType.CALORIES_BURNED->spFilter.setSelection(2)
        SortType.DISTANCE->spFilter.setSelection(3)
        SortType.RUNNING_TIME->spFilter.setSelection(4)

    }
    spFilter.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            when(pos){
                0->viewModel.sortRuns(SortType.DATE)
                1->viewModel.sortRuns(SortType.AVG_SPEED)
                2->viewModel.sortRuns(SortType.CALORIES_BURNED)
                3->viewModel.sortRuns(SortType.DISTANCE)
                4->viewModel.sortRuns(SortType.RUNNING_TIME)

            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }
        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
        fab.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragments2)
        }
    }
    private fun setupRecyclerView()=rvRuns.apply{
        runAdapter= RunAdapter()
        adapter=runAdapter
        layoutManager=LinearLayoutManager(requireContext())
    }
    private fun requestPermissions(){
//        at first check by utility function
        if(TrackingUtility.hasLocationPermission(requireContext()))
        {
            //            here context is not equal to null
//            here we have already the permission so do not need any request
            return
        }
//        if the user don,t accept the permission before  ,it is his first time  then this if cond
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
//            not running on android q
//            do not call has permission
//            instead of that  we have called easy permission
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
//                these request we have to pass
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
//                running on android q
//                EasyPermission library help in handle the denied permission,if any request of permission
//                is first time denied and then again denied and then by library it can not give request to
//                user and he can only enable in app settings
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
//        first implement the permission call back to get the result of that request in the upper
//        class heading by adding  Easypermission after adding fragment
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        Here we check whether the user permanently denied the permission or not,in that case show dialogue
//        if he denied first time then show rational instance then request again
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
//            permanently denied case
            AppSettingsDialog.Builder(this).build().show()
        }else{
//            denied first time or temporarily
            requestPermissions()
        }
    }
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
//        this on requestresult fun is called whenever we request permission
//        easypermisssion is not for android default
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}