package com.vkas.spacelocker.basesl

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vkas.spacelocker.enevtsl.StateLiveData

open class BaseViewModel (application: Application) : BaseViewModelMVVM(application) {
    var stateLiveData: StateLiveData<Any> = StateLiveData()
    fun getStateLiveData(): MutableLiveData<Any> {
        return stateLiveData
    }
}