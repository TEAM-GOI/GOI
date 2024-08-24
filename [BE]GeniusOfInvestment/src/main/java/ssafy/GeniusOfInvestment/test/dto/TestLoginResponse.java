package ssafy.GeniusOfInvestment.test.dto;

import lombok.Builder;

@Builder
public record TestLoginResponse (
    Long userId,
    String token
){}
