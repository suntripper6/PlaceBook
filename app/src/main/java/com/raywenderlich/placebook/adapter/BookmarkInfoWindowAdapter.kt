package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.R

// 1 Adapter Hosting Maps Activity
class BookmarkInfoWindowAdapter(context: Activity) :
    GoogleMap.InfoWindowAdapter {

    // 2
    private val contents: View

    // 3 Inflate and save contents
    init {
        contents = context.layoutInflater.inflate(
            R.layout.content_bookmark_info, null)
    }

    // 4 Not replacing contents
    override fun getInfoWindow(marker: Marker): View? {
        // This function is required but can return null if
        // not replacing the entire info window
        return null
    }

    // 5 Capture titleView, phoneView, imageView
    override fun getInfoContents(marker: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""

        val imageView = contents.findViewById<ImageView>(R.id.photo)
        imageView.setImageBitmap(marker.tag as Bitmap?)

        return contents
    }

}