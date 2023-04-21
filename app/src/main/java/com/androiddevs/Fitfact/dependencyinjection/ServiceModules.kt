package com.androiddevs.Fitfact.dependencyinjection

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.androiddevs.Fitfact.R
import com.androiddevs.Fitfact.other.constants
import com.androiddevs.Fitfact.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
//SCOPE TO THE LIVE TIME OF TRACKING SERVICE
//SO THE dependencies are lived as long as our tracking service does but not as long as the whole app does
@Module
@InstallIn(ServiceComponent::class)
//how long the dependencies in this service module is present--->@InstallIn
object ServiceModules {
    @SuppressLint("VisibleForTests")
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app:Context
//    as here we want that this func have single scoped so service scoped
    )=FusedLocationProviderClient(app)
    @RequiresApi(Build.VERSION_CODES.M)
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    )= PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action= constants.ACTION_SHOW_TRACKING_FRAGMENTS
        },
        PendingIntent.FLAG_IMMUTABLE or  PendingIntent.FLAG_UPDATE_CURRENT
    )!!
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    )=NotificationCompat.Builder(app, constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
//        just prevents if  users click on the notification that notification disappear
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Fit-Fact")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}