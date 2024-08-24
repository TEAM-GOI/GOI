package ssafy.GeniusOfInvestment._common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {


    //도메인별로 ErrorType 정의, 에러 발생 시 errorCode,msg 반환

    //****************************User****************************//
    NOT_FOUND_USER(HttpStatus.UNAUTHORIZED,"등록된 사용자가 없어요 !"),
    ALREADY_EXIST_USER_NICKNAME(HttpStatus.UNAUTHORIZED, "이미 존재하는 닉네임 입니다"),
    NOT_VALID_USER_NICKNAME(HttpStatus.BAD_REQUEST, "변경하려는 닉네임이 본인 닉네임 입니다"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 리프레쉬 토큰입니다"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 엑세스 토큰입니다"),
    FAIL_TO_GENERATE_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "엑세스 토큰 생성에 실패했습니다"),
    FAIL_TO_GET_USER_DISCONNECT(HttpStatus.BAD_REQUEST, "웹소켓 연결 해제시 map에서 유저 정보 얻기 실패"),
    NEED_TOKEN(HttpStatus.BAD_REQUEST, "토큰이 필요합니다."),

    //****************************Friend****************************//
    NOT_FOUND_FRIEND_ROOM(HttpStatus.BAD_REQUEST, "채팅하려는 친구 조회에 실패했습니다"),
    ALREADY_EXISTS_FRIEND(HttpStatus.BAD_REQUEST,"이미 친구가 맺어져있어요 !"),

    //****************************Alarm****************************//
    NOT_FOUND_INVITE_USER(HttpStatus.BAD_REQUEST, "해당하는 유저가 없어요 !"),
    NOT_FOUND_INVITATION(HttpStatus.BAD_REQUEST, "초대 요청을 찾지 못했습니다"),
    NOT_INVITE_YOURSELF(HttpStatus.BAD_REQUEST,"본인을 초대할 수 없어요 !"),
    //****************************Alarm****************************//
    NOT_FOUND_FRIEND(HttpStatus.BAD_REQUEST, "삭제할 친구 정보가 없습니다"),
    ALREADY_EXISTS_ALARM(HttpStatus.BAD_REQUEST,"이미 요청을 보낸 상태에요 !"),

    //****************************Room****************************//
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"입장하려는 방이 존재하지 않습니다"),
    IS_NOT_AVAILABLE_REDISUSER(HttpStatus.I_AM_A_TEAPOT,"유저동선 추적오류가 발생했습니다"),
    NOT_AVAILABLE_CHANNEL(HttpStatus.BAD_REQUEST,"해당채널이 존재하지 않습니다"),
    NOT_VALID_FRIEND_NUM(HttpStatus.BAD_REQUEST,"삭제할 친구가 나의 친구가 아닙니다"),
    //******************** Square & Room **************************//
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND,"해당채널이 존재하지 않습니다."),
    NOT_FOUND_REDISUSER(HttpStatus.I_AM_A_TEAPOT,"유저동선 추적오류 입니다."),
    INVALID_PASSWORD(HttpStatus.LOCKED, "비밀번호가 일치하지 않습니다."),
    IS_FULL_ROOM(HttpStatus.UPGRADE_REQUIRED, "방이 가득찼다."),
    IS_NOT_AVAILABLE_REDIS_GAMEROOM(HttpStatus.FORBIDDEN, "레디스에 방 정보가 없습니다."),
    FAIL_TO_CREATE_ROOM(HttpStatus.BAD_REQUEST, "방 생성에 실패했습니다."),

    //******************** CHANNEL **************************//
    CHANNEL_IS_FULL(HttpStatus.NOT_FOUND,"입장하려는 채널이 가득 찼습니다."),
    NOT_FOUND_CHANNEL(HttpStatus.I_AM_A_TEAPOT,"채널이 존재하지 않습니다 이놈아."),
    ERR_CREATE_ROOMNUM(HttpStatus.FORBIDDEN, "방 번호 생성 실패"),

    //****************************Game****************************//
    IS_NOT_MANAGER(HttpStatus.UNAUTHORIZED, "방장만이 요청을 할 수 있습니다."),
    NOT_YET_READY(HttpStatus.FORBIDDEN, "모든 사용자가 레디를 해야됩니다."),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "존재하는 방 정보가 아닙니다."),
    NOT_FOUND_INFO(HttpStatus.NOT_FOUND, "해당하는 정보가 존재하지 않습니다."),
    NOT_FOUND_USER_IN_ROOM(HttpStatus.NOT_FOUND, "방에 해당 유저가 존재하지 않습니다."),
    INSUFFICIENT_POINT(HttpStatus.NOT_ACCEPTABLE, "보유하신 포인트가 부족합니다."),
    INSUFFICIENT_BALANCE(HttpStatus.NOT_ACCEPTABLE, "잔고가 부족합니다."),
    NOT_FOUND_TRADINGINFO(HttpStatus.NOT_FOUND, "나의 거래 내역이 존재하지 않습니다."),
    NOT_FOUND_STOCK_ITEM(HttpStatus.NOT_FOUND, "해당하는 주식 종목을 찾을 수 없습니다."),
    NOT_STORE_MARKET(HttpStatus.CONFLICT, "시장 상황이 제대로 저장되지 않았습니다."),
    ALREADY_BUY_INFO(HttpStatus.FAILED_DEPENDENCY, "이미 구매한 정보입니다."),
    END_GAME(HttpStatus.GONE, "게임이 종료되었습니다."),


    //****************************Test****************************//
    IS_ALREADY_EXIST_NICKNAME(HttpStatus.CONFLICT,"이미 존재하는 UUID 입니다.");

    private final HttpStatus status; //http status
    private final String msg; //error message

}
