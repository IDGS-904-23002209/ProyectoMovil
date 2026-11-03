package com.utl.nomi.data.model

data class NominaDto(
    val id: Long,
    val periodoId: Int,
    val empleadoId: Int,
    val nombre: String?,
    val apellidoPaterno: String?,
    val apellidoMaterno: String?,
    val rfc: String?,
    val diasTrabajados: Int,
    val diasPagados: Int,
    val horasExtraTotales: Double,
    val faltasTotales: Int,
    val retardosTotales: Int,
    val totalPercepciones: Double,
    val totalDeducciones: Double,
    val sueldoNeto: Double,
    val estadoPago: String
)