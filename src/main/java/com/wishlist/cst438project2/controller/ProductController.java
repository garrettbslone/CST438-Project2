package com.wishlist.cst438project2.controller;

import com.wishlist.cst438project2.document.Product;
import com.wishlist.cst438project2.exception.BadRequestException;
import com.wishlist.cst438project2.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/v1/product")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/save")
    public String saveProduct(@RequestBody Product product) {

        log.info("ProductController: Starting saveProduct");

        if(Objects.isNull(product))
            throw new BadRequestException();

        String responseTimestamp = productService.saveProduct(product);

        log.info("ProductController: Exiting saveProduct");

        return responseTimestamp;
    }
}
