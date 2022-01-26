package com.codesoom.assignment.controller;

import com.codesoom.assignment.domain.Product;
import com.codesoom.assignment.exception.ProductNotFoundException;
import com.codesoom.assignment.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController 클래스")
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    @Autowired
    ObjectMapper objectMapper;

    List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
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
    @DisplayName("GET /products 요청은")
    class Describe_list {

        @Nested
        @DisplayName("등록된 Product들이 존재하면")
        class Context_has_product {

            int givenProductsCount;

            @BeforeEach
            void prepare() {
                givenProductsCount = products.size();
                given(productService.getProducts()).willReturn(products);
            }

            @Test
            @DisplayName("200(Ok)와 Product의 전체 리스트를 응답합니다.")
            void it_return_ok_and_product() throws Exception {
                mockMvc.perform(get("/products"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(givenProductsCount)))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("등록된 Product들이 없다면")
        class Context_has_not_product {
            List<Product> emptyProducts = new ArrayList<>();
            int givenProductsCount;

            @BeforeEach
            void prepare() {
                givenProductsCount = emptyProducts.size();
                given(productService.getProducts()).willReturn(emptyProducts);
            }

            @Test
            @DisplayName("200(Ok)와 빈 리스트를 응답합니다.")
            void it_return_ok_and_products() throws Exception {
                mockMvc.perform(get("/products"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(givenProductsCount)))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("GET /products/{id} 요청은")
    class Describe_detail {

        @Nested
        @DisplayName("등록된 Product의 id가 주어진다면")
        class Context_with_id {

            Long givenId = 1L;

            @BeforeEach
            void prepare() {
                given(productService.getProduct(givenId)).willReturn(products.get(0));
            }

            @Test
            @DisplayName("200(Ok)와 Product의 정보를 응답합니다.")
            void it_return_ok_and_product() throws Exception {
                mockMvc.perform(get("/products/" + givenId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isNotEmpty())
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("등록되지 않은 Product의 id가 주어진다면")
        class Context_with_invaild_id {

            Long givenInvalidId = 9999L;

            @BeforeEach
            void prepare() {
                given(productService.getProduct(givenInvalidId)).willThrow(new ProductNotFoundException(givenInvalidId));
            }

            @Test
            @DisplayName("404(Not found)를 응답합니다.")
            void it_return_not_fount() throws Exception {
                mockMvc.perform(get("/products/" + givenInvalidId))
                        .andExpect(status().isNotFound())
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("POST /products 요청은")
    class Describe_create {

        @Nested
        @DisplayName("Product가 주어진다면")
        class Context_with_product {

            Product givenProduct;

            @BeforeEach
            void prepare() {
                givenProduct = products.get(0);
                given(productService.createProduct(any(Product.class))).willReturn(givenProduct);
            }

            @Test
            @DisplayName("201(Created)와 Product를 응답합니다.")
            void it_create_product_return_created_and_product() throws Exception {
                mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productToContent(givenProduct)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value(givenProduct.getName()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("Product가 없다면")
        class Context_without_product {

            @Test
            @DisplayName("400(Bad Request)를 응답합니다.")
            void it_return_bad_request() throws Exception {
                mockMvc.perform(post("/products")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("PUT/PATCH /products 요청은")
    class Describe_update {

        @Nested
        @DisplayName("등록된 Product의 Id와 수정할 Product가 주어진다면")
        class Context_with_id_and_product {
            Long givenId = 1L;
            Product givenProduct;

            @BeforeEach
            void prepare() {
                givenProduct = products.get(0);
                givenProduct.setName("업데이트 제품");

                given(productService.updateProduct(eq(givenId), any(Product.class)))
                        .will(invocation -> {
                            Long id = invocation.getArgument(0);
                            Product product = invocation.getArgument(1);

                            product.setId(id);
                            return product;
                        });
            }

            @Test
            @DisplayName("PUT요청 / 200(Ok)과 Product를 응답합니다.")
            void it_put_update_product_return_ok_and_product() throws Exception {
                mockMvc.perform(put("/products/" + givenId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productToContent(givenProduct)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(givenId))
                        .andExpect(jsonPath("$.name").value(givenProduct.getName()))
                        .andDo(print());
            }

            @Test
            @DisplayName("PATCH요청 / 200(Ok)과 Product를 응답합니다.")
            void it_patch_update_product_return_ok_and_product() throws Exception {
                mockMvc.perform(patch("/products/" + givenId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productToContent(givenProduct)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(givenId))
                        .andExpect(jsonPath("$.name").value(givenProduct.getName()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("등록된 Product의 Id만 주어진다면")
        class Context_with_id {

            Long givenId = 1L;

            @Test
            @DisplayName("400(Bad Request)를 응답합니다.")
            void it_return_badRequest() throws Exception {
                mockMvc.perform(put("/products/" + givenId)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("수정할 Product만 주어진다면")
        class Context_with_product {

            Product givenProduct;

            @BeforeEach
            void prepare() {
                givenProduct = products.get(0);
            }

            @Test
            @DisplayName("405(Method not allowed)를 응답합니다.")
            void it_return_badRequest() throws Exception {
                mockMvc.perform(put("/products/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productToContent(givenProduct)))
                        .andExpect(status().isMethodNotAllowed())
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /products 요청은")
    class Describe_delete {

        @Nested
        @DisplayName("등록된 Product가 주어진다면")
        class Context_with_product {

            Long givenId = 1L;

            @Test
            @DisplayName("204(No Content)과 빈값을 응답합니다.")
            void it_delete_task_return_noContent() throws Exception {
                mockMvc.perform(delete("/products/" + givenId))
                        .andExpect(status().isNoContent())
                        .andDo(print());
            }
        }
    }

    private String productToContent(Product product) throws JsonProcessingException {
        return objectMapper.writeValueAsString(product);
    }
}
