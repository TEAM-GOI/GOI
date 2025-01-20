package ssafy.GeniusOfInvestment.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.jwt.JwtUtil;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment.test.dto.TestLoginResponse;
import ssafy.GeniusOfInvestment.user.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TestLoginResponse testUserLogin(String request) {

        if (userRepository.existsByNickName(request))
            throw new CustomBadRequestException(ErrorType.IS_ALREADY_EXIST_NICKNAME);

        String socialId = generateSocialId();
        User user = userRepository.save(new User(socialId,0L,1,request));

        isValidateLoginInfo(String.valueOf(user.getId()));

        String token = jwtUtil.generateAccessToken(String.valueOf(user.getId()));
        return buildTestLoginResponse(token,user);
    }

    private String generateSocialId() {
        String socialId = UUID.randomUUID().toString();
        while(userRepository.findBySocialId(socialId).isPresent()){
            socialId = UUID.randomUUID().toString();
        }
        return socialId;
    }

    public void isValidateLoginInfo(String id) {
        userRepository.findById(Long.valueOf(id))
                .orElseThrow(()-> new CustomBadRequestException(ErrorType.NOT_FOUND_USER));
    }

    private static TestLoginResponse buildTestLoginResponse(String token, User user) {
        return TestLoginResponse.builder().token(token).userId(user.getId()).build();
    }
}
