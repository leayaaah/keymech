package vn.edu.iuh.keymech.controller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.StaffOrderResponse;
import vn.edu.iuh.keymech.dto.StaffOrderUpdateRequest;
import vn.edu.iuh.keymech.dto.UpdateOrderItemQuantityRequest;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentStatus;
import vn.edu.iuh.keymech.service.StaffOrderService;

@RestController
@RequestMapping("/api/staff/orders")
public class StaffOrderController {

    private final StaffOrderService orders;

    public StaffOrderController(StaffOrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public PageResponse<StaffOrderResponse.Summary> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "uuuu-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "uuuu-MM-dd") LocalDate toDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orders.list(keyword, fromDate, toDate, status, page, size);
    }

    @GetMapping("/{id}")
    public StaffOrderResponse detail(@PathVariable long id) {
        validateId(id, "Mã đơn hàng");
        return orders.detail(id);
    }

    @PutMapping("/{id}/items/{itemId}")
    public StaffOrderResponse updateQuantity(@PathVariable long id, @PathVariable long itemId,
                                             @RequestBody UpdateOrderItemQuantityRequest request) {
        validateId(id, "Mã đơn hàng");
        validateId(itemId, "Mã dòng hàng");
        var quantity = request.quantity();
        if (quantity == null || !quantity.isIntegralNumber() || !quantity.canConvertToInt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Số lượng phải là số nguyên dương");
        }
        return orders.updateQuantity(id, itemId, quantity.intValue());
    }

    @PutMapping("/{id}/status")
    public StaffOrderResponse updateStatus(@PathVariable long id, @RequestBody StaffOrderUpdateRequest.Status request) {
        validateId(id, "Mã đơn hàng");
        return orders.updateStatus(id, parseStatus(request.status(), OrderStatus.class, "Trạng thái đơn hàng"));
    }

    @PutMapping("/{id}/payment")
    public StaffOrderResponse updatePayment(@PathVariable long id, @RequestBody StaffOrderUpdateRequest.Payment request) {
        validateId(id, "Mã đơn hàng");
        PaymentStatus next = parseStatus(request.paymentStatus(), PaymentStatus.class, "Trạng thái thanh toán");
        return orders.updatePayment(id, next);
    }

    private static <T extends Enum<T>> T parseStatus(JsonNode value, Class<T> type, String label) {
        if (value != null && value.isTextual()) {
            for (T status : type.getEnumConstants()) {
                if (status.name().equals(value.asText())) {
                    return status;
                }
            }
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", label + " không hợp lệ");
    }

    private static void validateId(long id, String label) {
        if (id < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                label + " phải là số nguyên dương");
        }
    }
}
