package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Form gửi yêu cầu Custom (C-03). Họ tên, email, số điện thoại lấy từ tài khoản đăng nhập
 * nên không có trong body. Trạng thái luôn là PENDING, không nhận từ client.
 * Trường tùy chọn không nhập thì gửi null (không gửi chuỗi rỗng).
 */
public record CreateCustomRequest(

    @NotBlank(message = "Vui lòng chọn layout")
    @Pattern(regexp = "60%|65%|75%|TKL|Fullsize", message = "Layout không hợp lệ")
    String layout,

    @NotBlank(message = "Vui lòng nhập loại switch")
    @Size(max = 80, message = "Loại switch tối đa 80 ký tự")
    String switchType,

    @Pattern(regexp = "Nhựa|Nhôm|Gỗ|Acrylic", message = "Chất liệu case không hợp lệ")
    String caseMaterial,

    @Size(max = 120, message = "Bộ keycap tối đa 120 ký tự")
    String keycapSet,

    Boolean lubing,

    Boolean hotSwap,

    @Pattern(regexp = "Không|RGB|Đơn sắc", message = "Tùy chọn đèn LED không hợp lệ")
    String rgbOption,

    @DecimalMin(value = "500000", message = "Ngân sách tối thiểu 500.000đ")
    @Digits(integer = 12, fraction = 0, message = "Ngân sách phải là số nguyên đồng")
    BigDecimal budget,

    @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
    String note
) {}
