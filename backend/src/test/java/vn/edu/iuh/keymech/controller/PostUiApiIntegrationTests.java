package vn.edu.iuh.keymech.controller;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PostUiApiIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;

    private MockMvc mvc;
    private RequestPostProcessor customer;
    private RequestPostProcessor staff;
    private RequestPostProcessor admin;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        customer = account(Role.CUSTOMER);
        staff = account(Role.STAFF);
        admin = account(Role.ADMIN);
    }

    @Test
    @DisplayName("TC-SEC-10: phân quyền bốn vai trò cho các API sau PR UI")
    void roleMatrixProtectsNewApiGroups() throws Exception {
        String[] paths = {"/api/custom-requests/my", "/api/staff/custom-requests", "/api/staff/products",
                "/api/admin/categories", "/api/admin/dashboard"};
        RequestPostProcessor[] actors = {anonymous(), customer, staff, admin};
        int[][] expected = {
                {401, 401, 401, 401, 401},
                {200, 403, 403, 403, 403},
                {403, 200, 200, 403, 403},
                {403, 200, 200, 200, 200}
        };
        for (int actor = 0; actor < actors.length; actor++) {
            for (int path = 0; path < paths.length; path++) {
                mvc.perform(get(paths[path]).with(actors[actor])).andExpect(status().is(expected[actor][path]));
            }
        }
    }

    @Test
    @DisplayName("TC-CUS-09, TC-CUS-13: bảo vệ yêu cầu và từ chối báo giá cũ")
    void customerCannotReadAnotherRequestOrAcceptAStaleQuote() throws Exception {
        var created = mvc.perform(post("/api/custom-requests").with(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"layout":"Fullsize","switchType":"Linear","switchName":"Milky Yellow","budget":1000000}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        String mine = "/api/custom-requests/my/" + id;
        String assigned = "/api/staff/custom-requests/" + id;

        mvc.perform(get(mine).with(account(Role.CUSTOMER))).andExpect(status().isNotFound());
        mvc.perform(put(assigned + "/quote").with(staff).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"quotedPrice":1250000,"quoteNote":"Gồm công lắp ráp"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get(mine).with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotedPrice").doesNotExist());

        mvc.perform(put(assigned + "/status").with(staff).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"status":"QUOTED"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history[0].toStatus").value("QUOTED"));
        mvc.perform(put(assigned + "/quote").with(staff).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"quotedPrice":1300000}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUOTED"));
        mvc.perform(put(mine + "/accept").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"quotedPrice":1250000}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("QUOTE_CHANGED"));
        mvc.perform(put(mine + "/accept").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"quotedPrice":1300000}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.history[0].toStatus").value("ACCEPTED"));
    }

    @Test
    @DisplayName("TC-HID-04: chặn ẩn danh mục còn sản phẩm đang hiển thị")
    void categoryCannotBeHiddenUntilItsActiveProductIsHiddenByAdmin() throws Exception {
        String slug = "test-" + UUID.randomUUID();
        var category = mvc.perform(post("/api/admin/categories").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"%s","slug":"%s"}
                            """.formatted(slug, slug)))
                .andExpect(status().isCreated()).andReturn();
        Number categoryId = JsonPath.read(category.getResponse().getContentAsString(), "$.id");
        var product = mvc.perform(multipart(HttpMethod.POST, "/api/staff/products").with(staff)
                        .param("name", "Bàn phím thử").param("categoryId", categoryId.toString())
                        .param("price", "1200000").param("stockQuantity", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        Number productId = JsonPath.read(product.getResponse().getContentAsString(), "$.id");
        String categoryVisibility = "/api/admin/categories/" + categoryId + "/visibility";
        String productVisibility = "/api/admin/products/" + productId + "/visibility";
        String hide = "{\"status\":\"HIDDEN\"}";

        mvc.perform(put(productVisibility).with(staff).contentType(MediaType.APPLICATION_JSON).content(hide))
                .andExpect(status().isForbidden());
        mvc.perform(put(categoryVisibility).with(admin).contentType(MediaType.APPLICATION_JSON).content(hide))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_EMPTY"));
        mvc.perform(put(productVisibility).with(admin).contentType(MediaType.APPLICATION_JSON).content(hide))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));
        mvc.perform(put(categoryVisibility).with(admin).contentType(MediaType.APPLICATION_JSON).content(hide))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));
        mvc.perform(get("/api/products/{id}", productId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/staff/products/{id}", productId).with(staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));
    }

    private RequestPostProcessor account(Role role) {
        User account = new User();
        account.setEmail("api-check-" + UUID.randomUUID() + "@example.test");
        account.setFullName("Tài khoản kiểm thử");
        account.setPassword("test-only");
        account.setRole(role);
        return user(new AppUserDetails(users.save(account)));
    }
}
