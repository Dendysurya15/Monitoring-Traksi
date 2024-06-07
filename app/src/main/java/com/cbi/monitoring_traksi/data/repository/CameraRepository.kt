package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.utils.AppUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import es.dmoral.toasty.Toasty

import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_layout_activity_informasi_unit
import kotlinx.android.synthetic.main.activity_layout_form_p2h.view.id_layout_activity_informasi_unit
import kotlinx.android.synthetic.main.camera_view_layout.view.captureCam
import kotlinx.android.synthetic.main.camera_view_layout.view.rlCamera
import kotlinx.android.synthetic.main.camera_view_layout.view.switchButton
import kotlinx.android.synthetic.main.camera_view_layout.view.torchButton
import kotlinx.android.synthetic.main.edit_foto_layout.view.fotoZoom
import kotlinx.android.synthetic.main.layout_foto_unit.view.ivAddFotoUnit

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class CameraRepository(private val context: Context, private val window: Window, private val view: View, private val zoomView: View) {

    interface PhotoCallback {
        fun onPhotoTaken(photoFile: File, fname: String, resultCode: String, pageForm: Int)
    }
    private var photoCallback: PhotoCallback? = null

    private var lastCameraId = 0
    private var rotatedCam = false
    private val aspectRatio = Rational(16, 9)
    private var selectedSize: Size? = null
    private lateinit var capReq: CaptureRequest.Builder
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private lateinit var cameraManager: CameraManager
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var textureViewCam: TextureView
    private var isCameraOpen = false
    private var isFlashlightOn = false

    fun setPhotoCallback(callback: PhotoCallback) {
        this.photoCallback = callback
    }

    private fun rotateBitmapOrientation(photoFilePath: String?): Bitmap {
        // Create and configure BitmapFactory
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoFilePath, bounds)
        val opts = BitmapFactory.Options()
        val bm = BitmapFactory.decodeFile(photoFilePath, opts)
        // Read EXIF Data
        var exif: ExifInterface? = null
        try {
            exif = photoFilePath?.let { ExifInterface(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientString: String? = exif?.getAttribute(ExifInterface.TAG_ORIENTATION)
        val orientation =
            orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
        var rotationAngle = 0
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotationAngle = 90
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotationAngle = 180
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotationAngle = 270
            }
        }
        // Rotate Bitmap
        val matrix = Matrix()
        matrix.setRotate(
            rotationAngle.toFloat(),
            bm.width.toFloat() / 2,
            bm.height.toFloat() / 2
        )
        // Return result
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true)
    }

    private fun addToGallery(photoFile: File) {
        val galleryIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val picUri = Uri.fromFile(photoFile)
        galleryIntent.data = picUri
        context.sendBroadcast(galleryIntent)
    }

    private fun addWatermark(bitmap: Bitmap, watermarkText: String): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val textPaint = Paint()
        textPaint.color = Color.YELLOW
        textPaint.textSize = if (width > height) {
            width
        } else {
            height
        } / 32f
        textPaint.textAlign = Paint.Align.RIGHT
