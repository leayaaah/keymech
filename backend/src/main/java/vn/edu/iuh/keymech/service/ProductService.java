package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.keymech.dto.product.ProductsDetailResponse;
import vn.edu.iuh.keymech.dto.product.ProductsFilterRequest;
import vn.edu.iuh.keymech.dto.product.ProductsResponse;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;
import vn.edu.iuh.keymech.repository.ProductRepository;

/**
 * G-01: Xem danh sách sản phẩm (lọc, phân trang, chỉ ACTIVE).
 * G-02: Xem chi tiết sản phẩm (ẩn trả 404).
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * G-01: Danh sách sản phẩm đang hiện, có lọc và phân trang.
     * page = 0-based; size = số sản phẩm mỗi trang (12 ở frontend).
     * Trang rỗng trả về content rỗng (không ném lỗi).
     * page/size null → dùng 0 và 12 (frontend default).
     */
    @Transactional(readOnly = true)
    public Page<ProductsResponse> list(ProductsFilterRequest f) {
        int page = f.page() != null ? Math.max(f.page(), 0) : 0;
        int size = f.size() != null ? Math.max(f.size(), 12) : 12;
        PageRequest pageRequest = PageRequest.of(page, size, sort(f.sort()));

        Specification<Product> spec = ProductRepository.withFilters(
                f.categoryId(), f.brand(), f.layout(),
                f.minPrice(), f.maxPrice(), f.keyword());

        Page<Product> pageProduct = productRepository.findAll(spec, pageRequest);
        return pageProduct.map(this::toResponse);
    }

    /**
     * G-02: Chi tiết sản phẩm. Sản phẩm đã ẩn hoặc không tồn tại → 404.
     */
    @Transactional(readOnly = true)
    public ProductsDetailResponse detail(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        if (p.getStatus() != VisibilityStatus.ACTIVE) {
            throw new ResourceNotFoundException("Sản phẩm không còn hiển thị");
        }
        return toDetailResponse(p);
    }

    private Sort sort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        return switch (sort.toLowerCase()) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name-asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name-desc" -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };
    }

    private ProductsResponse toResponse(Product p) {
        return new ProductsResponse(
                p.getId(),
                p.getName(),
                p.getBrand(),
                p.getPrice(),
                p.getLayout(),
                p.getSwitchType(),
                p.getStockQuantity(),
                p.getImageUrl(),
                shortDescription(p.getDescription()));
    }

    private ProductsDetailResponse toDetailResponse(Product p) {
        return new ProductsDetailResponse(
                p.getId(),
                p.getName(),
                p.getBrand(),
                p.getPrice(),
                p.getLayout(),
                p.getSwitchType(),
                p.getStockQuantity(),
                p.getImageUrl(),
                p.getDescription(),
                p.getSpecs());
    }

    private String shortDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        return description.length() <= 120 ? description : description.substring(0, 120) + "...";
    }

    /**
     * Truy vấn sản phẩm theo id (dùng nội bộ — Cart, Order, ActivityTracker).
     * Không lọc ACTIVE: caller phải kiểm tra trạng thái.
     */
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    /**
     * Lấy các product theo danh sách id (dùng cho Cart, Chatbot).
     */
    public List<Product> findByIdIn(List<Long> ids) {
        return productRepository.findAllById(ids);
    }

    public List<Product> findActiveByIdIn(List<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .filter(p -> p.getStatus() == VisibilityStatus.ACTIVE)
                .collect(Collectors.toList());
    }
}