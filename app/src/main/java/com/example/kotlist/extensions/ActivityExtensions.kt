package com.example.kotlist.extensions

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.kotlist.databinding.ComponentCustomDialogBinding
import com.example.kotlist.R

fun Activity.showCustomDialog(
    title: String,
    message: String,
    positiveButtonText: String = "Confirmar",
    negativeButtonText: String = "Cancelar",
    onPositiveClick: () -> Unit,
    onNegativeClick: (() -> Unit)? = null
) {
    val dialogBinding = ComponentCustomDialogBinding.inflate(layoutInflater)

    dialogBinding.apply {
        customDialogTitle.text = title
        customDialogMessage.text = message
        customDialogPositiveButton.text = positiveButtonText
        customDialogNegativeButton.text = negativeButtonText
    }

    val dialog = AlertDialog.Builder(this)
        .setView(dialogBinding.root)
        .create()

    dialog.window?.apply {
        setBackgroundDrawableResource(android.R.color.transparent)
        setDimAmount(0.75f)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            attributes = attributes?.apply {
                blurBehindRadius = 20
            }
        }
    }

    dialogBinding.customDialogNegativeButton.setOnClickListener {
        onNegativeClick?.invoke()
        dialog.dismiss()
    }

    dialogBinding.customDialogPositiveButton.setOnClickListener {
        onPositiveClick()
        dialog.dismiss()
    }

    dialog.show()
}

fun Activity.showDeleteDialog(
    title: String,
    message: String,
    positiveButtonText: String = "Excluir",
    negativeButtonText: String = "Cancelar",
    onPositiveClick: () -> Unit,
    onNegativeClick: (() -> Unit)? = null
) {
    val dialogBinding = ComponentCustomDialogBinding.inflate(layoutInflater)

    dialogBinding.apply {
        customDialogTitle.text = title
        customDialogMessage.text = message
        customDialogPositiveButton.text = positiveButtonText
        customDialogNegativeButton.text = negativeButtonText

        customDialogPositiveButton.apply {
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.deleteButton)
            rippleColor = ContextCompat.getColorStateList(context, R.color.deleteButtonRipple)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    val dialog = AlertDialog.Builder(this)
        .setView(dialogBinding.root)
        .create()

    dialog.window?.apply {
        setBackgroundDrawableResource(android.R.color.transparent)
        setDimAmount(0.75f)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            attributes = attributes?.apply {
                blurBehindRadius = 20
            }
        }
    }

    dialogBinding.customDialogNegativeButton.setOnClickListener {
        onNegativeClick?.invoke()
        dialog.dismiss()
    }

    dialogBinding.customDialogPositiveButton.setOnClickListener {
        onPositiveClick()
        dialog.dismiss()
    }

    dialog.show()
}