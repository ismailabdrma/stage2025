package com.example.stage2025.service.impl;

import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.entity.ApiSupplier;
import com.example.stage2025.entity.Supplier;
import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.service.ProductFetcher;
import jakarta.xml.ws.BindingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SOAP fetcher adapter for supplier integration.
 * You must generate SOAP stubs from the supplier WSDL (SupplierPort, SoapProduct, etc).
 */
@Service
@RequiredArgsConstructor
public class SoapProductFetcher implements ProductFetcher {

    private final SupplierPort supplierPort;  // Inject your generated SOAP client port here

    @Override
    public List<SupplierProductDto> fetchProducts(Supplier supplier) throws Exception {
        if (!(supplier instanceof ApiSupplier api) || api.getDataFormat() != DataFormat.SOAP) {
            throw new IllegalArgumentException("Not a SOAP supplier");
        }
        // Optional: set credentials if needed
        if (api.getApiKey() != null && !api.getApiKey().isBlank()) {
            ((BindingProvider) supplierPort).getRequestContext()
                    .put(BindingProvider.USERNAME_PROPERTY, api.getApiKey());
        }
        // You must replace 'getProducts()' with the real SOAP operation name
        List<SoapProduct> soapList = supplierPort.getProducts();

        // Convert to your internal DTO
        return soapList.stream()
                .map(p -> new SupplierProductDto(
                        p.getExternalId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        List.of(p.getImages().split("\\|"))
                ))
                .collect(Collectors.toList());
    }
}
