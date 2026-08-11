package com.smartinventorysystem.modules.unit.repository;

import com.smartinventorysystem.modules.unit.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer>, JpaSpecificationExecutor<Unit> {
    boolean existsByUnitName(String unitName);
}