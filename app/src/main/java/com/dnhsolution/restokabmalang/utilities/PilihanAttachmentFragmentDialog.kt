package com.dnhsolution.restokabmalang.utilities

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.sistem.produk.ProdukMasterActivity

class PilihanAttachmentFragmentDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Do all the stuff to initialize your custom view
        val view = inflater.inflate(R.layout.fragment_dialog_pilihan_attachment, container, false)
        val bCamera = view.findViewById(R.id.bCamera) as Button
        val bGambar = view.findViewById(R.id.bGambar) as Button
        val bVideo = view.findViewById(R.id.bVideo) as Button

        bVideo.visibility = View.GONE

        bCamera.setOnClickListener {
            (activity as ProdukMasterActivity).openCameraIntent()
            dismiss()
        }
        bGambar.setOnClickListener {
            (activity as ProdukMasterActivity).onPickPhoto()
            dismiss()
        }
        bVideo.setOnClickListener {
//            (activity as DetailTransaksiInboxActivity).onPickVideo()
            dismiss()
        }

        return view
    }
}