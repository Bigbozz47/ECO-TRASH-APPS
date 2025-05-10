package com.example.eco_trash.ui.bantuan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BantuanViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Bantuan Fragment"
    }
    val text: LiveData<String> = _text
}