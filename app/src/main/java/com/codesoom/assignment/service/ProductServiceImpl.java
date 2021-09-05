package com.codesoom.assignment.service;

import com.codesoom.assignment.domain.Product;
import com.codesoom.assignment.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository toyProductRepository;

    public ProductServiceImpl(ProductRepository toyProductRepository) {

        this.toyProductRepository = toyProductRepository;

    }

    @Override
    public Product register(Product product) {

       return toyProductRepository.save(product);

    }

    @Override
    public Product getProduct(Long id) {

        return toyProductRepository.findById(id).orElseThrow();

    }

    @Override
    public List<Product> getProducts() {

        return toyProductRepository.findAll();

    }

    @Override
    public Product updateProduct(Long id, Product product) {

        Product updateProduct = getProduct(id);
        updateProduct.setProduct(product);

        return updateProduct;

    }

    @Override
    public void delete(Long id) {

        toyProductRepository.findById(id).ifPresent(it -> {
            toyProductRepository.deleteById(it.getId());
        });


    }

}
