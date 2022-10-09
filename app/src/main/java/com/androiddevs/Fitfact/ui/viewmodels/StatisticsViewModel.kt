package com.androiddevs.Fitfact.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.androiddevs.Fitfact.Repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel()
// here this class inherit from our Viewmodel but we cannot create the instance of Viewmodel easily
//also dagger can't do this easily because when we pass parameters to view model ,we have  to create
//viewmodelfactory,but daggerhilt do
{
//    The job of main view model is to collect  the data from our repository and provide it for
//    all those fragments that will need this main view model
//    that means we need instance of our main repo in our main view model
//    five live datas
    val totalTimeRun=mainRepository.getTotalTimeInMillis()
    val totalDistance=mainRepository.getTotalDistance()
    val totalCaloriesBurned=mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed=mainRepository.getTotalAvgSpeed()

//    we want sorted  by date in statistics frag because of graph which are in chronological order here also
    val runSortedByDate=mainRepository.getAllRunsSortedByDate()
}