package pe.edu.upeu.vetandes.service.impl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.PropietarioResponseDTO;
import pe.edu.upeu.vetandes.mapper.PropietarioMapper;
import pe.edu.upeu.vetandes.repository.PropietarioRepository;
import pe.edu.upeu.vetandes.service.PropietarioService;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropietarioServiceImpl implements PropietarioService {
    private final PropietarioRepository repository;
    private final PropietarioMapper mapper;
    public List<PropietarioResponseDTO> listar() {
        return repository.findAll(Sort.by("nombres")).stream().map(mapper::toResponse).toList();
    }
}

