package com.raywenderlich.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raywenderlich.placebook.util.ImageUtils

// Single data source
// 1 DB Entity Class
@Entity
// 2 - primary constructor with Primary Key and fields with default values
data class Bookmark(
    // 3
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    // 4
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = ""
)
//***
{
    // 1
    fun setImage(image: Bitmap, context: Context) {
        //2
        id?.let {
            ImageUtils.saveBitmapToFile(context, image, generateImageFilename(it))
        }
    }
    // 3
    companion object {
        fun generateImageFilename(id: Long): String {
            // 4
            return "bookmark$id.png"
        }
    }
}