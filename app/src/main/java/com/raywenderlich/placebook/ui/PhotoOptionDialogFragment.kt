package com.raywenderlich.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {
    // 1
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }
    // 2
    private lateinit var listener: PhotoOptionDialogListener
    // 3
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 4
        listener = activity as PhotoOptionDialogListener
        // 5
        var captureSelectIdx = -1
        var pickSelectIdx = -1
        // 6
        val options = ArrayList<String>()
        // 7
        val context = activity as Context
        // 8
        if (canCapture(context)) {
            options.add("Camera")
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }
        // 9
        if (canPick(context)) {
            options.add("Gallery")
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }
        // 10
        return AlertDialog.Builder(context)
            .setTitle("Photo Option")
            .setItems(options.toTypedArray<CharSequence>()) {
                _, which ->
                if (which == captureSelectIdx) {
                    // 11
                    listener.onCaptureClick()
                } else if (which == pickSelectIdx) {
                    // 12
                    listener.onCaptureClick()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        // 13
        fun canPick(context: Context): Boolean {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            return (pickIntent.resolveActivity(context.packageManager) != null)
        }

        // 14
        fun canCapture(context: Context): Boolean {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            return (captureIntent.resolveActivity(context.packageManager) != null)
        }

        // 15
        fun newInstance(context: Context): PhotoOptionDialogFragment? {
            // 16
            if (canPick(context) || canCapture(context)) {
                val frag = PhotoOptionDialogFragment()
                return frag
            } else {
                return null
            }
        }
    }
}