package com.cpa.yusin.quiz.product.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.controller.port.ProductService;
import com.cpa.yusin.quiz.product.domain.ProductDomain;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@RestController
public class ProductController
{
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<GlobalResponse<ProductRegisterResponse>> save(@RequestBody ProductRegisterRequest productRegisterRequest)
    {
        ProductRegisterResponse response = productService.save(productRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GlobalResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProductDTO>> getById(@Positive @PathVariable("id") Long id)
    {
        ProductDTO response = productService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
