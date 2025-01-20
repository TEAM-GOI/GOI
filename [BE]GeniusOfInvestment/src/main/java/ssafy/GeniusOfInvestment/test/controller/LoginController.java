package ssafy.GeniusOfInvestment.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment._common.response.SuccessResponse;
import ssafy.GeniusOfInvestment._common.response.SuccessType;
import ssafy.GeniusOfInvestment.test.dto.*;
import ssafy.GeniusOfInvestment.test.service.LoginService;
import ssafy.GeniusOfInvestment.user.repository.UserRepository;

import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    // uuid 보내면 자동 회원가입 및 token 과 userid 반환
    @PostMapping("/dev/{uuid}")
    public SuccessResponse<TestLoginResponse> login(@PathVariable("uuid") String request){
        return SuccessResponse.of(SuccessType.LOGIN_SUCCESSFULLY,loginService.testUserLogin(request));
    }



}
