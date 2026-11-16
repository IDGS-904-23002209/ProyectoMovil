package com.utl.nomi.data.model

// Modelos de los endpoints api/movil/* del servidor (MovilController).
// Los nombres coinciden con el JSON que regresa ASP.NET (camelCase).

// GET api/movil/inicio
data class InicioMovil(
    val empleadoId: Int,
    val nombre: String,
    val turno: String?,
    val horaEntradaTurno: String?,
    val horaSalidaTurno: String?,
    val sede: String?,
    val plantaLatitud: Double?,
    val plantaLongitud: Double?,
    val plantaRadioMetros: Int?,
    val hoyEntrada: String?,
    val hoySalida: String?,
    val hoyRetardo: Boolean,
    val hoyMinutosRetardo: Int,
    val semanaDias: Int,
    val semanaRetardos: Int,
    val semanaMinutos: Int
)

// POST api/movil/asistencia (lo que manda la app). No lleva el id del empleado:
// el servidor lo saca del token.
data class RegistroAsistenciaMovilRequest(
    val codigoQr: String,
    val latitud: Double,
    val longitud: Double
)

// POST api/movil/asistencia (respuesta cuando se registró)
data class RegistroAsistenciaMovil(
    val tipo: String,            // "ENTRADA" o "SALIDA"
    val hora: String,            // HH:mm
    val retardo: Boolean,
    val minutosRetardo: Int,
    val distanciaMetros: Int,
    val mensaje: String
)

// Cuerpo de las respuestas de error (400, 404, 500) de api/movil/*
data class ErrorApi(
    val codigo: Int?,
    val mensaje: String?,
    val distanciaMetros: Int?
)

// GET api/movil/recibos (cada elemento) y encabezado del detalle
data class ReciboMovil(
    val nominaId: Long,
    val periodo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val fechaPago: String?,
    val estadoPeriodo: String?,
    val estadoPago: String?,
    val diasPagados: Int?,
    val totalPercepciones: Double,
    val totalDeducciones: Double,
    val sueldoNeto: Double
)

data class ConceptoRecibo(
    val concepto: String,
    val tipo: String,            // "PERCEPCION" o "DEDUCCION"
    val cantidad: Double?,
    val montoTotal: Double
)

// GET api/movil/recibos/{id}
data class ReciboDetalleMovil(
    val recibo: ReciboMovil,
    val conceptos: List<ConceptoRecibo>
)

// ---------------- Solicitudes ----------------

// GET api/movil/solicitudes/tipos
data class TipoPermiso(
    val id: Int,
    val nombre: String,
    val conGoceSueldo: Boolean,
    val diasMaximos: Int?,
    val descripcion: String?
)

// GET api/movil/solicitudes (cada elemento)
data class SolicitudMovil(
    val id: Long,
    val tipo: String,
    val fechaSolicitud: String,
    val fechaInicio: String,
    val fechaFin: String,
    val dias: Int,
    val motivo: String?,
    val estado: String           // PENDIENTE, APROBADA, RECHAZADA o CANCELADA
)

// POST api/movil/solicitudes. Fechas en formato yyyy-MM-dd.
data class NuevaSolicitudRequest(
    val tipoPermisoId: Int,
    val fechaInicio: String,
    val fechaFin: String,
    val motivo: String
)

// Respuesta de POST api/movil/solicitudes (también la de error)
data class RespuestaSolicitud(
    val codigo: Int,
    val mensaje: String,
    val id: Long?
)

// ---------------- Notificaciones ----------------

data class NotificacionMovil(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String?,           // SOLICITUD, NOMINA, ...
    val leida: Boolean,
    val fechaCreacion: String?
)

// GET api/movil/notificaciones
data class NotificacionesMovil(
    val noLeidas: Int,
    val notificaciones: List<NotificacionMovil>
)
