package com.cbi.monitoring_traksi.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.ui.view.MainActivity
import kotlinx.android.synthetic.main.alert_dialog_view.view.llButtonDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.lottieDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.mbCancelDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.mbSuccessDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.tvDescDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.tvTitleDialog
import kotlinx.android.synthetic.main.alert_dialog_view.view.viewDialog

class AlertDialogUtility {
    companion object {
        @RequiresApi(Build.VERSION_CODES.R)
        @SuppressLint("InflateParams")
        fun alertDialog(context: Context, titleText: String, alertText: String, animAsset: String) {
            if (context is Activity && !context.isFinishing) {
                val rootView = context.findViewById<View>(android.R.id.content)
                val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                val layoutBuilder =
                    LayoutInflater.from(context).inflate(R.layout.alert_dialog_view, parentLayout)

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(context).setView(layoutBuilder)
                val alertDialog: AlertDialog = builder.create()

                layoutBuilder.lottieDialog.setAnimation(animAsset)
                layoutBuilder.lottieDialog.loop(true)
                layoutBuilder.lottieDialog.playAnimation()
                layoutBuilder.llButtonDialog.visibility = View.GONE
                layoutBuilder.viewDialog.visibility = View.VISIBLE
                layoutBuilder.tvTitleDialog.text = titleText
                layoutBuilder.tvDescDialog.text = alertText

                if (alertDialog.window != null) {
                    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }

                alertDialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
//                    AppUtils.checkCameraPermissions(context as MainActivity)
//                    AppUtils.checkReadAndWriteExternalCameraPermissions(context as MainActivity)
                    AppUtils.checkGeneralPermissions(context, context as Activity)
                }, 2000)
            }
        }

        @SuppressLint("InflateParams")
        fun alertDialogAction(context: Context, titleText: String, alertText: String, animAsset: String, function: () -> Unit) {
            if (context is Activity && !context.isFinishing) {
                val rootView = context.findViewById<View>(android.R.id.content)
                val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                val layoutBuilder =
                    LayoutInflater.from(context).inflate(R.layout.alert_dialog_view, parentLayout)

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(context).setView(layoutBuilder)
                val alertDialog: AlertDialog = builder.create()

                layoutBuilder.lottieDialog.setAnimation(animAsset)
                layoutBuilder.lottieDialog.loop(true)
                layoutBuilder.lottieDialog.playAnimation()
                layoutBuilder.llButtonDialog.visibility = View.GONE
                layoutBuilder.viewDialog.visibility = View.VISIBLE
                layoutBuilder.tvTitleDialog.text = titleText
                layoutBuilder.tvDescDialog.text = alertText

                if (alertDialog.window != null) {
                    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }

                alertDialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog.dismiss()
                    function()
                }, 2000)
            }
        }

        @SuppressLint("InflateParams")
        fun withTwoActions(context: Context, actionText: String, titleText: String, alertText: String, animAsset: String, function: () -> Unit) {
            if (context is Activity && !context.isFinishing) {
                val rootView = context.findViewById<View>(android.R.id.content)
                val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                val layoutBuilder =
                    LayoutInflater.from(context).inflate(R.layout.alert_dialog_view, parentLayout)

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(context).setView(layoutBuilder).setCancelable(false)
                val alertDialog: AlertDialog = builder.create()

                layoutBuilder.lottieDialog.setAnimation(animAsset)
                layoutBuilder.lottieDialog.loop(true)
                layoutBuilder.lottieDialog.playAnimation()
                layoutBuilder.tvTitleDialog.text = titleText
                layoutBuilder.tvDescDialog.text = alertText
                layoutBuilder.mbSuccessDialog.text = actionText
                layoutBuilder.mbSuccessDialog.setOnClickListener {
                    alertDialog.dismiss()
                    function()
                }
                layoutBuilder.mbCancelDialog.setOnClickListener {
                    alertDialog.dismiss()
                }

                if (alertDialog.window != null) {
                    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }

                alertDialog.show()
            }
        }

        @SuppressLint("InflateParams")
        fun withSingleAction(context: Context, actionText: String, titleText: String, alertText: String, animAsset: String, function: () -> Unit) {
            if (context is Activity && !context.isFinishing) {
                val rootView = context.findViewById<View>(android.R.id.content)
                val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                val layoutBuilder =
                    LayoutInflater.from(context).inflate(R.layout.alert_dialog_view, parentLayout)

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(context).setView(layoutBuilder).setCancelable(false)
                val alertDialog: AlertDialog = builder.create()

                layoutBuilder.mbCancelDialog.visibility = View.GONE
                layoutBuilder.lottieDialog.setAnimation(animAsset)
                layoutBuilder.lottieDialog.loop(true)
                layoutBuilder.lottieDialog.playAnimation()
                layoutBuilder.tvTitleDialog.text = titleText
                layoutBuilder.tvDescDialog.text = alertText
                layoutBuilder.mbSuccessDialog.text = actionText
                layoutBuilder.mbSuccessDialog.setOnClickListener {
                    alertDialog.dismiss()
                    function()
                }

                if (alertDialog.window != null) {
                    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }

                alertDialog.show()
            }
        }
    }
}