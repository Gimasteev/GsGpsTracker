package com.galileosky.gsgpstracker.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.galileosky.gsgpstracker.R

object DialogManager{
    fun showLocEnableDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_positive)){
            _, _ -> listener.onClick()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.button_negative)){
                _, _ -> dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick(){
        }
    }
}

