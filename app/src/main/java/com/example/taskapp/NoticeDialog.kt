package com.example.taskapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class NoticeDialog : DialogFragment()
{
    private lateinit var listener: NoticeDialogListener
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_message)
                .setPositiveButton(R.string.confirm_text_button) { dialog, id ->
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.cancel_text_button) { dialog, id ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("activity cannot be null")
    }
    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        try
        {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException)
        {
            throw ClassCastException(
                context.toString() +
                        "must implement NoticeDialogListener"
            )
        }
    }

    interface NoticeDialogListener
    {
        fun onDialogPositiveClick(dialog: DialogFragment?)
    }
}