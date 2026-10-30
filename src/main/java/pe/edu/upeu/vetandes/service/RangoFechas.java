package pe.edu.upeu.vetandes.service;
import java.time.LocalDate;
import pe.edu.upeu.vetandes.exception.DatosInvalidosException;
public final class RangoFechas {
    private RangoFechas() {}
    public static void validar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new DatosInvalidosException("La fecha desde no puede ser posterior a hasta");
        if (hasta != null && hasta.equals(LocalDate.MAX))
            throw new DatosInvalidosException("La fecha hasta esta fuera de rango");
    }
}

