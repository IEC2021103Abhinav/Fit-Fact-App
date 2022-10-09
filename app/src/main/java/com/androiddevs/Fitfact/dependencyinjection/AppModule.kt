package com.androiddevs.Fitfact.dependencyinjection

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.androiddevs.Fitfact.database.RunningDatabase
import com.androiddevs.Fitfact.other.constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.Fitfact.other.constants.KEY_HEIGHT
import com.androiddevs.Fitfact.other.constants.KEY_NAME
import com.androiddevs.Fitfact.other.constants.KEY_WEIGHT
import com.androiddevs.Fitfact.other.constants.RUNNING_DATABASE_NAME
import com.androiddevs.Fitfact.other.constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
//in  old dagger hilt we have to create components by ourselves
//but in new we do not to do any more,because the app components are determined when the app objects are created
//and when they are destroyed in the app modules
//install app modules in the application component
//but if the component is changed to activity component then the app dependencies is destroyed
//when the activity is destroyed
object AppModule {
//    let we create a manual in which we defined the running database
    @Singleton
    @Provides
//this function provides something for us to tell dagger
//if not annotate then we have to create all time its  new instances for use of it
//but we want singleton instance so we attach singleton scope to this function
    fun provideRunningDatabase(
//    create here context,so that use in room database builder
//    dagger have to now where the context of this app
    @ApplicationContext app:Context
//
    )= Room.databaseBuilder(
//    if we not pass in constructor,then we cannot pass the context
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()
//    now we have to make a fun for  provides DAO Object for us
    @Singleton
    @Provides
    fun provideRunDao(db:RunningDatabase)=db.getRunDao()
//    now here we provide db as a parameter and dagger will automatically recognise
//    we have only define those function but we have not to call it
//    now in manifest file we have to mark application name it main application
    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext app:Context)=
        app.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)
//    mode in which we open those shared preference
    @Singleton
    @Provides
    fun provideName(sharedPref:SharedPreferences)=sharedPref.getString(KEY_NAME,"")?:""
//    here key_NAME has not default value so it has to check for null value

    @Singleton
    @Provides
    fun provideWeight(sharedPref:SharedPreferences)=sharedPref.getFloat(KEY_WEIGHT,80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref:SharedPreferences)=
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE,true)



}