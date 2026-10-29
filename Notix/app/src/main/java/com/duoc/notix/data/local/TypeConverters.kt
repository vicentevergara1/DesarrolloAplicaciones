package com.duoc.notix.data.local

import androidx.room.TypeConverter
import com.duoc.notix.model.EstadoDocumento
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.TipoFirma

class NotixTypeConverters {

    @TypeConverter
    fun fromTipoCuenta(value: TipoCuenta): String = value.name

    @TypeConverter
    fun toTipoCuenta(value: String): TipoCuenta = runCatching { TipoCuenta.valueOf(value) }.getOrDefault(TipoCuenta.PERSONA)

    @TypeConverter
    fun fromEstadoTramite(value: EstadoTramite): String = value.name

    @TypeConverter
    fun toEstadoTramite(value: String): EstadoTramite = runCatching { EstadoTramite.valueOf(value) }.getOrDefault(EstadoTramite.ENVIADO)

    @TypeConverter
    fun fromTipoFirma(value: TipoFirma): String = value.name

    @TypeConverter
    fun toTipoFirma(value: String): TipoFirma = runCatching { TipoFirma.valueOf(value) }.getOrDefault(TipoFirma.SIMPLE)

    @TypeConverter
    fun fromEstadoDocumento(value: EstadoDocumento): String = value.name

    @TypeConverter
    fun toEstadoDocumento(value: String): EstadoDocumento = runCatching { EstadoDocumento.valueOf(value) }.getOrDefault(EstadoDocumento.ACEPTADO)
}
