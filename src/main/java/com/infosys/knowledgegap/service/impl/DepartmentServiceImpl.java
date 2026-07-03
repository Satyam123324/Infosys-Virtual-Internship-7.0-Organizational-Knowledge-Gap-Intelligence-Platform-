package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.DepartmentDto;
import com.infosys.knowledgegap.entity.Department;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.exception.DuplicateResourceException;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.DepartmentRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public DepartmentDto create(DepartmentDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Department already exists: " + dto.getName());
        }
        Department dept = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        if (dto.getDepartmentHeadId() != null) {
            User head = userRepository.findById(dto.getDepartmentHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department head user not found"));
            dept.setDepartmentHead(head);
        }
        return toDto(departmentRepository.save(dept));
    }

    @Override
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department dept = findById(id);
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        if (dto.getDepartmentHeadId() != null) {
            User head = userRepository.findById(dto.getDepartmentHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department head user not found"));
            dept.setDepartmentHead(head);
        }
        return toDto(departmentRepository.save(dept));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAll() {
        return departmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private DepartmentDto toDto(Department d) {
        return DepartmentDto.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .departmentHeadId(d.getDepartmentHead() != null ? d.getDepartmentHead().getId() : null)
                .departmentHeadName(d.getDepartmentHead() != null ? d.getDepartmentHead().getFullName() : null)
                .build();
    }
}
