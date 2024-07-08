package ssafy.GeniusOfInvestment.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment.user.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;

    public boolean checkLoginInfo(String id, String pwd) {
        Optional<User> byId = userRepository.findById(Long.valueOf(id));
        if (byId.isEmpty())
            return false;
        else if (byId.get().getId().equals(id) && byId.get().getPassword().equals(pwd))
            return true;
        return false;
    }
}
