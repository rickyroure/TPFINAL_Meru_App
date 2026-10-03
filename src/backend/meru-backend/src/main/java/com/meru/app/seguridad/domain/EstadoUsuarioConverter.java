package com.meru.app.seguridad.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoUsuarioConverter implements AttributeConverter<EstadoUsuario, String> {

    @Override
    public String convertToDatabaseColumn(EstadoUsuario estado) {
        if (estado == null) {
            return null;
        }
        return estado.name().toLowerCase();
    }

    @Override
    public EstadoUsuario convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EstadoUsuario.valueOf(dbData.toUpperCase());
    }
}
