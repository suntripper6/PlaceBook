package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo

// Interacts with repos (like controller in MVC)
// 1
class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MapsViewModel"
    // 2
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    // Variable to store list of bookmark Views
    private var bookmarks: LiveData<List<BookMarkerView>>? = null
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

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    // Helper method
    private fun bookToMarkerView(bookmark: Bookmark) : MapsViewModel.BookMarkerView {
        return MapsViewModel.BookMarkerView(bookmark.id,
        LatLng(bookmark.latitude, bookmark.longitude)
        )
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

    fun getBookmarkMarkerViews() : LiveData<List<BookMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }
        return bookmarks
    }

    // Hold data for visible bookmark marker
    data class BookMarkerView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0)
    )
}