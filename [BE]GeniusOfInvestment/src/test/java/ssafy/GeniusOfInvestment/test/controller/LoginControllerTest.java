package ssafy.GeniusOfInvestment.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;
import ssafy.GeniusOfInvestment._common.config.SecurityConfig;
import ssafy.GeniusOfInvestment._common.jwt.JwtAuthorizationFilter;
import ssafy.GeniusOfInvestment._common.jwt.JwtExceptionFilter;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import ssafy.GeniusOfInvestment._common.oauth2.handler.OAuth2AuthenticationFailureHandler;
import ssafy.GeniusOfInvestment._common.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import ssafy.GeniusOfInvestment._common.oauth2.service.CustomOAuth2UserService;
import ssafy.GeniusOfInvestment.test.dto.TestLoginResponse;
import ssafy.GeniusOfInvestment.test.service.LoginService;
import ssafy.GeniusOfInvestment.user.service.UserService;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WebMvcTest(controllers = LoginController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(SecurityConfig.class)
class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    //json과 객체간의 직렬화와 역직렬화를 도와주는 것
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService loginService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @MockBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    @MockBean
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    @MockBean
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    @MockBean
    private JwtExceptionFilter jwtExceptionFilter;

    @MockBean
    private UserService userService;

    @DisplayName("UUID로 로그인을 시도힌다.")
    @Test
    void login() throws Exception {
        // given
        String uuid = UUID.randomUUID().toString();

        // when
        when(loginService.testUserLogin(uuid)).thenReturn(TestLoginResponse.builder().build());

        // then
        mockMvc.perform(
                    post("/api/test/dev/" + uuid)
                            .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("로그인 성공"))
                .andExpect(jsonPath("$.data").hasJsonPath());

    }

}