package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;

public record CheckoutRequest(
    @NotBlank(message = "Vui lòng nhập tên người nhận")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự") String receiverName,
    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự") String receiverPhone,
    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự") String shippingAddress,
    @NotNull(message = "Vui lòng chọn phương thức thanh toán") PaymentMethod paymentMethod
) {}
