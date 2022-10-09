package com.androiddevs.Fitfact.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
class Convertors {
//    As Database is not saved the complex data like bitmap so ,we provide a way to room database
//    to save this bitmap and get bitmap again
//    for that we need to implement a type convertor to database
//    here we define two func
    @TypeConverter
//by @typeconvertor we are telling database that these are typeconvertor
// our database doesn't save the bitmap so it ant to convert and then get these typeconvertors
    fun toBitmap(bytes:ByteArray):Bitmap{
//    here we convert bytes which we are saved in output stream
//    are convert to bitmap
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }
    @TypeConverter
    fun fromBitmap(bmp:Bitmap):ByteArray{
//        here we take parameter as bitmap which is changes into bytearray(01100)
//        which the database understand
        val outputStream=ByteArrayOutputStream()
//    outputstream is needed to converts from bitmap to bytes
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream)
//    the bytes are saved in outputstream
    return outputStream.toByteArray()
    }
}