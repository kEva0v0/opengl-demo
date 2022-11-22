package com.mashiro.filament.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mashiro.filament.model.NormalPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PointCloudViewModel: ViewModel() {

    private val pointCloudModel = PointCloudModel()

    val pointCloudListData : MutableLiveData<NormalPoint> = MutableLiveData()

    fun updateData() {
        viewModelScope.launch {
            val normalPoint = withContext(Dispatchers.IO) {
                val pt = pointCloudModel.getPointCloudData()
                val minZ = pt.minZ()
                pt.pointList.forEach {
                    it.z = it.z + abs(minZ) + 0.001f
                }
                pt
            }
            pointCloudListData.postValue(normalPoint)
        }
    }
}