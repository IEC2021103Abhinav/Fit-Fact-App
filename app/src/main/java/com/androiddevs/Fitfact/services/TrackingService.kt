package com.androiddevs.Fitfact.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.TrackingUtility
import com.androiddevs.Fitfact.other.constants.ACTION_PAUSE_SERVICE
import com.androiddevs.Fitfact.other.constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.Fitfact.other.constants.ACTION_STOP_SERVICE
import com.androiddevs.Fitfact.other.constants.FASTEST_LOCATION_INTERVAL
import com.androiddevs.Fitfact.other.constants.LOCATION_UPDATE_INTERVAL
import com.androiddevs.Fitfact.other.constants.NOTIFICATION_CHANNEL_ID
import com.androiddevs.Fitfact.other.constants.NOTIFICATION_CHANNEL_NAME
import com.androiddevs.Fitfact.other.constants.NOTIFICATION_ID
import com.androiddevs.Fitfact.other.constants.TIMER_UPDATE_INTERVAL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

typealias Polyline=MutableList<LatLng>
//by type alias we can assign different name
typealias Polylines=MutableList<Polyline>
@AndroidEntryPoint
class TrackingService:LifecycleService() {
//    whenever we got new location by user then we can simply react to those changes in our tracking frag
//    to observe the changes in tracking frag we have live data objects in companion objects
//    and draw the lines in our map view
    var isFirstRun=true
    var serviceKilled=false
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSecond=MutableLiveData<Long>()
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
//    to update our notification with current time ,to do that just post new notification with same id that why i create baseNo...Builder
//    which will  hold the configuration of every of our notifications
    lateinit var curNotificationBuilder: NotificationCompat.Builder
    companion object{
//        here we create live data objects
        val isTracking=MutableLiveData<Boolean>()
        val timeRunInMillis=MutableLiveData<Long>()
//        here saved the location
        val pathPoints=MutableLiveData<Polylines>()
//        list of list of several co-ordinates
//        latlong means longitude and longitude.
    }
    private fun postInitialValues(){
//        initially it is not tracking anything that why it set to be false
        isTracking.postValue(false)
//        we wnt to use our pathpoints live data and just we use empty list because there is no any co-ordinates
//        in the begining
        pathPoints.postValue(mutableListOf())
        timeRunInSecond.postValue(0L)
        timeRunInMillis.postValue(0L)
    }
//    here the tracking service is inherit from life cycleService()
//    we need to observe the live data objects inside of this service class and the observe function of
//    live data objects needs the life cycle owner and if we don't specify the lifecycle service we can
//    not pass its instance of service to observe func's owner
//    how we manage communication  activity to services,use intents to services
//    now communication from service to activity/fragments -->two major options
//    first singleton pattern (which means)-->we will take our property inside of our service class
//    that we want to be accessible from outside and  put them in an companion object so,fragments can access
//    second-->service to be make bound service so,service acts like a server and clients can binds to it
//    here clients are our fragments
    override fun onCreate() {
        super.onCreate()
//    at start with baseNoti..Builder but for change We have to do CurNo...Builder
        curNotificationBuilder=baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient= FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingstate(it)
    })
    }
    private fun killService(){
        serviceKilled=true
        isFirstRun=true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//    this fun is call whenever we send a command(intent attached with some activity) to our service
//    in our case three potential action which are to sent to our service
//    an action to start it ,an action to resume it,in action to pause it
//    intent get from this func check first
    intent?.let{
        when(it.action){
//            depending on the action we can now act to those fun
            ACTION_START_OR_RESUME_SERVICE->{
                if(isFirstRun){
                    startForegroundService()
                    isFirstRun=false
                }else {
                    Timber.d("Resuming service...")
                    startTimer()
                }
            }
            ACTION_PAUSE_SERVICE->{
                Timber.d("Paused service")
                pauseService()
            }
            ACTION_STOP_SERVICE->{
                Timber.d("stopped service")
                killService()
//                Timber comes in to reduce the tedious task by automatically generating the tags
//                and later removing the logs from the generated apks
            }
        }
//        but we have also function in our tracking frag that sends the intent to our service
//        with the commands attached
    }
        return super.onStartCommand(intent, flags, startId)
    }
//    fun that starts the timer here and also trigger our service
    private var isTimerEnabled=false
//    the time where we begin the run and time between each start and pause
    private var lapTime=0L
