package com.util.nomi.data.model

import com.google.gson.annotations.SerializedName

data class AsistenciaDto(
    @SerializedName("empleadoId") val empleadoId: Int,
    @SerializedName("latitudEntrada") val latitudEntrada: Double,
    @SerializedName("longitudEntrada") val longitudEntrada: Double,
    @SerializedName("qrCode") val qrCode: String? = null
)