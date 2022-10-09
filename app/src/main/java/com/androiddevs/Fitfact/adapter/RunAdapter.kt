package com.androiddevs.Fitfact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.database.Run
import com.androiddevs.Fitfact.other.TrackingUtility
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter:RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
//    implement a list differ which is a tool to calculate the difference between lists and return them
//    we have to update the only items which are different from we have to list
//    for list differ we need  differ callback which is basically a way to tell that list differ how to items look like
//    they are same or different
    val diffCallback= object : DiffUtil.ItemCallback<Run>(){
    override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
//        if the old item and new item are same
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
//        if they have same content like exactly same bitmap,same avg speed
        return oldItem.hashCode()==newItem.hashCode()
    }
}
    val differ=AsyncListDiffer(this,diffCallback)
//    it will do asynchronously do in the background
    fun submitList(list: List<Run>)=differ.submitList(list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run =differ.currentList[position]
        holder.itemView.apply{
            Glide.with(this).load(run.image).into(ivRunImage)
            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
//                the timestamp of our run is basically the date in millisecond
//                now we use the calender object to format the time stamp in actual date format
            }
            val dateFormat=SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            tvDate.text=dateFormat.format(calendar.time)
            val avgSpeed="${run.avgSpeedInKMH}km/h"
            tvAvgSpeed.text=avgSpeed
            val distanceInKm="${run.distanceInMeters/1000f}km"
            tvDistance.text=distanceInKm
            tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
            val caloriesBurned="${run.caloriesBurned}"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}