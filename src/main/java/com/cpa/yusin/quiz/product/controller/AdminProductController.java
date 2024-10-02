package com.cpa.yusin.quiz.product.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.controller.port.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/admin/product")
@RequiredArgsConstructor
@RestController
public class AdminProductController
{
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<GlobalResponse<ProductRegisterResponse>> save(@Valid @RequestBody ProductRegisterRequest productRegisterRequest)
    {
        ProductRegisterResponse response = productService.save(productRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GlobalResponse<>(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProductDTO>> update(@Positive @PathVariable("id") Long id,
                                                             @Validated @RequestBody ProductUpdateRequest productUpdateRequest)
    {
        productService.update(id, productUpdateRequest);
        ProductDTO response = productService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProductDTO>> getById(@Positive @PathVariable("id") Long id)
    {
        ProductDTO response = productService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProductDTO>>> getAllProducts()
    {
        List<ProductDTO> response = productService.getAll();

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<?>> deleteById(@Positive @PathVariable("id") Long id)
    {
        productService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
