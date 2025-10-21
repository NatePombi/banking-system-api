package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;



    @Nested
    class Register {
        @Test
        void testRegister_Success() throws Exception {
            RegisterDto reg = new RegisterDto("tester", "email", "test123");

            mvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());
        }

        @Test
        void testRegister_FailBadRequestNoUsername() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setEmail("test@gmail.com");
            reg.setPassword("test123");

            mvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_FailBadRequestNoEmail() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setUsername("tester");
            reg.setPassword("test123");

            mvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_FailBadRequestNoPassword() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setEmail("test@gmail.com");
            reg.setUsername("tester");

            mvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class login {
        @Test
        void testLogin_Success() throws Exception {
            register();
            LoginDto dto = new LoginDto("tester1","test123");

            mvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        void testLogin_FailBadRequestNoUsername() throws Exception {
            register();
            LoginDto dto = new LoginDto();
            dto.setPassword("test123");

            mvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testLogin_FailBadRequestNoPassword() throws Exception {
            register();
            LoginDto dto = new LoginDto();
            dto.setUsername("tester1");

            mvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    private void register() throws Exception {
        RegisterDto reg = new RegisterDto("tester1", "email", "test123");

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
    }
}
