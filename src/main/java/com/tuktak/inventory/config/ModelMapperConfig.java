package com.tuktak.inventory.config;

import com.tuktak.inventory.dto.*;
import com.tuktak.inventory.entity.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        /* =========================
           Category → CategoryDto
        ========================== */

        Converter<Category, Integer> productCountConverter = ctx -> {
            Category category = ctx.getSource();
            if (category == null || category.getProducts() == null) {
                return 0;
            }
            return category.getProducts().size();
        };

        modelMapper.createTypeMap(Category.class, CategoryDto.class)
                .addMappings(mapper ->
                        mapper.using(productCountConverter)
                                .map(src -> src, CategoryDto::setProductCount)
                );


        /* =========================
           Product → ProductDto
        ========================== */

        modelMapper.createTypeMap(Product.class, ProductDto.class)
                .addMappings(mapper -> {

                    mapper.map(
                            src -> src.getCategory() != null ? src.getCategory().getId() : null,
                            ProductDto::setCategoryId
                    );

                    mapper.map(
                            src -> src.getCategory() != null ? src.getCategory().getName() : null,
                            ProductDto::setCategoryName
                    );

                    mapper.map(
                            src -> src.getStock() != null ? src.getStock().getQuantityAvailable() : 0,
                            ProductDto::setStockQuantity
                    );
                });


        /* =========================
           Stock → StockDto
        ========================== */

        modelMapper.createTypeMap(Stock.class, StockDto.class)
                .addMappings(mapper -> {

                    mapper.map(
                            src -> src.getProduct() != null ? src.getProduct().getId() : null,
                            StockDto::setProductId
                    );

                    mapper.map(
                            src -> src.getProduct() != null ? src.getProduct().getName() : null,
                            StockDto::setProductName
                    );

                    mapper.map(
                            src -> src.getProduct() != null ? src.getProduct().getSku() : null,
                            StockDto::setProductSku
                    );
                });


        /* =========================
           AdminUser → AdminDto
        ========================== */

        modelMapper.createTypeMap(AdminUser.class, AdminDto.class)
                .addMappings(mapper -> {
                    mapper.map(
                            src -> src.getRole() != null ? src.getRole().name() : null,
                            AdminDto::setRole
                    );
                    mapper.skip(AdminDto::setPassword);
                });

        return modelMapper;
    }
}