//    all lap run sum timeRun
    private var timeRun=0L
    private var timeStarted=0L
    private var lastSecondTimeStamp=0L
    private fun startTimer(){
//        when this fun is work
//        it will work at the time of start and resume our service also
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted=System.currentTimeMillis()
        isTimerEnabled=true
//        here i want to track the time by courtine because it is very bad performance to app to call observer every time
//        after that delay that courtine for few milliseconds .
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
//                time-diff between now and time started
                lapTime=System.currentTimeMillis()-timeStarted
//                post the new lap time
                timeRunInMillis.postValue(timeRun+lapTime)
                if(timeRunInMillis.value!!>=lastSecondTimeStamp+1000L){
//                    we have update the timeRunInSeconds object in the new 1 sec
                    timeRunInSecond.postValue(timeRunInSecond.value!!+1)
                    lastSecondTimeStamp+=1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun+=lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled=false
    }
    private fun updateNotificationTrackingstate(isTracking: Boolean)
    {
//        need to specify our action text for action of notification which is  either pause or resume
        val notificationActionText=if(isTracking)"Pause" else "Resume"
        val pendingIntent= if(isTracking){
            val pauseIntent=Intent(this,TrackingService::class.java).apply {
                action= ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent= Intent(this,TrackingService::class.java).apply {
                action= ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
//        the way to remove all actions before we update the notification with new action
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible=true
//            by empty list clear all action
            set(curNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            curNotificationBuilder=baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp,notificationActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
        }

    }
    @SuppressLint("MissingPermission")
    private  fun updateLocationTracking(isTracking:Boolean){
//        whenever we changes the value of is tracking or start or end the tracking then
//        then we have to update the location tracking and update by request
        if(isTracking){
            if(TrackingUtility.hasLocationPermission(this)){  //if allow to permission
//                we need location request
                val request = com.google.android.gms.location.LocationRequest().apply {
                   interval = LOCATION_UPDATE_INTERVAL
//                    for how much interval of time  we want tha location updates like after 5sec we ant updates
                    fastestInterval= FASTEST_LOCATION_INTERVAL
//                    we want fast 2sec interval
                    priority=PRIORITY_HIGH_ACCURACY
//                    we wnt accurate result
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }else{
//                here we remove location updates
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }
    val locationCallback=object:LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let { locations->
                    for(location in locations){
                        addPathPoint(location)
                        Timber.d("New Location:${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }
    private fun addPathPoint(location: Location){
//        add the co-ordinates to our last polylines of polylines list
//        here we check the location which is null is equal to null
//        here we have to convert the lat and lang
        location?.let {
//            here we have to change the co-ordinates of location to lat long
            val  pos=LatLng(location.latitude,location.longitude)
//            add the position
            pathPoints.value?.apply{
                last().add(pos)
//                add at the last polylines
                pathPoints.postValue(this)
            }
        }
    }
//    here our next step is to find the location callback
//    add co-ordinates of the point on map in the polyline list
    private fun addEmptyPolyline()= pathPoints.value?.apply{
//    add empty list of latlong co-ordinates at the end of polylines list because we can pause our tracking or resume
//    it again so before we put empty list first and then put list of co-ordinates
        add(mutableListOf());
        pathPoints.postValue(this);
//    because we add new polylines that why we can see changes in our tracking frag so we have to post new value
//    of our fragment
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))  //this is case of value =0

    private fun startForegroundService(){
//        this func is work at the time of start only not at the time of resume
        startTimer()
        addEmptyPolyline()
        isTracking.postValue(true)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)
        as NotificationManager
//        notification manager is a system service of android frame work that we need whenever we wnt to show
//        notification so,we get reference to system service and then crete our notification channel locally in
//        app
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
//            if build version greater than android oreo
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())
        timeRunInSecond.observe(this, Observer {
            if(!serviceKilled)
            {
                val notification=curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it*1000L))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        })

//        this is our notification initial time and we will upgrade this from initial time and intent new value to it
//        appending intent is used here when we click on notification,the main activity should open
//        but in general the run fragments show because that is our initial frag
//        when we create pending intent at that time we have to pass a normal intent to that with passing an action
//        that will check when our main activity is launch


    }
//    private fun getMainActivityPendingIntent()=PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this,MainActivity::class.java).also {
//            it.action= ACTION_SHOW_TRACKING_FRAGMENTS
//        },
//        FLAG_UPDATE_CURRENT
//    whenever we launch pending intent that already exist it will update instant of recreation it or restarting it
//    we can  go to main activity and whenever we get new intent in that main activity, we can check if it is that action
//    is attached to that intent we simply want to navigate to our fragments
//    but now there is no option to navigate to tracking frag in nav graph so we have to define global action in nav_graph
//    for tracking frag
//    )
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

}
// foreground service must comes with notification ,foreground means user aware of that service of run
//It can be killed by the android system
//we can also use background service but if the android system need the memory then it might happen
//it kills ur service and there is no way to complete it with background service

//for implementingSTOP WATCH we have to created two live data objects -->Current time Run in sec(used in notification)
// because notification do not to update frequently,other give current time run in millisec ,it is used to update trcking frag
