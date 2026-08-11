package com.smartinventorysystem.modules.unit.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.unit.dto.request.CreateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.request.UnitFilterRequest;
import com.smartinventorysystem.modules.unit.dto.request.UpdateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.response.UnitResponse;
import java.util.List;

public interface UnitService {
    UnitResponse createUnit(CreateUnitRequest request);
    UnitResponse updateUnit(Integer unitId, UpdateUnitRequest request);
    void deleteUnit(Integer unitId);
    UnitResponse getUnitById(Integer unitId);
    List<UnitResponse> getAllUnits();
    PageResponse<UnitResponse> getUnits(UnitFilterRequest request);
}
