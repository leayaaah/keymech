package vn.edu.iuh.keymech.dto;

import java.util.List;

public record ChatResponse(String answer, List<ChatProductResponse> products,
                           List<String> suggestions, String mode) {}
