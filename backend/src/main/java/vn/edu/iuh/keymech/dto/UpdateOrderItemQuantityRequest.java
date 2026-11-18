package vn.edu.iuh.keymech.dto;

import tools.jackson.databind.JsonNode;

public record UpdateOrderItemQuantityRequest(JsonNode quantity) {}
