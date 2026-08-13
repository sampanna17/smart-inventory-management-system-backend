package com.smartinventorysystem.modules.unit.mapper;

import com.smartinventorysystem.modules.unit.dto.request.CreateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.response.UnitResponse;
import com.smartinventorysystem.modules.unit.entity.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "unitId", ignore = true)
    Unit toEntity(CreateUnitRequest request);

    @Mapping(source = "unitId", target = "unitId")
    UnitResponse toResponse(Unit unit);

    List<UnitResponse> toResponseList(List<Unit> units);
}