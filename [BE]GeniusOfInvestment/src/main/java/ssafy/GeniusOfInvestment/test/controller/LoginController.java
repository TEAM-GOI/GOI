package ssafy.GeniusOfInvestment.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment._common.response.SuccessResponse;
import ssafy.GeniusOfInvestment._common.response.SuccessType;
import ssafy.GeniusOfInvestment.auth.service.AuthTokenService;
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
    public SuccessResponse<String> login(@RequestBody String request){

        if(!loginService.checkLoginInfo(request)) {
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
        }
        String token = jwtUtil.generateAccessToken(request);
        return SuccessResponse.of(SuccessType.LOGIN_SUCCESSFULLY,token);
    }
}
