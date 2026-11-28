package vn.edu.iuh.keymech.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;
import vn.edu.iuh.keymech.repository.CategoryRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PostUiApiIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired EntityManager entityManager;

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
        mvc.perform(get("/api/custom-requests/components")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/custom-requests/components").with(staff)).andExpect(status().isForbidden());
        mvc.perform(get("/api/custom-requests/components").with(admin)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-SPA-01: mở trực tiếp các trang giao diện và giữ quyền API")
    void opensFrontendRoutesWithoutExposingProtectedApis() throws Exception {
        for (String path : new String[]{"/login", "/products/23", "/customer/orders/1/success",
                "/staff", "/staff/products/1/edit", "/admin/categories"}) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
        mvc.perform(get("/api/staff/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/categories").with(staff)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-STOCK-01: lưu thông tin sản phẩm giữ tồn kho đã cập nhật")
    void editingProductPreservesStockUpdatedSeparately() throws Exception {
        var category = mvc.perform(post("/api/admin/categories").with(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Danh mục thử tồn kho","slug":"stock-test"}
                    """))
            .andExpect(status().isCreated()).andReturn();
        Number categoryId = JsonPath.read(category.getResponse().getContentAsString(), "$.id");
        var product = mvc.perform(multipart(HttpMethod.POST, "/api/staff/products").with(staff)
                .param("name", "Sản phẩm thử tồn kho").param("categoryId", categoryId.toString())
                .param("price", "1200000").param("stockQuantity", "5"))
            .andExpect(status().isCreated()).andReturn();
        Number productId = JsonPath.read(product.getResponse().getContentAsString(), "$.id");
        String path = "/api/staff/products/" + productId;

        mvc.perform(patch(path + "/stock").with(customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"stockQuantity\":4}")).andExpect(status().isForbidden());
        mvc.perform(patch(path + "/stock").with(staff).contentType(MediaType.APPLICATION_JSON)
                .content("{\"stockQuantity\":4}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.stockQuantity").value(4));
        mvc.perform(multipart(HttpMethod.PUT, path).with(staff)
                .param("name", "Đổi tên sản phẩm").param("categoryId", categoryId.toString())
                .param("price", "1300000").param("stockQuantity", "5"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.stockQuantity").value(4));
    }

    @Test
    @DisplayName("TC-CUS-09, TC-CUS-13: bảo vệ yêu cầu và từ chối báo giá cũ")
    void customerCannotReadAnotherRequestOrAcceptAStaleQuote() throws Exception {
        BuildFixture parts = buildFixture(true);
        var created = mvc.perform(post("/api/custom-requests").with(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parts.payload(null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.components.length()").value(3))
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
    @DisplayName("C-03: lọc linh kiện theo kit và giữ bản chụp cấu hình khi gửi")
    void customUsesCatalogPartsAndPreservesSnapshot() throws Exception {
        BuildFixture parts = buildFixture(false);
        mvc.perform(get("/api/custom-requests/components").with(customer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.kit.productId == %d)].switches[0].quantity".formatted(parts.kit().getId())).value(org.hamcrest.Matchers.hasItem(3)));

        var result = mvc.perform(post("/api/custom-requests").with(customer)
                .contentType(MediaType.APPLICATION_JSON).content(parts.payload(parts.stabilizer().getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.layout").value("75%"))
            .andExpect(jsonPath("$.components.length()").value(4))
            .andExpect(jsonPath("$.components[1].slot").value("SWITCH"))
            .andExpect(jsonPath("$.components[1].quantity").value(3))
            .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        String originalSwitch = parts.switches().getName();
        parts.switches().setName("Tên sản phẩm đã cập nhật " + UUID.randomUUID());
        parts.switches().setPrice(BigDecimal.valueOf(300_000));
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/api/custom-requests/my/" + id).with(customer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.components[1].name").value(originalSwitch))
            .andExpect(jsonPath("$.components[1].unitPrice").value(205_000));
    }

    @Test
    @DisplayName("C-03: từ chối cấu hình nhập tay, thiếu stabilizer, sai thông số, ẩn hoặc thiếu hàng")
    void customRejectsInvalidConfigurations() throws Exception {
        BuildFixture parts = buildFixture(false);
        submitCustom("{\"layout\":\"75%\",\"switchName\":\"Tự nhập\"}", 400, "VALIDATION_ERROR");
        submitCustom(parts.payload(null), 400, "VALIDATION_ERROR");

        String validSwitch = parts.switches().getSpecs();
        parts.switches().setSpecs(validSwitch.replace("\"pinCount\":5", "\"pinCount\":6"));
        entityManager.flush();
        submitCustom(parts.payload(parts.stabilizer().getId()), 400, "CUSTOM_COMPONENT_INCOMPATIBLE");
        parts.switches().setSpecs(validSwitch);

        String validKeycap = parts.keycaps().getSpecs();
        parts.keycaps().setSpecs(validKeycap.replace("ANSI", "ISO"));
        entityManager.flush();
        submitCustom(parts.payload(parts.stabilizer().getId()), 400, "CUSTOM_COMPONENT_INCOMPATIBLE");
        parts.keycaps().setSpecs(validKeycap);
        parts.keycaps().setLayout("65%");
        entityManager.flush();
        submitCustom(parts.payload(parts.stabilizer().getId()), 400, "CUSTOM_COMPONENT_INCOMPATIBLE");
        parts.keycaps().setLayout("75%");

        parts.switches().setStockQuantity(2);
        entityManager.flush();
        submitCustom(parts.payload(parts.stabilizer().getId()), 409, "OUT_OF_STOCK");
        parts.switches().setStockQuantity(10);
        parts.kit().setStatus(VisibilityStatus.HIDDEN);
        entityManager.flush();
        submitCustom(parts.payload(parts.stabilizer().getId()), 409, "CUSTOM_COMPONENT_UNAVAILABLE");
    }

    private void submitCustom(String payload, int expectedStatus, String errorCode) throws Exception {
        mvc.perform(post("/api/custom-requests").with(customer)
                .contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().is(expectedStatus)).andExpect(jsonPath("$.error").value(errorCode));
    }

    private BuildFixture buildFixture(boolean bundledStabilizer) {
        String keycapFit = "\"stem\":\"MX\",\"physicalLayout\":\"ANSI\",\"spacebarWidth\":\"6.25u\",\"rightShiftWidth\":\"1.75u\"";
        Product kit = part("kit-ban-phim", "Kit", null, """
            {"keys":84,"includes":"Case, Plate, PCB%s","hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],
             "platform":"Test75","stabilizerMount":"plate-mount","stabilizerSizes":["6.25u","2u"],%s}
            """.formatted(bundledStabilizer ? ", Stabilizer" : "", keycapFit));
        Product switches = part("switch", "Switch", "Linear", """
            {"pack":35,"pinCount":5,"switchFamily":"MX","stem":"MX"}
            """);
        Product keycaps = part("keycap", "Keycap", null, "{\"componentType\":\"keycap-set\"," + keycapFit + "}");
        Product stabilizer = bundledStabilizer ? null : part("dung-cu-phu-kien", "Stabilizer", null, """
            {"componentType":"stabilizer","platform":"Test75","mount":"plate-mount","sizes":["2u","6.25u"]}
            """);
        return new BuildFixture(kit, switches, keycaps, stabilizer);
    }

    private Product part(String slug, String name, String switchType, String specs) {
        Category category = categories.findBySlug(slug).orElseGet(() -> {
            Category created = new Category();
            created.setSlug(slug);
            created.setName("Danh mục test " + slug);
            return categories.saveAndFlush(created);
        });
        Product product = new Product();
        product.setCategory(category);
        product.setName(name + " test " + UUID.randomUUID());
        product.setLayout("75%");
        product.setPrice(BigDecimal.valueOf(205_000));
        product.setStockQuantity(10);
        product.setSwitchType(switchType);
        product.setSpecs(specs);
        return products.saveAndFlush(product);
    }

    private record BuildFixture(Product kit, Product switches, Product keycaps, Product stabilizer) {
        String payload(Long stabilizerId) {
            return """
                {"kitProductId":%d,"switchProductId":%d,"keycapProductId":%d,"stabilizerProductId":%s,"budget":2000000}
                """.formatted(kit.getId(), switches.getId(), keycaps.getId(), stabilizerId);
        }
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
