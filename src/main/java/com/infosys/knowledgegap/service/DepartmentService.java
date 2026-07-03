package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.DepartmentDto;
import java.util.List;

public interface DepartmentService {
    DepartmentDto create(DepartmentDto dto);
    DepartmentDto update(Long id, DepartmentDto dto);
    List<DepartmentDto> getAll();
    DepartmentDto getById(Long id);
    void delete(Long id);
}
