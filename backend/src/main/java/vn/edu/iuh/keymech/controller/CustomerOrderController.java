package vn.edu.iuh.keymech.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.keymech.dto.CustomerOrderResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CustomerOrderService;

@RestController
@RequestMapping("/api/orders/my")
public class CustomerOrderController {

    private final CustomerOrderService orders;

    public CustomerOrderController(CustomerOrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public PageResponse<CustomerOrderResponse.Summary> list(@AuthenticationPrincipal AppUserDetails customer,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return orders.list(customer.getId(), page, size);
    }

    @GetMapping("/{id}")
    public CustomerOrderResponse detail(@AuthenticationPrincipal AppUserDetails customer, @PathVariable long id) {
        return orders.detail(customer.getId(), id);
    }
}
