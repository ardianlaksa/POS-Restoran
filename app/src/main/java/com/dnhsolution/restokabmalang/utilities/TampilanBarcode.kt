package com.dnhsolution.restokabmalang.utilities

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import com.dnhsolution.restokabmalang.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer

class TampilanBarcode {

    fun displayBitmap(context: Context, value: String) : Bitmap {
//        val widthPixels = resources.getDimensionPixelSize(R.dimen.width_barcode)
//        val heightPixels = resources.getDimensionPixelSize(R.dimen.height_barcode)
        val widthPixels = 250
        val heightPixels = 50

        return createBarcodeBitmap(
            barcodeValue = value,
            barcodeColor = getColor(context,R.color.colorPrimary),
            backgroundColor = getColor(context,android.R.color.white),
            widthPixels = widthPixels,
            heightPixels = heightPixels
        )
    }

    private fun createBarcodeBitmap(
        barcodeValue: String,
        @ColorInt barcodeColor: Int,
        @ColorInt backgroundColor: Int,
        widthPixels: Int,
        heightPixels: Int
    ): Bitmap {
        val bitMatrix = Code128Writer().encode(
            barcodeValue,
            BarcodeFormat.CODE_128,
            widthPixels,
            heightPixels
        )

        val pixels = IntArray(bitMatrix.width * bitMatrix.height)
        for (y in 0 until bitMatrix.height) {
            val offset = y * bitMatrix.width
            for (x in 0 until bitMatrix.width) {
                pixels[offset + x] =
                    if (bitMatrix.get(x, y)) barcodeColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(
            bitMatrix.width,
            bitMatrix.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.setPixels(
            pixels,
            0,
            bitMatrix.width,
            0,
            0,
            bitMatrix.width,
            bitMatrix.height
        )
        return bitmap
    }
}