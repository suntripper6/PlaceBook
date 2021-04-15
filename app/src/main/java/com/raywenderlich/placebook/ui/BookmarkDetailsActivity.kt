package com.raywenderlich.placebook.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarldetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    //***items for Toolbar
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    //*** Populate fields in View
    private fun populateFields() {
        bookmarkDetailsView?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    //*** Bookmark image from view model and ass to image UI element
    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }
        }
    }

    //*** Read Intent data to populate UI
    private fun getIntentData() {
        // 1
        val bookmarkId = intent.getLongExtra(MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        // 2
        bookmarldetailsViewModel.getBookmark(bookmarkId)?.observe(
            this, Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                // 3
                it?.let {
                    bookmarkDetailsView = it
                    // Populate fields from bookmark
                    populateFields()
                    populateImageView()
                    imageViewPlace.setOnClickListener {
                        replaceImage()
                    }
                }
            }
        )
    }
    //*** Modify
    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = editTextName.text.toString()
            bookmarkView.notes = editTextNotes.text.toString()
            bookmarkView.address = editTextAddress.text.toString()
            bookmarkView.phone = editTextPhone.text.toString()
            bookmarldetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }
    //*** Save
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //*** Implement interface
    override fun onCaptureClick() {
        Toast.makeText(this, "Camera Capture",
        Toast.LENGTH_SHORT).show()
    }
    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick",
        Toast.LENGTH_SHORT).show()
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }
}