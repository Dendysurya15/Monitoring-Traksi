package com.cbi.monitoring_traksi.ui.viewModel

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.data.repository.CameraRepository
import com.google.android.material.button.MaterialButton
import java.io.File

class CameraViewModel(private val cameraRepository: CameraRepository) : ViewModel() {

    fun takeCameraPhotos(resultCode : String, imageView: ImageView, pageForm : Int, deletePhoto : View?,komentar :String, kodeFoto:String) {
        cameraRepository.takeCameraPhotos(resultCode, imageView, pageForm, deletePhoto,komentar, kodeFoto)
    }

    fun statusCamera(): Boolean = cameraRepository.statusCamera()

    fun closeCamera() {
        cameraRepository.closeCamera()
    }

    fun openZoomPhotos(file: File, function: () -> Unit) {
        cameraRepository.openZoomPhotos(file, function)
    }

    fun closeZoomPhotos() {
        cameraRepository.closeZoomPhotos()
    }


    fun deletePhotoSelected(fname :String): Boolean{
        return cameraRepository.deletePhotoSelected(fname)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val cameraRepository: CameraRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                return CameraViewModel(cameraRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
