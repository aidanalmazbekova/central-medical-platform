package kg.megalab.cmp.service.impl;

import kg.megalab.cmp.exception.exceptionType.AlreadyExistsException;
import kg.megalab.cmp.model.dto.request.CreateSpecialtyDto;
import kg.megalab.cmp.model.dto.response.SpecialtyResponseDto;
import kg.megalab.cmp.model.entity.Specialty;
import kg.megalab.cmp.repo.SpecialtyRepo;
import kg.megalab.cmp.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepo specialtyRepo;

    @Override
    public SpecialtyResponseDto createSpecialty(CreateSpecialtyDto dto) {
        String name = dto.name().trim();
        if (specialtyRepo.existsByNameIgnoreCase(name)) {
            throw new AlreadyExistsException("Специальность с таким названием уже существует");
        }
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setDescription(dto.description());
        Specialty savedSpecialty = specialtyRepo.save(specialty);
        return new SpecialtyResponseDto(savedSpecialty.getId(), savedSpecialty.getName(), savedSpecialty.getDescription());
    }

    @Override
    public List<SpecialtyResponseDto> getAllSpecialties() {
        return specialtyRepo.findAll().stream().map(specialty ->
                new SpecialtyResponseDto(specialty.getId(), specialty.getName(), specialty.getDescription())).toList();
    }

}
