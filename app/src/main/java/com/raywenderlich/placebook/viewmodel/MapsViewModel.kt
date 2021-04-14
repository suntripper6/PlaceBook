package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

// Interacts with repos (like controller in MVC)
// 1
class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MapsViewModel"
    // 2
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    // Variable to store list of bookmark Views
    private var bookmarks: LiveData<List<BookmarkMarkerView>>? = null
    // 3
    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        // 4
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        // 5
        val newId = bookmarkRepo.addBookmark(bookmark)

        // ***save image to Bookmark
        image?.let {bookmark.setImage(it, getApplication())}

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    // Helper method
    //*** add image info
    private fun bookToMarkerView(bookmark: Bookmark) : MapsViewModel.BookmarkMarkerView {
        return MapsViewModel.BookmarkMarkerView(bookmark.id,
                        LatLng(bookmark.latitude, bookmark.longitude),
                        bookmark.name,
                        bookmark.phone)
    }

    // Maps LiveData updates changes in the db
    private fun mapBookmarksToMarkerView() {
        // 1
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) {
            repoBookMarks ->
            // 2
            repoBookMarks.map { bookmark ->
                bookToMarkerView(bookmark)
            }
        }
    }

    fun getBookmarkMarkerViews() : LiveData<List<BookmarkMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }
        return bookmarks
    }

    // Hold data for visible bookmark marker
    //*** Gets image
    data class BookmarkMarkerView(var id: Long? = null,
                                  var location: LatLng = LatLng(0.0, 0.0),
                                  var name: String = "",
                                  var phone: String = "") {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitMapFromFile(context, Bookmark.generateImageFilename(it))
            }
            return null
        }
    }
}