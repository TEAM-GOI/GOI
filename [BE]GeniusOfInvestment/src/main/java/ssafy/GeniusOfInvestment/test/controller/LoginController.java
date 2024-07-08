package ssafy.GeniusOfInvestment.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment._common.response.SuccessResponse;
import ssafy.GeniusOfInvestment._common.response.SuccessType;
import ssafy.GeniusOfInvestment.auth.service.AuthTokenService;
import ssafy.GeniusOfInvestment.test.dto.request.UserLoginRequest;
import ssafy.GeniusOfInvestment.test.dto.response.TestResponse;
import ssafy.GeniusOfInvestment.test.service.LoginService;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class LoginController {
    private final AuthTokenService authTokenService;
    private final LoginService loginService;

    private final JwtUtil jwtUtil;
    @PostMapping("/dev")
    public SuccessResponse<String> login(@RequestBody UserLoginRequest request){

        if(!loginService.checkLoginInfo(request.userId(),request.userPwd())) {
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
        }
        String token = jwtUtil.generateAccessToken(request.userId());
        return SuccessResponse.of(SuccessType.LOGIN_SUCCESSFULLY,token);
    }
}
