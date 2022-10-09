package com.androiddevs.Fitfact.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "running_table")
@TypeConverters(Convertors::class)
//entity is a table which has several columns and here we have columns for date,average,speed etc
data class Run(
//    here in constructor we give all the properties which we defined in all columns
//    just like preview image of maps,speed,date ,time duration
//    when we create var image it is not initialised so we give null
    var image:Bitmap?=null,
    var timestamp: Long=0L,
//    here we don't sort run distance by date it is harder,so we give time-stamp
//    here long is type of data and 0L l for long
    var avgSpeedInKMH:Float=0f,
    var distanceInMeters:Int=0,
    var timeInMillis:Long=0L,
    var caloriesBurned:Int=0
) {
    @PrimaryKey(autoGenerate = true)
//    autogenerate means room handles the id for all entries
    var id:Int?=null
}