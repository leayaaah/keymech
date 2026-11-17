package vn.edu.iuh.keymech.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;

@SpringBootTest
@Transactional
class AdminAccessIntegrationTests {

    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void staffCannotCreateUsers() throws Exception {
        mvc.perform(post("/api/admin/users").with(user("staff").roles("STAFF"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fullName":"Nhân viên thử", "email":"staff-test@example.test", "password":"test-only"}
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void adminCannotEditOrHideAnAdminAccount() throws Exception {
        User target = new User();
        target.setEmail("protected-" + UUID.randomUUID() + "@example.test");
        target.setFullName("Admin thử");
        target.setPassword("test-only");
        target.setRole(Role.ADMIN);
        Long id = users.save(target).getId();

        mvc.perform(put("/api/admin/users/{id}", id).with(user("admin").roles("ADMIN"))
                .param("fullName", "Tên khác"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/users/{id}/visibility", id).with(user("admin").roles("ADMIN")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void invalidRoleFilterAndMalformedCreateBodyReturn400() throws Exception {
        mvc.perform(get("/api/admin/users").with(user("admin").roles("ADMIN")).param("role", "UNKNOWN"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
