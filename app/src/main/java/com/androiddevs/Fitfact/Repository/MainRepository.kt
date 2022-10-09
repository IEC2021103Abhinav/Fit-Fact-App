package com.androiddevs.Fitfact.Repository

import com.androiddevs.Fitfact.database.Run
import com.androiddevs.Fitfact.database.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO
) {
//    To provide functions to our database,so that functions w provided to dao object for viewmodels
//    we  will need the dao objects in our main repo
//    we will get by injecting it
    suspend fun insertRun(run: Run)=runDAO.insertRun(run)
    suspend fun deleteRun(run: Run)=runDAO.deleteRun(run)
    fun getAllRunsSortedByDate()=runDAO.getAllRunsSortedByDate()
//    here return live data object and  live data object is asynchronous anyways
    fun getAllRunsSortedByDistance()=runDAO.getAllRunsSortedByDistance()
    fun getAllRunsSortedByTimeInMillis()=runDAO.getAllRunsSortedByTimeInMillis()
    fun getAllRunsSortedByAvgSpeed()=runDAO.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned()=runDAO.getAllRunsSortedByCaloriesBurned()
    fun getTotalAvgSpeed()=runDAO.getTotalAvgSpeed()
    fun getTotalDistance()=runDAO.getTotalDistance()
    fun getTotalTimeInMillis()=runDAO.getTotalTimeInMillis()
    fun getTotalCaloriesBurned()=runDAO.getTotalCaloriesBurned()


}