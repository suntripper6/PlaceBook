package com.raywenderlich.placebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    var phone: String = ""

)