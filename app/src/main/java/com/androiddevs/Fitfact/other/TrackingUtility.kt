package com.androiddevs.Fitfact.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.androiddevs.Fitfact.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {
// just check the user already accepts the location permissions or not
//    here we have only function ,don't need any instances
    fun hasLocationPermission(context: Context)=
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
//            if the device is  not running on android q,in that case we have not need any background
        //            permission
//            if device running on android Q then else condition will start
            EasyPermissions.hasPermissions(
                context,
//                check permission
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )

        }
    fun calculatePolylinelength(polyline: Polyline):Float{
        var distance=0f
        for(i in 0..polyline.size-2){
            val pos1=polyline[i]
            val pos2=polyline[i+1]
            val result=FloatArray(1)
//            result is saved in float array
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance+=result[0]
        }
        return distance
    }
    fun getFormattedStopWatchTime(ms:Long,includeMillis:Boolean=false):String{
//        we have not want time in millisec always just like in the notifiation
        var milliseconds=ms
        val hours=TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds-=TimeUnit.HOURS.toMillis(hours)
        val minutes=TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds-=TimeUnit.MINUTES.toMillis(minutes)
        val seconds=TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis){
            return "${if(hours<10)"0" else ""}$hours:"+
                    "${if(minutes<10)"0" else ""}$minutes:"+
                    "${if(seconds<10)"0" else ""}$seconds:"
//            if our hours is less than then add zero before the hours digit
//            if hours is greater or equal to 10 then prepend empty strings there

        }
        milliseconds-=TimeUnit.SECONDS.toMillis(seconds)
        milliseconds/=10
//        we only want two digit number for millisec
        return "${if(hours<10)"0" else ""}$hours:"+
                "${if(minutes<10)"0" else ""}$minutes:"+
                "${if(seconds<10)"0" else ""}$seconds:"+
                "${if(milliseconds<10)"0" else ""}$milliseconds"


    }

}