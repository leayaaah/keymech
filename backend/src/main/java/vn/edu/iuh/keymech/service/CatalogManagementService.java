package vn.edu.iuh.keymech.service;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CategoryResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.product.StaffProductInput;
import vn.edu.iuh.keymech.dto.product.StaffProductResponse;
import vn.edu.iuh.keymech.event.ProductChangedEvent;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;
import vn.edu.iuh.keymech.repository.CategoryRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;

import static vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE;
import static vn.edu.iuh.keymech.model.enums.VisibilityStatus.HIDDEN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogManagementService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final ProductImageStorage images;
    private final ApplicationEventPublisher events;

    public PageResponse<StaffProductResponse> listProducts(String keyword, VisibilityStatus status, int page, int size) {
        Specification<Product> filter = Specification.unrestricted();
        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.strip().toLowerCase(Locale.ROOT);
            filter = filter.and((product, query, cb) -> cb.or(
                    cb.gt(cb.locate(cb.lower(product.get("name")), term), 0),
                    cb.gt(cb.locate(cb.lower(product.get("brand")), term), 0)));
        }
        if (status != null) {
            filter = filter.and((product, query, cb) -> cb.equal(product.get("status"), status));
        }
        var pageable = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100), Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.of(products.findAll(filter, pageable).map(StaffProductResponse::from));
    }

    public StaffProductResponse getProduct(long id) {
        return products.findById(id).map(StaffProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
    }

    @Transactional
    public StaffProductResponse createProduct(StaffProductInput input) {
        Product product = new Product();
        apply(product, input);
        products.save(product);
        events.publishEvent(new ProductChangedEvent(this, product.getId(), "created"));
        return StaffProductResponse.from(product);
    }

    @Transactional
    public StaffProductResponse updateProduct(long id, StaffProductInput input) {
        Product product = lockProduct(id);
        apply(product, input);
        events.publishEvent(new ProductChangedEvent(this, id, "updated"));
        return StaffProductResponse.from(product);
    }

    @Transactional
    public StaffProductResponse changeProductVisibility(long id, VisibilityStatus status) {
        Product product = lockProduct(id);
        if (status == ACTIVE) requireActiveCategory(product.getCategory().getId());
        if (product.getStatus() != status) {
            product.setStatus(status);
            events.publishEvent(new ProductChangedEvent(this, id, status == ACTIVE ? "visible" : "hidden"));
        }
        return StaffProductResponse.from(product);
    }

    public List<CategoryResponse> listCategories(boolean activeOnly) {
        Sort sort = Sort.by("name", "id");
        var result = activeOnly ? categories.findByStatus(ACTIVE, sort) : categories.findAll(sort);
        return result.stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryResponse.Create input) {
        Category category = new Category();
        category.setName(input.name().strip());
        category.setSlug(input.slug());
        try {
            return CategoryResponse.from(categories.saveAndFlush(category));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("CATEGORY_DUPLICATE", "Tên hoặc slug danh mục đã tồn tại.");
        }
    }

    @Transactional
    public CategoryResponse changeCategoryVisibility(long id, VisibilityStatus status) {
        Category category = lockCategory(id);
        if (status == HIDDEN && products.existsByCategoryIdAndStatus(id, ACTIVE)) {
            throw new BusinessException("CATEGORY_NOT_EMPTY", "Danh mục vẫn còn sản phẩm đang hiển thị.");
        }
        category.setStatus(status);
        return CategoryResponse.from(category);
    }

    private void apply(Product product, StaffProductInput input) {
        product.setCategory(requireActiveCategory(input.categoryId()));
        product.setName(input.name().strip());
        product.setBrand(input.brand());
        product.setLayout(input.layout());
        product.setSwitchType(input.switchType());
        product.setPrice(input.price());
        product.setStockQuantity(input.stockQuantity());
        product.setDescription(input.description());
        product.setSpecs(input.specs());
        if (input.image() != null) {
            product.setImageUrl(images.store(input.image(), product.getImageUrl()));
        }
    }

    private Product lockProduct(long id) {
        return products.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
    }

    private Category lockCategory(long id) {
        return categories.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
    }

    private Category requireActiveCategory(long id) {
        Category category = lockCategory(id);
        if (category.getStatus() != ACTIVE) {
            throw new BusinessException("CATEGORY_HIDDEN", "Vui lòng chọn danh mục đang hiển thị.");
        }
        return category;
    }
}
