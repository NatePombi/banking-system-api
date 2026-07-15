package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.user.LoginDto;
import com.nate.bankingsystemapi.dto.user.RegisterDto;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.enums.UserStatus;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwtService;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void startUp(){
        User user = User.createUser("Test Tester","tester1",encoder.encode("test123"));
        User user2 = User.createUser("Test Tester2","tester2",encoder.encode("test123"));
        User user3 = User.createUser("Test Tester3","tester3",encoder.encode("test123"));
        testUser = userRepository.save(user);
        userRepository.save(user2);
        userRepository.save(user3);

        testAdmin = User.createUser("Admin","admin1","admin123");
        testAdmin.changeRole(Role.ADMIN);
        userRepository.save(testAdmin);

    }


    @Nested
    class Register {
        @Test
        void testRegister_Success() throws Exception {
            RegisterDto reg = new RegisterDto("Tester","tester", "test@gmail.com", "test123");

            mvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fullName").value("Tester"))
                    .andExpect(jsonPath("$.username").value("tester"))
                    .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                    .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.toString()));
        }

        @Test
        void testRegister_FailBadRequestNoFullName() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setUsername("tester");
            reg.setEmail("test@gmail.com");
            reg.setPassword("test123");

            mvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_FailBadRequestNoUsername() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setEmail("test@gmail.com");
            reg.setPassword("test123");

            mvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_FailBadRequestNoEmail() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setUsername("tester");
            reg.setPassword("test123");

            mvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_FailBadRequestNoPassword() throws Exception {

            RegisterDto reg = new RegisterDto();
            reg.setEmail("test@gmail.com");
            reg.setUsername("tester");

            mvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(reg)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class login {
        @Test
        void testLogin_Success() throws Exception {
            LoginDto dto = new LoginDto("tester1","test123");


            mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        void testLogin_FailBadRequestNoUsername() throws Exception {
            LoginDto dto = new LoginDto();
            dto.setPassword("test123");

            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testLogin_FailBadRequestNoPassword() throws Exception {
            LoginDto dto = new LoginDto();
            dto.setUsername("tester1");

            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class admin{
        @Test
        void shouldGetUsersForAdmin() throws Exception {
            String token = jwtService.generateToken(testAdmin);

            mvc.perform(get("/api/v1/admin/fetch")
                            .header("Authorization", "Bearer " + token)
                    .param("page","0")
                    .param("size","5")
                    .param("sortBy","id")
                    .param("direction","desc")
                    .header("Authorization","Bearer "+token)
                    .with(csrf()))
                    .andExpect(status().isOk());

        }

        @Test
        void shouldFailGetUsersForAdmin_AdminNotFound() throws Exception {

            mvc.perform(get("/api/v1/admin/fetch")
                    .param("page","0")
                    .param("size","5")
                    .param("sortBy","id")
                    .param("direction","desc")
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }


        @Test
        void shouldGetUserByIdForAdmin() throws Exception {
            String token = jwtService.generateToken(testAdmin);

            Long id = testUser.getId();

            mvc.perform(get("/api/v1/admin/fetch/" + id)
                    .header("Authorization", "Bearer " + token)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.fullName").value("Test Tester"))
                    .andExpect(jsonPath("$.username").value("tester1"))
                    .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                    .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.toString()));
        }
    }


}
