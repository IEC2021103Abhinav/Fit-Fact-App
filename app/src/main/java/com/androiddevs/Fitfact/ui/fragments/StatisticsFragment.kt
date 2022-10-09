package com.androiddevs.Fitfact.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.CustomMarkerView
import com.androiddevs.Fitfact.other.TrackingUtility
import com.androiddevs.Fitfact.other.constants.AXIS_LINE_COLOR
import com.androiddevs.Fitfact.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment:Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }
    private fun setupBarChart(){
        barchart.xAxis.apply {
            position=XAxis.XAxisPosition.BOTTOM
//            disable the labels of our x axis
            setDrawLabels(false)
            axisLineColor = AXIS_LINE_COLOR
            textColor= AXIS_LINE_COLOR
            setDrawGridLines(false)
        }
        barchart.axisLeft.apply {
            axisLineColor= AXIS_LINE_COLOR
            textColor= AXIS_LINE_COLOR
            setDrawGridLines(false)
        }
        barchart.axisRight.apply {
            axisLineColor= AXIS_LINE_COLOR
            textColor= AXIS_LINE_COLOR
            setDrawGridLines(false)
        }
        barchart.apply {
            description.text="Avg Speed Over Time"
            legend.isEnabled=false

        }
    }
    private  fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
//                if our database is empty then sql does not know how to calculate the total time run
                val totalTimeRun=TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text=totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
//                if our database is empty then sql does not know how to calculate the total time run
                val km=it/1000f
                val totalDistance=round(km*10f)/10f
                val totalDistanceString="${totalDistance}km"
                tvTotalDistance.text=totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed= round(it*10f)/10f
//                round is used for cut off all the decimal part
                val avgSpeedString="${avgSpeed}km/h"
                tvAverageSpeed.text=avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories="${it}kcal"
                tvTotalCalories.text=totalCalories
            }
        })
        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
//                we need to create a list of called bar entries so,those  bar entries contains the value of x-bar and y-bar
//                and the whole entries are filledUp the BAR
                val allAvgSpeed=it.indices.map { i->BarEntry(i.toFloat(),it[i].avgSpeedInKMH) }
                val barDataSet=BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor= AXIS_LINE_COLOR
                    color=ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barchart.data= BarData(barDataSet)
                barchart.marker=CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
//                reverse that is in descending order
                barchart.invalidate()
//                for update the bar graph if we change in the run
//                we wnt to display some details on the graph if we click on the graph by marker view



            }
        })

    }


}