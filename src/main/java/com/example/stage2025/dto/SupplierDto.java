package com.example.stage2025.dto;

import com.example.stage2025.enums.PayoutFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SupplierDto {
    private Long id;
    private String name;
    private boolean active;
    private String type;
    private String apiUrl;
    private String apiKey;
    private String authMethod;
    private String fileName;
    private String filePath;
    private LocalDateTime created;
    private LocalDateTime lastImport;

    private PayoutFrequency payoutFrequency;


}