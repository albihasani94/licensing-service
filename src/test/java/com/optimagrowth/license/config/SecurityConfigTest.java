package com.optimagrowth.license.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimagrowth.license.controller.LicenseController;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicenseController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LicenseService licenseService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void licenseEndpointsRequireBearerToken() throws Exception {
        mockMvc.perform(get("/v1/organization/1/license/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void licenseEndpointsRejectJwtWithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/v1/organization/1/license/1").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLicenseAllowsUserAndAdminRoles() throws Exception {
        when(licenseService.getLicense(1L)).thenReturn(license());

        mockMvc.perform(get("/v1/organization/1/license/1").with(jwt()
                        .authorities(() -> "ROLE_user")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/organization/1/license/1").with(jwt()
                        .authorities(() -> "ROLE_admin")))
                .andExpect(status().isOk());
    }

    @Test
    void updateLicenseAllowsUserAndAdminRoles() throws Exception {
        when(licenseService.createOrUpdate(any(License.class))).thenReturn(license());

        mockMvc.perform(put("/v1/organization/1/license")
                        .with(jwt().authorities(() -> "ROLE_user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(license())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/organization/1/license")
                        .with(jwt().authorities(() -> "ROLE_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(license())))
                .andExpect(status().isOk());
    }

    @Test
    void createLicenseAllowsUserAndAdminRoles() throws Exception {
        when(licenseService.createOrUpdate(any(License.class))).thenReturn(license());

        mockMvc.perform(post("/v1/organization/1/license")
                        .with(jwt().authorities(() -> "ROLE_user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(license())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/organization/1/license")
                        .with(jwt().authorities(() -> "ROLE_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(license())))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLicenseAllowsAdminRoleOnly() throws Exception {
        when(licenseService.deleteLicense(1L)).thenReturn("deleted");

        mockMvc.perform(delete("/v1/organization/1/license/1").with(jwt()
                        .authorities(() -> "ROLE_user")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/v1/organization/1/license/1").with(jwt()
                        .authorities(() -> "ROLE_admin")))
                .andExpect(status().isOk());
    }

    private License license() {
        License license = new License();
        license.setLicenseId(1L);
        license.setOrganizationId(1L);
        license.setDescription("Software product");
        license.setProductName("Ostock");
        license.setLicenseType("full");
        return license;
    }
}