//        textPaint.typeface = ResourcesCompat.getFont(context, R.font.helvetica)

        val backgroundPaint = Paint()
        backgroundPaint.color = Color.parseColor("#3D000000") // Black color with 25% transparency

        val watermarkLines = watermarkText.split("\n")
        val textHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top

        val x = width - width / 40f
        val y = height - (textHeight * watermarkLines.size) - height / 40f

        var maxWidth = 0f
        for (line in watermarkLines) {
            val lineWidth = textPaint.measureText(line)
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth
            }
        }

        val backgroundWidth = maxWidth
        for (i in watermarkLines.indices) {
            val line = watermarkLines[i]
            val lineY = y + (textHeight * (i + 1))
            canvas.drawRect(x - backgroundWidth, lineY - textHeight, x, lineY, backgroundPaint)
            canvas.drawText(line, x, lineY, textPaint)
        }

        return resultBitmap
    }

    fun takeCameraPhotos(resultCode: String, imageView: ImageView, pageForm : Int) {
        // Initialize Camera View
        val rootDCIM = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Monitoring-Traksi"
        ).toString()

        val rootApp = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        textureViewCam = TextureView(context)
        view.rlCamera.addView(textureViewCam)

        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread!!.start()
        handler = Handler((handlerThread)!!.looper)

        textureViewCam.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                @SuppressLint("MissingPermission")
                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    // Open View of Camera
                    cameraManager.openCamera(
                        cameraManager.cameraIdList[lastCameraId],
                        object : CameraDevice.StateCallback() {
                            @SuppressLint("SimpleDateFormat")
                            override fun onOpened(p0: CameraDevice) {
                                var fileName = ""
                                lateinit var file: File

                                cameraDevice = p0
                                isCameraOpen = true
                                capReq =
                                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                                val characteristics =
                                    cameraManager.getCameraCharacteristics(cameraDevice!!.id)
                                val streamConfigurationMap =
                                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                val outputSizes = streamConfigurationMap?.getOutputSizes(
                                    ImageFormat.JPEG
                                )

                                for (size in outputSizes!!) {
                                    val rational = Rational(size.width, size.height)
                                    if (rational == aspectRatio) {
                                        selectedSize = size
                                        break
                                    }
                                }

                                selectedSize?.let { size ->
                                    val surfaceTexture = textureViewCam.surfaceTexture
                                    surfaceTexture!!.setDefaultBufferSize(size.width, size.height)
                                    val surface = Surface(surfaceTexture)
                                    capReq.addTarget(surface)

                                    view.torchButton.apply {
                                        setOnClickListener {
                                            isFlashlightOn = !isFlashlightOn
                                            if (isFlashlightOn) {
                                                view.torchButton.setBackgroundResource(R.drawable.ic_lightning_on)
                                                view.torchButton.backgroundTintList =
                                                    ColorStateList.valueOf(Color.YELLOW)
                                                capReq.set(
                                                    CaptureRequest.FLASH_MODE,
                                                    CaptureRequest.FLASH_MODE_TORCH
                                                )
                                            } else {
                                                setDefaultIcon(view)
                                                capReq.set(
                                                    CaptureRequest.FLASH_MODE,
                                                    CaptureRequest.FLASH_MODE_OFF
                                                )
                                            }
                                            cameraCaptureSession!!.setRepeatingRequest(
                                                capReq.build(),
                                                null,
                                                null
                                            )
                                        }
                                    }

                                    view.switchButton.apply {
                                        setOnClickListener {
                                            isFlashlightOn = false
                                            rotatedCam = true
                                            closeCamera()
                                            setDefaultIcon(view)

                                            lastCameraId = if (lastCameraId == 0) {
                                                1
                                            } else {
                                                0
                                            }

                                            takeCameraPhotos(resultCode, imageView, pageForm)
                                        }
                                    }

                                    imageReader =
                                        ImageReader.newInstance(
                                            size.width,
                                            size.height,
                                            ImageFormat.JPEG,
                                            1
                                        )
                                    imageReader!!.setOnImageAvailableListener({ p0 ->
                                        val image = p0?.acquireLatestImage()
                                        var fileDCIM: File? = null
                                        if (image != null) {
                                            val buffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)

                                            var dirDCIM:File
                                            var dirApp: File
                                            if (pageForm == -1){
                                                dirApp = File(rootApp, "LaporP2H")
                                                dirApp.mkdirs()
                                                dirDCIM = File(rootDCIM, "LaporP2H")
                                                dirDCIM.mkdirs()
                                            }else{
                                                dirApp = File(rootApp, "FolderPerPertanyaanP2H")
                                                dirApp.mkdirs()
                                                dirDCIM = File(rootDCIM, "FolderPerPertanyaanP2H")
                                                dirDCIM.mkdirs()
                                            }

//                                            val dirApp = File(rootApp)
//                                            dirApp.mkdirs()
//                                            val dirDCIM = File(rootDCIM)
//                                            dirDCIM.mkdirs()

                                            val dateFormat =
                                                SimpleDateFormat("yyyyMdd_HHmmss").format(Calendar.getInstance().time)
                                            fileName = "MT_${dateFormat}.jpg"
                                            file = File(dirApp, fileName)

                                            fileDCIM = File(dirDCIM, fileName)
                                            if (fileDCIM.exists()) fileDCIM.delete()
                                            addToGallery(fileDCIM)

                                            if (file.exists()) file.delete()
                                            addToGallery(file)

                                            val opStream = FileOutputStream(file)
                                            opStream.write(bytes)
                                            opStream.close()
                                            image.close()
                                        } else {
                                            Toasty.error(
                                                context,
                                                "Unable to capture image. Please try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            closeCamera()
                                        }

                                        val takenImage = rotateBitmapOrientation(file.path)
                                        val dateWM = SimpleDateFormat(
                                            "dd MMMM yyyy HH:mm:ss",
                                            Locale("id", "ID")
                                        ).format(Calendar.getInstance().time)

                                        var commentWm = "comment"
                                        commentWm = commentWm.replace("|", ",").replace("\n", "")
                                        commentWm = AppUtils.splitStringWatermark(commentWm, 60)
                                        val watermarkText = if (resultCode == "0") {
                                            "MONITORING TRAKSI\n${dateWM}"
                                        } else {
                                            "MONITORING TRAKSI\n${commentWm}\n${dateWM}"
                                        }

                                        val watermarkedBitmap = addWatermark(takenImage, watermarkText)

                                        try {
                                            val targetSizeBytes = 100 * 1024
                                            var quality = 100
                                            val minQuality = 50
                                            val maxWidth = 1024

                                            val sourceWidth = watermarkedBitmap.width
                                            val sourceHeight = watermarkedBitmap.height

                                            val maxHeight =
                                                (maxWidth.toFloat() / sourceWidth.toFloat() * sourceHeight).toInt()

                                            var scaledBitmap: Bitmap? = null

                                            while (true) {
                                                if (quality <= minQuality || sourceWidth <= maxWidth || sourceHeight <= maxHeight) {
                                                    break
                                                }

                                                val aspectRatio =
                                                    sourceWidth.toFloat() / sourceHeight.toFloat()
                                                val newWidth =
                                                    maxWidth.coerceAtMost((maxHeight * aspectRatio).toInt())
                                                scaledBitmap = Bitmap.createScaledBitmap(
                                                    watermarkedBitmap,
                                                    newWidth,
                                                    maxHeight,
                                                    true
                                                )

                                                val outputStream = ByteArrayOutputStream()
                                                scaledBitmap.compress(
                                                    Bitmap.CompressFormat.JPEG,
                                                    quality,
                                                    outputStream
                                                )

                                                if (outputStream.size() > targetSizeBytes) {
                                                    quality -= 5
                                                } else {
                                                    break
                                                }
                                            }

                                            try {
                                                val out = FileOutputStream(file)
                                                scaledBitmap?.compress(
                                                    Bitmap.CompressFormat.JPEG,
                                                    quality,
                                                    out
                                                )
                                                out.flush()
                                                out.close()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }

                                        try {
                                            val outDCIM = FileOutputStream(fileDCIM)
                                            watermarkedBitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                100,
                                                outDCIM
                                            )
                                            outDCIM.flush()
                                            outDCIM.close()
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }

                                        mainHandler.post {
                                            rotatedCam = false
                                            closeCamera()

                                            Glide.with(context).load(Uri.fromFile(file))
                                                .diskCacheStrategy(
                                                    DiskCacheStrategy.NONE
                                                ).skipMemoryCache(true).centerCrop()
                                                .into(imageView)


                                            photoCallback?.onPhotoTaken(file, fileName, resultCode, pageForm)
                                        }
                                    }, handler)

                                    cameraDevice!!.createCaptureSession(
                                        listOf(surface, imageReader!!.surface),
                                        object : CameraCaptureSession.StateCallback() {
                                            override fun onConfigured(p0: CameraCaptureSession) {
                                                cameraCaptureSession = p0
                                                cameraCaptureSession!!.setRepeatingRequest(
                                                    capReq.build(),
                                                    null,
                                                    null
                                                )
                                            }

                                            override fun onConfigureFailed(p0: CameraCaptureSession) {

                                            }
                                        },
                                        handler
                                    )
                                }
                            }

                            override fun onDisconnected(p0: CameraDevice) {

                            }

                            override fun onError(p0: CameraDevice, p1: Int) {

                            }
                        },
                        handler
                    )
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return true
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

                }

            }

        // Take Photos
        view.captureCam.apply {
            setOnClickListener {
                capReq =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader!!.surface)
                cameraCaptureSession?.capture(capReq.build(), null, null)
            }
        }
    }

    fun statusCamera(): Boolean {
        rotatedCam = false
        return isCameraOpen
    }

    fun closeCamera() {
        if (isCameraOpen) {
            view.rlCamera.removeView(textureViewCam)

            cameraCaptureSession!!.close()
            cameraCaptureSession!!.device.close()
            cameraCaptureSession = null

            cameraDevice!!.close()
            cameraDevice = null

            imageReader!!.close()
            imageReader = null

            handlerThread!!.quitSafely()
            handlerThread = null
            handler = null

            isCameraOpen = false

            if (!rotatedCam) {
                view.visibility = View.GONE
            }

            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun openZoomPhotos(file: File, function: () -> Unit) {
        YoYo.with(Techniques.FadeIn)
            .onStart {
                Glide.with(context).load(Uri.fromFile(file)).diskCacheStrategy(
                    DiskCacheStrategy.NONE
                ).skipMemoryCache(true).into(zoomView.fotoZoom)
                function()
            }
            .duration(500)
            .repeat(0)
            .playOn(zoomView)
    }

    fun closeZoomPhotos() {
        YoYo.with(Techniques.FadeOut)
            .onEnd {
                zoomView.visibility = View.GONE
            }
            .duration(500)
            .repeat(0)
            .playOn(zoomView)
    }

    private fun setDefaultIcon(view: View) {
        view.torchButton.setBackgroundResource(R.drawable.ic_lightning_off)
        view.torchButton.backgroundTintList =
            ColorStateList.valueOf(Color.WHITE)
    }
}