package com.kotlist.app.extensions

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.text.InputType
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kotlist.app.databinding.ComponentCustomDialogBinding
import com.kotlist.app.R
import com.kotlist.app.databinding.ComponentCustomInputDialogBinding

data class DialogInputConfig(
    val label: String,
    val hint: String = "",
    val initialValue: String = "",
    val inputType: Int = InputType.TYPE_CLASS_TEXT,
    val maxLength: Int? = null,
    val errorMessage: String? = null,
    val validator: ((String) -> Boolean)? = null
)

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

fun Activity.showCustomInputDialog(
    title: String,
    message: String,
    positiveButtonText: String = "Confirmar",
    negativeButtonText: String = "Confirmar",
    inputConfig: DialogInputConfig,
    onPositiveClick: (inputText: String) -> Unit,
    onNegativeClick: (() -> Unit)? = null
): Pair<AlertDialog, ComponentCustomInputDialogBinding> {
    val dialogBinding = ComponentCustomInputDialogBinding.inflate(layoutInflater)

    dialogBinding.apply {
        customInputDialogTitle.text = title
        customInputDialogMessage.text = message
        customInputDialogPositiveButton.text = positiveButtonText
        customInputDialogNegativeButton.text = negativeButtonText

        customInputDialogInputWrapper.hint = inputConfig.label
        customInputDialogInput.setText(inputConfig.initialValue)
        customInputDialogInput.inputType = inputConfig.inputType
    }

    val dialog = AlertDialog.Builder(this)
        .setView(dialogBinding.root)
        .setCancelable(true)
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

    dialogBinding.customInputDialogNegativeButton.setOnClickListener {
        onNegativeClick?.invoke()
        dialog.dismiss()
    }

    dialogBinding.customInputDialogPositiveButton.setOnClickListener {
        val inputText = dialogBinding.customInputDialogInput.text.toString()

        if(inputConfig.validator != null) {
            val isValid = inputConfig.validator.invoke(inputText)

            if(!isValid) {
                dialogBinding.customInputDialogInputWrapper.error = inputConfig.errorMessage ?: "Valor inválido"
                return@setOnClickListener
            }
        }

        dialogBinding.customInputDialogInputWrapper.error = null
        onPositiveClick(inputText)
    }

    dialog.show()

    return dialog to dialogBinding
}

fun placeholderIdToDrawable(placeholderId: Int): Int {
    return when (placeholderId) {
        0 -> R.drawable.placeholder_img_list_0
        1 -> R.drawable.placeholder_img_list_1
        2 -> R.drawable.placeholder_img_list_2
        else -> R.drawable.placeholder_img_list_0
    }
}