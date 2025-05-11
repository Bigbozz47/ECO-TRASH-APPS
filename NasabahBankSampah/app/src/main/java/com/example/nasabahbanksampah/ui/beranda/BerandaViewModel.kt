package com.example.nasabahbanksampah.ui.beranda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BerandaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Beranda"
    }
    val text: LiveData<String> = _text
}