package com.codesoom.assignment.service;

import com.codesoom.assignment.domain.Product;
import com.codesoom.assignment.exception.ProductNotFoundException;
import com.codesoom.assignment.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("ProductsService 클래스")
public class ProductServiceTest {

    private ProductService productService;

    private ProductRepository productRepository;

    List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository, new ModelMapper());

        Product product = Product.builder()
                .name("테스트 제품")
                .maker("테스트 메이커")
                .price(1000)
                .image("http://test.com/test.jpg")
                .build();

        IntStream.range(0, 5).forEach(i -> {
            product.setId(Long.valueOf(i));
            products.add(product);
        });
    }

    @Nested
    @DisplayName("getProducts 메소드는")
    class Describe_getProducts {

        @Nested
        @DisplayName("등록된 Product가 있다면")
        class Context_has_product {

            int givenProductsCount;

            @BeforeEach
            void prepare() {
                givenProductsCount = products.size();
                given(productRepository.findAll()).willReturn(products);
            }

            @Test
            @DisplayName("Product의 전체 리스트를 리턴한다.")
            void it_return_products() {
                assertThat(productService.getProducts()).hasSize(givenProductsCount);
            }
        }

        @Nested
        @DisplayName("등록된 Product가 없다면")
        class Context_has_not_product {

            @BeforeEach
            void prepare() {
                List<Product> products = new ArrayList<>();

                given(productRepository.findAll()).willReturn(products);
            }

            @Test
            @DisplayName("빈 리스트를 리턴한다.")
            void it_return_products() {
                assertThat(productService.getProducts()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getProduct 메소드는")
    class Describe_getProduct {

        @Nested
        @DisplayName("등록된 Product의 id 값이 주어진다면")
        class Context_with_id {

            Long givenId = 1L;

            @BeforeEach
            void prepare() {
                given(productRepository.findById(givenId)).willReturn(Optional.of(products.get(0)));
            }

            @Test
            @DisplayName("등록된 product 정보를 리턴한다.")
            void it_return_product() {
                Product foundProduct = productService.getProduct(givenId);

                assertThat(foundProduct).isNotNull();
            }
        }

        @Nested
        @DisplayName("등록되지 않은 Product의 id 값이 주어진다면")
        class Context_with_invalid_id {

            private Long givenInvalidId = 100L;

            @BeforeEach
            void prepare() {
                given(productRepository.findById(givenInvalidId)).willReturn(Optional.empty());
            }

            @Test
            @DisplayName("Product를 찾을 수 없다는 내용의 예외를 던진다.")
            void it_return_productNotFoundException() {
                assertThatThrownBy(() -> productService.getProduct(givenInvalidId)).isInstanceOf(ProductNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("createProduct 메소드는")
    class Describe_createProduct {

        @Nested
        @DisplayName("등록할 Product가 주어진다면")
        class Context_with_product {

            Product givenProduct;
            Long givenId = 1L;

            @BeforeEach
            void prepare() {
                givenProduct = products.get(0);
                given(productRepository.save(any(Product.class))).will(invocation -> {
                    Product product = invocation.getArgument(0);
                    product.setId(givenId);
                    return product;
                });
            }

            @Test
            @DisplayName("Product를 생성하고, 리턴한다.")
            void it_create_product_return_product() {
                Product createdProduct = productService.createProduct(givenProduct);

                verify(productRepository).save(any(Product.class));

                assertThat(createdProduct.getId()).isEqualTo(givenId);
                assertThat(createdProduct.getName()).isEqualTo(givenProduct.getName());
            }
        }

        @Nested
        @DisplayName("null 주어진다면")
        class Context_with_null {

            Product givenNullProduct = null;

            @BeforeEach
            void prepare() {
                given(productRepository.save(givenNullProduct)).willThrow(RuntimeException.class);
            }

            @Test
            @DisplayName("RuntimeException을 던진다.")
            void it_return_runtimeException() {
                assertThatThrownBy(() -> productService.createProduct(givenNullProduct)).isInstanceOf(RuntimeException.class);
            }
        }
    }

    @Nested
    @DisplayName("updateProduct 메소드는")
    class Describe_updateProduct {

        @Nested
        @DisplayName("등록된 Product의 id와 수정할 Product가 주어진다면")
        class Context_with_id_and_product {

            Long givenId = 1L;
            Product givenProduct;

            @BeforeEach
            void prepare() {
                givenProduct = products.get(0);
                given(productRepository.findById(givenId)).willReturn(Optional.of(givenProduct));
                given(productRepository.save(any(Product.class))).will(args -> {
                    Product product = args.getArgument(0);
                    product.setId(givenId);
                    return product;
                });
            }

            @Test
            @DisplayName("해당 id의 Product를 수정하고, 리턴한다.")
            void it_update_product_return_product() {
                Product updatedProduct = productService.updateProduct(givenId, givenProduct);

                verify(productRepository).findById(givenId);

                assertThat(updatedProduct.getName()).isEqualTo(givenProduct.getName());
            }
        }
        @Nested
        @DisplayName("등록되지 않은 Product의 id 와 Product가 있다면 ")
        class Context_with_invalid_id_and_product {

            Long givenInvalidId = 9999L;

            @BeforeEach
            void prepare() {
                given(productRepository.findById(givenInvalidId)).willReturn(Optional.empty());
            }

            @Test
            @DisplayName("Product를 찾을 수 없다는 내용의 예외를 던진다.")
            void it_return_productNotFoundException() {
                assertThatThrownBy(() -> productService.updateProduct(givenInvalidId, products.get(0))).isInstanceOf(ProductNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("deleteProduct 메소드는")
    class Describe_deleteProduct {

        @Nested
        @DisplayName("등록된 Product의 id가 주어진다면")
        class Context_with_id {

            Long givenProductId = 1L;

            @BeforeEach
            void prepare() {
                given(productRepository.findById(givenProductId)).willReturn(Optional.of(products.get(0)));
            }

            @Test
            @DisplayName("등록된 Product를 삭제하고, 빈값이 리턴한다.")
            void it_delete_product_return() {
                productService.deleteProduct(givenProductId);

                verify(productRepository).findById(givenProductId);
                verify(productRepository).delete(any(Product.class));
            }
        }

        @Nested
        @DisplayName("등록되지 않은 Product의 id가 주어진다면")
        class Context_with_invalid_id {

            Long givenProductInvalidId = 100L;

            @Test
            @DisplayName("Product를 찾을 수 없다는 내용의 예외를 던진다.")
            void it_return_productNotFoundException() {
                assertThatThrownBy(() -> productService.deleteProduct(givenProductInvalidId)).isInstanceOf(ProductNotFoundException.class);

                verify(productRepository).findById(givenProductInvalidId);
            }
        }
    }
}
