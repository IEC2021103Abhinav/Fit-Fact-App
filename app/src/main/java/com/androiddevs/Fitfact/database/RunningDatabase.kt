package com.androiddevs.Fitfact.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import javax.inject.Singleton

@Database(
//    by annotations we tell our room that this is our database
//    it take two parameters that is first entities
    entities = [Run::class],
    version = 1
)
@TypeConverters(Convertors::class)
//    as we are framed type convertors ,so we have to tell the room to use typeconvertors
//from which location

@Singleton
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDAO
}
//abstract class RunningDatabase :RoomDatabase(){
////    here the runningdb class is inherit from roomdatabase
////    inside of this class we have function that returns runDAO objects
////    the behaviour of room controlled by room
//    abstract fun getRunDao():RunDAO
//}