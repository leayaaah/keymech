package vn.edu.iuh.keymech.dto;

import tools.jackson.databind.JsonNode;

public final class StaffOrderUpdateRequest {

    private StaffOrderUpdateRequest() {}

    public record Status(JsonNode status) {}

    public record Payment(JsonNode paymentStatus) {}
}
