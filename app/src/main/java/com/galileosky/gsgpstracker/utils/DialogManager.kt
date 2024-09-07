package com.galileosky.gsgpstracker.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.galileosky.gsgpstracker.R
import com.galileosky.gsgpstracker.databinding.SaveDialogBinding

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

    // логика диалога сохранения трека
    fun showSaveDialog(context: Context, listener: Listener){
        // Обращаюсь к AlertDialog т.к. через него проще делать воспроизведение кастомного диалога
        val builder = AlertDialog.Builder(context)
        // собственную разметку добавляем
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            bSave.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener{
                dialog.dismiss()
            }
        }
        // Убираю стандартную подложку
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    interface Listener{
        fun onClick(){
        }
    }
}

