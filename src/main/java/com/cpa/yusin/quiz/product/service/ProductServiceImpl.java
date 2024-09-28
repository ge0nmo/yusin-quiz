package com.cpa.yusin.quiz.product.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.controller.port.ProductService;
import com.cpa.yusin.quiz.product.domain.ProductDomain;
import com.cpa.yusin.quiz.product.mapper.ProductMapper;
import com.cpa.yusin.quiz.product.service.port.ProductRepository;
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

    @Transactional
    @Override
    public ProductRegisterResponse save(ProductRegisterRequest request)
    {
        ProductDomain product = productMapper.toProductDomain(request);

        product = productRepository.save(product);
        return productMapper.toProductRegisterResponse(product);
    }

    @Override
    public void update(long productId, ProductUpdateRequest request)
    {
        ProductDomain product = findById(productId);

        product.update(request);
        productRepository.save(product);
    }

    @Override
    public ProductDomain findById(Long id)
    {
        return productRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }

    @Override
    public ProductDTO getById(Long id)
    {
        return productMapper.toProductDTO(findById(id));
    }

    @Override
    public List<ProductDTO> findAll()
    {
        return productRepository.findAll().stream()
                .map(productMapper::toProductDTO)
                .sorted(Comparator.comparing(ProductDTO::getDurationMonth))
                .toList();
    }

    @Override
    public boolean deleteById(Long id)
    {
        productRepository.deleteById(id);

        return !productRepository.existsById(id);
    }


}
