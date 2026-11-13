package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;

public record ChatProductResponse(Long id, String name, BigDecimal price,
                                  String imageUrl, boolean inStock, String url) {}
