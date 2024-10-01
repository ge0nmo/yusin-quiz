package com.cpa.yusin.quiz.product.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.controller.port.ProductService;
import com.cpa.yusin.quiz.product.domain.Product;
import com.cpa.yusin.quiz.product.controller.mapper.ProductMapper;
import com.cpa.yusin.quiz.product.service.port.ProductRepository;
import com.cpa.yusin.quiz.product.service.port.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductServiceImpl implements ProductService
{
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    @Transactional
    @Override
    public ProductRegisterResponse save(ProductRegisterRequest request)
    {
        productValidator.validateDurationMonth(request.getDurationMonths());

        Product product = productMapper.toProductDomain(request);

        product = productRepository.save(product);
        return productMapper.toProductRegisterResponse(product);
    }

    @Transactional
    @Override
    public void update(long productId, ProductUpdateRequest request)
    {
        Product product = findById(productId);
        productValidator.validateDurationMonth(productId, request.getDurationMonths());

        product.update(request);
        productRepository.save(product);
    }

    @Override
    public Product findById(Long id)
    {
        return productRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductDTO getById(Long id)
    {
        return productMapper.toProductDTO(findById(id));
    }

    @Override
    public List<ProductDTO> getAll()
    {
        return productRepository.findAll().stream()
                .map(productMapper::toProductDTO)
                .sorted(Comparator.comparing(ProductDTO::getDurationMonth))
                .toList();
    }

    @Transactional
    @Override
    public void deleteById(Long id)
    {
        findById(id);

        productRepository.deleteById(id);
    }


}
