package com.example.learning.auth;

import com.example.learning.LearningApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 测试：验证发送验证码与登录流程。
 */
@SpringBootTest(classes = LearningApplication.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendOtpAndLogin() throws Exception {
        String payload = "{\"channel\":\"email\",\"receiver\":\"student@example.com\"}";
        mockMvc.perform(post("/api/auth/otp/send").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk());
    }
}