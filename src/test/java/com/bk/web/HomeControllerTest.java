package com.bk.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;
import com.bk.security.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = HomeController.class, properties = {
        "admin.username=test-admin",
        "admin.password=test-password"
})
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Choose breakfast for today.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/bread.png")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/chapati.jpg")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/andazi.jpg")));
    }

    @Test
    void adminPagesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("http://localhost/admin/login"));
    }
}
