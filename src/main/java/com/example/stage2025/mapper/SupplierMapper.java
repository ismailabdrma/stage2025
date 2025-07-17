package com.example.stage2025.mapper;

import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.entity.Supplier;

import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.entity.ApiSupplier;
import com.example.stage2025.entity.ExcelSupplier;
import com.example.stage2025.entity.Supplier;

public class SupplierMapper {

    public static SupplierDto toDto(Supplier supplier) {
        SupplierDto dto = new SupplierDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setActive(supplier.isActive());
        dto.setCreated(supplier.getCreated());
        dto.setLastImport(supplier.getLastImport());

        if (supplier instanceof ApiSupplier api) {
            dto.setType("API");
            dto.setApiUrl(api.getApiUrl());
            dto.setApiKey(api.getApiKey());
            dto.setAuthMethod(api.getAuthMethod());
        } else if (supplier instanceof ExcelSupplier excel) {
            dto.setType("EXCEL");
            dto.setFileName(excel.getFileName());
            dto.setFilePath(excel.getFilePath());
        } else {
            dto.setType("BASE");
        }

        return dto;
    }

}

