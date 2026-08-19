package com.smartinventorysystem.modules.unit.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.exceptions.DuplicateResourceException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.unit.dto.request.CreateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.request.UnitFilterRequest;
import com.smartinventorysystem.modules.unit.dto.request.UpdateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.response.UnitResponse;
import com.smartinventorysystem.modules.unit.entity.Unit;
import com.smartinventorysystem.modules.unit.mapper.UnitMapper;
import com.smartinventorysystem.modules.unit.repository.UnitRepository;
import com.smartinventorysystem.modules.unit.specification.UnitSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    @Override
    @Transactional
    public UnitResponse createUnit(CreateUnitRequest request) {

        if (unitRepository.existsByUnitName(request.getUnitName())) {
            throw new DuplicateResourceException("Unit already exists with name: " + request.getUnitName());
        }

        Unit unit = unitMapper.toEntity(request);

        return unitMapper.toResponse(unitRepository.save(unit));
    }

    @Override
    @Transactional
    public UnitResponse updateUnit(Integer unitId, UpdateUnitRequest request) {

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.UNIT_NOT_FOUND));

        if (request.getUnitName() != null && !request.getUnitName().isBlank()) {
            unit.setUnitName(request.getUnitName());
        }

        return unitMapper.toResponse(unitRepository.save(unit));
    }

    @Override
    @Transactional
    public void deleteUnit(Integer unitId) {

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.UNIT_NOT_FOUND));

        unitRepository.delete(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getUnitById(Integer unitId) {

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.UNIT_NOT_FOUND));

        return unitMapper.toResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        return unitMapper.toResponseList(unitRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnitResponse> getUnits(UnitFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Unit> specification = UnitSpecification.withFilters(request);

        Page<Unit> unitPage = unitRepository.findAll(specification, pageable);
        return PageResponse.of(unitPage, unitMapper::toResponse);
    }

    private Pageable createPageable(UnitFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "unitId";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "asc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "unitId";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "name", "unitname" -> "unitName";
            case "id", "unitid" -> "unitId";
            default -> "unitId";
        };
    }
}