package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.PageResponse;
import com.tuktak.inventory.dto.ProductDto;
import com.tuktak.inventory.entity.Category;
import com.tuktak.inventory.entity.Product;
import com.tuktak.inventory.entity.Stock;
import com.tuktak.inventory.repository.CategoryRepository;
import com.tuktak.inventory.repository.ProductRepository;
import com.tuktak.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.existsBySku(productDto.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + productDto.getSku());
        }

        Product product = mapToEntity(productDto);
        Product savedProduct = productRepository.save(product);

        Stock stock = Stock.builder()
                .product(savedProduct)
                .quantityAvailable(0)
                .minStockLevel(10)
                .maxStockLevel(1000)
                .reorderPoint(20)
                .build();
        stockRepository.save(stock);
        savedProduct.setStock(stock);

        log.info("Product created: {} with SKU: {}", savedProduct.getName(), savedProduct.getSku());
        return mapToDto(savedProduct);
    }

    // Pagination methods
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findAll(pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getActiveProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByCategoryName(String categoryName, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findActiveByCategoryName(categoryName, pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getFeaturedProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findFeaturedProducts(pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        return toPageResponse(productPage);
    }

    // Non-paginated methods (for backward compatibility)
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getActiveProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getActiveProductsByCategoryName(String categoryName) {
        return productRepository.findActiveByCategoryName(categoryName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        if (productDto.getName() != null) {
            product.setName(productDto.getName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getPrice() != null) {
            product.setPrice(productDto.getPrice());
        }
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + productDto.getCategoryId()));
            product.setCategory(category);
        }
        product.setFeatured(productDto.isFeatured());
        if (productDto.getImageUrl() != null) {
            product.setImageUrl(productDto.getImageUrl());  // ✅ add this
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: {}", updatedProduct.getName());
        return mapToDto(updatedProduct);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deactivated: {}", product.getName());
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }

    private Product mapToEntity(ProductDto dto) {
        Product.ProductBuilder builder = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .price(dto.getPrice())
                .active(true)
                .featured(dto.isFeatured())
                .imageUrl(dto.getImageUrl());  // ✅ add this

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));
            builder.category(category);
        }

        return builder.build();
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .active(product.isActive())
                .featured(product.isFeatured())
                .imageUrl(product.getImageUrl())  // ✅ add this
                .build();

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getStock() != null) {
            dto.setStockQuantity(product.getStock().getQuantityAvailable());
        }

        return dto;
    }

    private PageResponse<ProductDto> toPageResponse(Page<Product> page) {
        List<ProductDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
