package ssafy.GeniusOfInvestment.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.exception.CustomRoomEnterException;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment._common.response.SuccessResponse;
import ssafy.GeniusOfInvestment._common.response.SuccessType;
import ssafy.GeniusOfInvestment.test.dto.*;
import ssafy.GeniusOfInvestment.test.service.LoginService;
import ssafy.GeniusOfInvestment.user.repository.UserRepository;
import ssafy.GeniusOfInvestment.user.service.UserService;

import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    // uuid 보내면 자동 회원가입 및 token 과 userid 반환
    @PostMapping("/dev/{uuid}")
    public SuccessResponse<TestLoginResponse> login(@PathVariable("uuid") String request){
        Random random = new Random();
        if (userRepository.existsByNickName(request))
            throw new CustomBadRequestException(ErrorType.IS_ALREADY_EXIST_NICKNAME);

        String socialId = String.valueOf(
                random.nextLong(8999999999L)+1000000000L);
        while(userRepository.findBySocialId(socialId).isPresent()){
            socialId = String.valueOf(
                    random.nextLong(8999999999L)+1000000000L);
        }
        User user = userRepository.save(new User(socialId,0L,1,request));

        if(!loginService.checkLoginInfo(String.valueOf(user.getId()))) {
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
        }
        String token = jwtUtil.generateAccessToken(String.valueOf(user.getId()));

        return SuccessResponse.of(SuccessType.LOGIN_SUCCESSFULLY,TestLoginResponse.builder().token(token).userId(user.getId()).build());
    }



}
