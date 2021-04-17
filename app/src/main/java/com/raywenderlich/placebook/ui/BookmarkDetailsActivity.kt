package com.raywenderlich.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.utils.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import java.io.File
import java.net.URLEncoder

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
        setupFab()
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
        imageViewPlace.setOnClickListener {
            replaceImage()
        }
    }

    //*** Read Intent data to populate UI
    private fun getIntentData() {
        // 1
        val bookmarkId = intent.getLongExtra(MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        // 2
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
            this, androidx.lifecycle.Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                // 3
                it?.let {
                    bookmarkDetailsView = it
                    // Populate fields from bookmark
                    populateFields()
                    populateImageView()
                    poplateCategoryList()
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
            bookmarkView.category = spinnerCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
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
            R.id.action_delete -> {
                deleteBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //*** Implement interface
    override fun onCaptureClick() {
        photoFile = null
        try {
            // 2
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException) {
            // 3
            return
        }
        // 4
        photoFile?.let { photoFile ->
            // 5
            val photoUri = FileProvider.getUriForFile(this,
            "com.raywenderlich.placebook.fileprovider", photoFile)
            // 6
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // 7
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
            // 8
            val intentActivities = packageManager.queryIntentActivities(captureIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}
            // 9
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }
    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    //*** Return downsampled processed image
    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri, resources.getDimensionPixelSize(
            R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height), this)
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    //*** Saves to bookmark image file
    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    //*** Loads downsampled image
    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height))
    }

    //*** Process camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1
        if (resultCode == android.app.Activity.RESULT_OK) {
            // 2
            when (requestCode) {
                // 3
                REQUEST_CAPTURE_IMAGE -> {
                    // 4
                    val photoFile = photoFile ?: return
                    // 5
                    val uri = FileProvider.getUriForFile(this,
                        "com.raywenderlich.placebook.fileprovider", photoFile)
                    revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    // 6
                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let { updateImage(it) }
                }
                REQUEST_GALLERY_IMAGE -> if (data!= null && data.data != null) {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    private fun poplateCategoryList() {
        // 1
        val bookmarkView = bookmarkDetailsView ?: return
        // 2
        val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(bookmarkView.category)
        // 3
        resourceId?.let { imageViewCategory.setImageResource(it) }
        // 4
        val categories = bookmarkDetailsViewModel.getCategories()
        // 5
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        // 6
        spinnerCategory.adapter = adapter
        // 7
        val placeCategory = bookmarkView.category
        spinnerCategory.setSelection(
            adapter.getPosition(placeCategory))

        //*** Save category
        spinnerCategory.post {
            // 2
            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View,
                                            position: Int, id: Long) {
                    // 3
                    val category = parent.getItemAtPosition(position) as String
                    val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(category)
                    resourceId?. let {
                        imageViewCategory.setImageResource(it)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Required method but not used.
                }

            }
        }
    }

    // Delete the bookmark
    private fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return

        AlertDialog.Builder(this)
            .setMessage("Delete?")
            .setPositiveButton("Ok") { _, _ ->
                bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create().show()
    }

    // Share bookmark
    private fun sharePlace() {
        // 1
        val bookmarkView = bookmarkDetailsView ?: return
        // 2
        var mapUrl = ""
        if (bookmarkView.placeId == null) {
            // 3
            val location = URLEncoder.encode("${bookmarkView.latitude}," +
                                            "${bookmarkView.longtitude}", "utf-8")
            mapUrl = "https://www.google.com/maps/dir/?api=1" + "&destingation=$location"
        } else {
            // 4
            val name = URLEncoder.encode(bookmarkView.name, "utf-8")
            mapUrl = "https://google.com/maps/dir/?api=1" +
                    "&destination=$name&destination_place_id=" +
                    "${bookmarkView.placeId}"
        }
        // 5
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        // 6
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out ${bookmarkView.name} at:\n$mapUrl")
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing ${bookmarkView.name}")
        // 7
        sendIntent.type = "text/plain"
        // 8
        startActivity(sendIntent)
    }

    // Share bookmark button
    private fun setupFab() {
        fab.setOnClickListener { sharePlace() }
    }

    // Request code
    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}