package vn.edu.iuh.keymech.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Phản hồi phân trang thống nhất theo hợp đồng API v1:
 * content, page, size, totalElements, totalPages.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
