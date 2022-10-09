package com.androiddevs.Fitfact.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.Fitfact.Repository.MainRepository
import com.androiddevs.Fitfact.database.Run
import com.androiddevs.Fitfact.other.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel()
// here this class inherit from our Viewmodel but we cannot create the instance of Viewmodel easily
//also dagger can't do this easily because when we pass parameters to view model ,we have  to create
//viewmodelfactory,but daggerhilt do
{
    private val runSortedByDate=mainRepository.getAllRunsSortedByDate()
   private val runSortedByDistance=mainRepository.getAllRunsSortedByDistance()
    private val runSortedByCaloriesBurned=mainRepository.getAllRunsSortedByCaloriesBurned()
   private val runSortedByTimeInMillis=mainRepository.getAllRunsSortedByTimeInMillis()
    private val runSortedByAvgSpeed=mainRepository.getAllRunsSortedByAvgSpeed()

    val runs=MediatorLiveData<List<Run>>()
    var sortType=SortType.DATE
    init {
        runs.addSource(runSortedByDate){result->
            if(sortType==SortType.DATE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runSortedByDistance){result->
            if(sortType==SortType.DISTANCE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runSortedByAvgSpeed){result->
            if(sortType==SortType.AVG_SPEED){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runSortedByTimeInMillis){result->
            if(sortType==SortType.RUNNING_TIME){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runSortedByCaloriesBurned){result->
            if(sortType==SortType.CALORIES_BURNED){
                result?.let { runs.value=it }
            }
        }
    }
    fun sortRuns(sortType: SortType)=when(sortType){
        SortType.DATE->runSortedByDate.value?.let { runs.value=it }
        SortType.RUNNING_TIME->runSortedByTimeInMillis.value?.let { runs.value=it }
        SortType.DISTANCE->runSortedByDistance.value?.let { runs.value=it }
        SortType.AVG_SPEED->runSortedByAvgSpeed.value?.let { runs.value=it }
        SortType.CALORIES_BURNED->runSortedByCaloriesBurned.value?.let { runs.value=it }
    }.also {
        this.sortType=sortType
    }

    //    The job of main view model is to collect  the data from our repository and provide it for
//    all those fragments that will need this main view model
//    that means we need instance of our main repo in our main view model
    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepository.insertRun(run)
}
}