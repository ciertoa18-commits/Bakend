package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/*
 * Convierte el Boolean de la entidad al NUMBER(1) de la tabla.
 *
 * Por que hace falta: el script del anexo declara las banderas como
 * NUMBER(1) DEFAULT 1 (prestacion.activo, usuarios.activo). Oracle
 * 23ai si tiene un tipo BOOLEAN propio, asi que Hibernate, al ver un
 * campo Boolean, espera encontrar una columna BOOLEAN y al arrancar
 * falla con:
 *
 *   Schema validation: wrong column type encountered in column
 *   [activo] in table [prestacion]; found [number (Types#NUMERIC)],
 *   but expecting [boolean (Types#BOOLEAN)]
 *
 * Con este convertidor la entidad sigue trabajando con un Boolean
 * limpio, la columna sigue siendo 0/1 como pide el enunciado y la
 * validacion del esquema cuadra. La alternativa habria sido cambiar
 * el script a BOOLEAN, pero el enunciado pide usarlo tal cual.
 *
 * Un NULL en la base se lee como false: la restriccion NOT NULL
 * impide que ocurra, pero asi el metodo nunca devuelve null.
 */
@Converter
public class BooleanNumericoConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer valor) {
        return valor != null && valor.intValue() == 1;
    }
}
