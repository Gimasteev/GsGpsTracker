package com.galileosky.gsgpstracker.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.galileosky.gsgpstracker.R

object DialogManager{
    fun showLocEnableDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_positive)){
            _, _ -> Toast.makeText(context, R.string.button_positive, Toast.LENGTH_SHORT).show()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.button_negative)){
                _, _ ->
            Toast.makeText(context, R.string.button_negative, Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }
}

