package com.androiddevs.Fitfact.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.database.Run
import com.androiddevs.Fitfact.other.TrackingUtility
import com.androiddevs.Fitfact.other.constants.ACTION_PAUSE_SERVICE
import com.androiddevs.Fitfact.other.constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.Fitfact.other.constants.ACTION_STOP_SERVICE
import com.androiddevs.Fitfact.other.constants.CANCEL_TRACKING_DIALOG_TAG
import com.androiddevs.Fitfact.other.constants.MAP_ZOOM
import com.androiddevs.Fitfact.other.constants.POLYLINE_COLOR
import com.androiddevs.Fitfact.other.constants.POLYLINE_WIDTH
import com.androiddevs.Fitfact.services.Polyline
import com.androiddevs.Fitfact.services.TrackingService
import com.androiddevs.Fitfact.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragments:Fragment(R.layout.fragment_tracking) {
    private  val viewModel: MainViewModel by viewModels()
    private var isTracking =false
    private var pathPoints = mutableListOf<Polyline>()
//    Here we used the map view not map fragments
//    but google maps provide both there is one advantage in map fragments
//    A map fragments nothing else than a map view inside of an fragments but why we need fragments
//    each map which i want to be include in my app has own life cycle
//    if u use map fragments then there is no need of worry about its life cycle
//    but u use map view then u have to control ur life cycle
    private  var map:GoogleMap?=null
    private var curTimeInMillis=0L
    private var menu: Menu?=null
    @set:Inject
    var weight=80f
    @set:Inject
    var height=120f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
//        we want this function is not to be always called but for this tracking frag it is to be default called so
//        setHasOption to be true
        return super.onCreateView(inflater, container, savedInstanceState)
    }
//    here  map is of type google map and map view of type map view and it is just display of google map
//    google map is our object of map view
//    we use this map to track our running path to draw on it
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener{
            toggleRun()
        }
//    after rotation of device savedInstance state is not null
    if(savedInstanceState!=null){
        val cancelTrackingDialogue=parentFragmentManager.findFragmentByTag(
            CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialogue?
        cancelTrackingDialogue?.setYesListener {
            stopRun()
        }
    }
    btnFinishRun.setOnClickListener{
        ZoomToSeeWholeTrack()
        endRunAndSaveToDb()
    }
//   just  load our map this func
        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }
    subscribeToObservers()
    }
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addAllPolylines()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis=it
            val formattedTime=TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            tvTimer.text=formattedTime
        })
    }

    private fun toggleRun() {
//        for pause or resume
        if (isTracking) {
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
//            to pause service
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu=menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis>0L)
        {
            this.menu?.getItem(0)?.isVisible=true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking->{
                showCancelTrackingDialogue()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showCancelTrackingDialogue(){
        CancelTrackingDialogue().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG_TAG)
    }
    private fun stopRun()
    {
        tvTimer.text="00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragments_to_runFragment2)

    }
    private fun updateTracking(isTracking:Boolean)
    {
//        Observe the data from our service and react to those changes
//        update the is tracking to new one
        this.isTracking=isTracking
        if(!isTracking&&curTimeInMillis>0L){
//            agar track nahi kar rha hai
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE
        }else if(isTracking){
//            agar track kar raha hai
            btnToggleRun.text="Stop"
            menu?.getItem(0)?.isVisible=true
            btnFinishRun.visibility=View.GONE
        }

    }
    private fun moveCameraToUser(){
//        when app detect new pos in pathpoint list camera adjust to user
        if(pathPoints.isNotEmpty()&&pathPoints.last().isNotEmpty())
        {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }
    private fun ZoomToSeeWholeTrack (){
    val bounds=LatLngBounds.Builder()
        for(polyline in pathPoints){
            for(pos in polyline)
            {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height*0.05f).toInt()
            )
        )
    }
//    how to save the info written in our app in our db
    private fun endRunAndSaveToDb()
    {
        map?.snapshot { bmp->
//            Calculate distance of run first
            var distanceInMeters=0
            for(polyline in pathPoints){
                distanceInMeters+=TrackingUtility.calculatePolylinelength(polyline).toInt()
            }
            val avgSpeed= round((distanceInMeters/1000f) /(curTimeInMillis/1000f/60/60)*10)/10f
            val dateTimeStamp=Calendar.getInstance().timeInMillis
            val caloriesBurned=((distanceInMeters/1000f)*weight).toInt()
            val run= Run(bmp,dateTimeStamp,avgSpeed,distanceInMeters,curTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()

        }

    }
//    by add all polylines fun weccan connect all the polyline on map
    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)

        }
    }
//    whenever we see changes in the pathpoints then we want to connect our last points of that list with the 2nd last
//    point by fun add leastpolyline
    private fun addLeastPolyline(){
        if(pathPoints.isNotEmpty()&& pathPoints.last().size>1)
        {
//            atleast two elements in the last list of polyline
//            now we have to reference those two last co-ordinates inside of the last list of polyline i.e
//            our current polyline
            val preLastLatLng=pathPoints.last()[pathPoints.last().size-2]
//            2nd last element
            val lastLatLng=pathPoints.last().last()
            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
//            here we add the two polyline pathpoints
//            but this func is not enough because this func do not draw all of the polyline on the  map again when we
//            rotate the device for that we used add all polylines
        }
    }
//    these our map life cycles function
    private fun sendCommandToService(action:String)=
        Intent(requireContext(),TrackingService::class.java).also{
            it.action=action
            requireContext().startService(it)
//            add our service to manifest
        }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
//        just check for null also
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
//        if the device is run on low memory then we tell to our map view
//        just it can sve some resources
        mapView?.onLowMemory()
    }
//        it always didn't work because  always got null point exception there
//        map view destroyed before destroy function. so, u don,t need destroy func
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//    it help to save some resources or catch this map
//    Don't need to reload this map every time
        mapView?.onSaveInstanceState(outState)
    }
}
