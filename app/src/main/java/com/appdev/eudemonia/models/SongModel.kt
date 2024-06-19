package com.appdev.eudemonia.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongModel(
    val coverUrl: String = "",
    val id: String = "",
    val subtitle: String = "",
    val title: String = "",
    val url: String = ""
) : Parcelable
