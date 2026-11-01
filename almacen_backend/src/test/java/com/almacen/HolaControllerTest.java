package com.almacen;

import com.almacen.controller.HolaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HolaController.class)
@AutoConfigureMockMvc(addFilters = false)
class HolaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saludaAlMundo() throws Exception {
        mockMvc.perform(get("/api/hola"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("¡Hola Mundo desde Spring Boot!"));
    }
}
