package com.androiddevs.Fitfact.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {
//    here we have all functions like database queries that provides us the necessary runs at that time
//    function for insert run
    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    when we insert an run if that already exist in database then old one replace by new one
    suspend fun insertRun(run: Run)
    @Delete
    suspend fun deleteRun(run: Run)
//    function for delete a run and as it is a suspend fun so it will in courtine
//    we want to get everything from our running table and we want that ordered by timestamp
//    latest runs on top so we ordered in descending i.e. DESC
//    next function on database queries which return live data objects,and want to get something from our databse
//    this not work in courtine and return type list of Run
//    sort every value which os in our table
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate():LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis():LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned():LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed():LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance():LiveData<List<Run>>
//    we need more functions for calculating all the statistics of all run that is total values of
//    parameters like total time ,total speed for which we need database
//    As here time in millis return live data lst of type long
    @Query("SELECT SUM(timeInMillis) FROM running_table ")
    fun getTotalTimeInMillis():LiveData<Long>
//here SUM function goes to our running table and adds up all of time in millis entiies
//now we do same for all entries
    @Query("SELECT SUM(caloriesBurned) FROM running_table ")
    fun getTotalCaloriesBurned():LiveData<Int>
    @Query("SELECT SUM(distanceInMeters) FROM running_table ")
    fun getTotalDistance():LiveData<Int>
    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table ")
    fun getTotalAvgSpeed():LiveData<Float>
}