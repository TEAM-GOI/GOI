package ssafy.GeniusOfInvestment.game.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment._common.stomp.dto.MessageDto;
import ssafy.GeniusOfInvestment.game.dto.ParticipantInfo;
import ssafy.GeniusOfInvestment.game.dto.ReadyResponse;
import ssafy.GeniusOfInvestment.game.service.GameService;
import ssafy.GeniusOfInvestment.game.service.TimerService;
import ssafy.GeniusOfInvestment.game.dto.TurnResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final SimpMessageSendingOperations messageTemplate;
    private final TimerService timerService;

    //게임 시작시 초기 주식 정보를 넘겨준다.
    @GetMapping("/start")
    public Map<String, String> getInitStockInfo(@AuthenticationPrincipal User user, @RequestParam("id") Long grId){
        TurnResponse rst = gameService.getInitStockInfo(user, grId);
        //방 채팅과 보내는 주소와 데이터 형식을 맞춰야 될듯
        sendMsg(grId, rst, MessageDto.MessageType.STOCK_MARKET); //웹소켓으로 게임에 참가한 모든 이용자들에게 초기 주식 정보를 보낸다.
        timerService.setTimer(grId); //비동기적으로(멀티 쓰레드 환경)으로 타이머 실행(100ms 뒤에 타이머 실행)
        Map<String, String> json = new HashMap<>();
        json.put("msg", "게임 시작 완료");
        return json;
    }

    //다음 턴 넘기기(참가한 모든 유저에 대한 평가 금액도 모두 함께 계산)
    @GetMapping("/next")
    public Map<String, String> getNextStockInfo(@AuthenticationPrincipal User user, @RequestParam("id") Long grId){
        TurnResponse rst = gameService.getNextStockInfo(grId);
        sendMsg(grId, rst, MessageDto.MessageType.STOCK_MARKET); //웹소켓으로 게임에 참가한 모든 이용자들에게 다음 턴 주식 정보를 보낸다.
        timerService.setTimer(grId); //비동기적으로(멀티 쓰레드 환경)으로 타이머 실행(100ms 뒤에 타이머 실행)
        Map<String, String> json = new HashMap<>();
        json.put("msg", "다음 턴 시작");
        return json;
    }

    //유저가 레디를 눌렀다.
    @PutMapping("/ready/{id}")
    public Map<String, String> doingReady(@AuthenticationPrincipal User user, @PathVariable("id") Long grId){
        int status = gameService.doingReady(user, grId);
        Map<String, String> json = new HashMap<>();
        if(status == 0 || status == -1){ //아직 전체 참여자가 레디를 다 누르지 않았다.
            //status = 0이면 레디를 한것, status = -1이면 레디를 취소한 것
            sendMsg(grId,
                    ReadyResponse.builder()
                            .userId(user.getId())
                            .ready(status == 0)
                            .build(),
                    MessageDto.MessageType.READY);
            json.put("msg", "레디 완료");
        }else { //참여자 전체가 레디를 하였다.(바로 다음 턴으로 넘긴다.)
            TurnResponse rst = gameService.getNextStockInfo(grId);
            sendMsg(grId, rst, MessageDto.MessageType.STOCK_MARKET);
            timerService.setTimer(grId); //비동기적으로(멀티 쓰레드 환경)으로 타이머 실행(100ms 뒤에 타이머 실행)
            json.put("msg", "다음 턴 넘기기 완료");
        }
        return json;
    }

    //게임 정상 종료
    @PutMapping("/end/{id}")
    public Map<String, String> endGame(@PathVariable("id") Long grId){
        List<ParticipantInfo> rst = gameService.endGame(grId);
        sendMsg(grId, rst, MessageDto.MessageType.GAME_RESULT);
        Map<String, String> json = new HashMap<>();
        json.put("msg", "게임 종료");
        return json;
    }

    //유저 게임방 탈퇴
    @DeleteMapping("/exit/{id}")
    public Map<String, String> exitGame(@AuthenticationPrincipal User user, @PathVariable("id") Long grId){
        //게임을 나갈때 웹소켓으로 뭘 보내줘야되지??
        int num = gameService.exitGame(user, grId);
        Map<String, String> json = new HashMap<>();
        if(num == 1){ //1명이 남았을 경우에는 게임을 종료
            List<ParticipantInfo> rst = gameService.endGame(grId);
            sendMsg(grId, rst, MessageDto.MessageType.GAME_RESULT);
            json.put("msg", "게임 종료");
            return json;
        }else {
            json.put("msg", "게임 탈퇴");
            //임시(얘기 해봐야 될듯)
            sendMsg(grId, json, MessageDto.MessageType.GAME_RESULT);
            return json;
        }
    }



    public void sendMsg(Long grId, Object obj, MessageDto.MessageType type){
        messageTemplate.convertAndSend("/sub/room/chat/" + grId,
                MessageDto.builder()
                        .type(type)
                        .data(obj)
                        .build()); //웹소켓으로 게임에 참가한 모든 이용자들에게 게임 결과를 보낸다.
    }
}
