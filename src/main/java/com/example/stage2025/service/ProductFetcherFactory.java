package com.example.stage2025.service;

import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.entity.ApiSupplier;
import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.service.parser.ProductParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFetcherFactory {

    private final WebClient web;
    private final List<ProductParser> parsers;

    // Fetch all products from a supplier (REST, CSV, XML, Excel)
    public List<SupplierProductDto> fetch(ApiSupplier s) throws Exception {
        return parsePayload(s, s.getProductsEndpoint());
    }

    // Fetch a single product by externalId (REST-style only)
    public SupplierProductDto fetchOne(ApiSupplier s, String externalId) throws Exception {
        List<SupplierProductDto> list = parsePayload(
                s,
                s.getSingleEndpoint().replace("{externalId}", externalId));
        if (list.isEmpty()) throw new IllegalStateException("Product not found in supplier feed");
        return list.get(0);
    }

    // --- Internal utility ---

    private List<SupplierProductDto> parsePayload(ApiSupplier s, String path) throws Exception {
        byte[] payload = web.get()
                .uri(s.getApiUrl() + path)
                .header(HttpHeaders.AUTHORIZATION, authHeader(s))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
        return parserFor(s.getDataFormat()).parse(payload);
    }

    private ProductParser parserFor(DataFormat fmt) {
        return parsers.stream()
                .filter(p -> p.supports() == fmt)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No parser for " + fmt));
    }

    private String authHeader(ApiSupplier s) {
        if (s.getApiKey() == null) return "";
        return "BEARER".equalsIgnoreCase(s.getAuthMethod())
                ? "Bearer " + s.getApiKey()
                : s.getApiKey();
    }
}
