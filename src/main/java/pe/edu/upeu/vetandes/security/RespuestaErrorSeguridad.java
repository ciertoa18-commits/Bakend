package pe.edu.upeu.vetandes.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.vetandes.dto.ErrorResponseDTO;

@Component
@RequiredArgsConstructor
public class RespuestaErrorSeguridad {
    private final ObjectMapper mapper;
    private final Clock clock;
    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(new ErrorResponseDTO(
                LocalDateTime.now(clock), status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI(), null)));
    }
}

