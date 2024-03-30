package ssafy.GeniusOfInvestment.game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.GeniusOfInvestment._common.entity.Information;
import ssafy.GeniusOfInvestment._common.entity.Room;
import ssafy.GeniusOfInvestment._common.exception.CustomBadRequestException;
import ssafy.GeniusOfInvestment._common.redis.*;
import ssafy.GeniusOfInvestment._common.response.ErrorType;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment.game.repository.InformationRepository;
import ssafy.GeniusOfInvestment.game.repository.RedisGameRepository;
import ssafy.GeniusOfInvestment.game.dto.*;
import ssafy.GeniusOfInvestment.game.repository.RedisMyTradingInfoRepository;
import ssafy.GeniusOfInvestment.square_room.repository.RedisUserRepository;
import ssafy.GeniusOfInvestment.square_room.repository.RoomRepository;
import ssafy.GeniusOfInvestment.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final RedisGameRepository gameRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RedisUserRepository redisUserRepository;
    private final InformationRepository informationRepository;
    private final RedisMyTradingInfoRepository myTradingInfoRepository;

    public void startGame(User user, Long grId){
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }

        //방 상태 바꾸기
        Optional<Room> rinfo = roomRepository.findById(grId);
        if(rinfo.isEmpty()){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }
        //나중에 setter지우고 update 메소드 만들기
        rinfo.get().updateStatus(1); //방 상태를 게임 중으로 바꾼다.(이걸로 바로 DB에 반영이 되나?)

        GameUser gu = new GameUser();
        gu.setUserId(user.getId());
        int idx = room.getParticipants().indexOf(gu);
        if(idx == -1) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER_IN_ROOM);

        gu = room.getParticipants().get(idx);
        if(!gu.isManager()){ //요청을 한 사용자가 방장이 아니다.
            throw new CustomBadRequestException(ErrorType.IS_NOT_MANAGER);
        }

        for(GameUser guser : room.getParticipants()){
            if(!guser.isReady() && !guser.isManager()){ //방장이 아닌 사용자가 아직 레디를 누르지 않았다.
                throw new CustomBadRequestException(ErrorType.NOT_YET_READY);
            }

            RedisUser rdu = redisUserRepository.getOneRedisUser(guser.getUserId());
            if(rdu == null) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            rdu.setStatus(true); //상태 true가 게임중
            redisUserRepository.updateUserStatusGameing(rdu); //각 유저마다의 상태값을 변경
        }
        roomRepository.save(rinfo.get());
    }

    @Transactional
    public TurnResponse getInitStockInfo(User user, Long grId){ //grId는 방 테이블의 아이디값
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }

        //방 상태 바꾸기
        Optional<Room> rinfo = roomRepository.findById(grId);
        if(rinfo.isEmpty()){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }
        int year = rinfo.get().getFromYear(); //방에 설정된 시작년도 불러오기
        int turn = rinfo.get().getTurnNum(); //방에 설정된 총 턴수 불러오기
        //나중에 setter지우고 update 메소드 만들기
        //rinfo.get().updateStatus(1); //방 상태를 게임 중으로 바꾼다.(이걸로 바로 DB에 반영이 되나?)

        log.info("rinfo here1!!!!");
        List<ParticipantInfo> parts = new ArrayList<>();
        List<GameUser> gameUserList = new ArrayList<>();
        for(GameUser guser : room.getParticipants()){
//            if(guser.isManager() && !Objects.equals(guser.getUserId(), user.getId())){ //요청을 한 사용자가 방장이 아니다.
//                throw new CustomBadRequestException(ErrorType.IS_NOT_MANAGER);
//            }
//            if(!guser.isReady() && !guser.isManager()){ //방장이 아닌 사용자가 아직 레디를 누르지 않았다.
//                throw new CustomBadRequestException(ErrorType.NOT_YET_READY);
//            }
            Optional<User> unick = userRepository.findById(guser.getUserId());
            if(unick.isEmpty()){
                throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            }
            parts.add(ParticipantInfo.builder()
                            .userId(guser.getUserId())
                            .userNick(unick.get().getNickName())
                            .totalCost(500000L)
                            .point(3)
                    .build()); //참가자들 초기 게임 정보를 저장(응답용)

//            RedisUser rdu = redisUserRepository.getOneRedisUser(guser.getUserId());
//            if(rdu == null) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
//            rdu.setStatus(true); //상태 true가 게임중
//            redisUserRepository.updateUserStatusGameing(rdu); //각 유저마다의 상태값을 변경

            //GameUser(참가자)의 상태값을 변경
            guser.setReady(false);
            guser.setTotalCost(500000L);
            guser.setPoint(3);
            gameUserList.add(guser);
        }

        log.info("주식 현황 저장하기 전!!!!");
        List<Items1> selectedOne = Stream.of(Items1.values()) //6개 중에서 4개 선택
                .limit(4)
                .toList();

        List<Items2> selectedTwo = Stream.of(Items2.values()) //4개 중에서 3개 선택
                .limit(3)
                .toList();

        List<StockInfoResponse> stockInfos = new ArrayList<>();
        selTwoItems(stockInfos, selectedTwo);
        selOneItems(stockInfos, selectedOne);
        for(StockInfoResponse st : stockInfos){
            System.out.print(st.getItem() + " ");
        }

        //redis의 GameMarket객체에 현재(초기) 시장 상황 저장하기
        List<GameMarket> gms = new ArrayList<>();
        for(StockInfoResponse stok : stockInfos){
            List<Long> tmp = new ArrayList<>();
            tmp.add(stok.getThisCost());
            gms.add(GameMarket.builder()
                            .item(stok.getItem())
                            .Cost(tmp)
                    .build());
        }

        //GameRoom(redis)에 정보 업데이트
        room.setRemainTurn(turn-1); //남은 턴수 설정
        room.setYear(year); //게임의 현재 년도 설정
        room.setParticipants(gameUserList); //상태값이 변경된 새로운 리스트를 저장
        room.setMarket(gms); //새로 생성된 시장 상황을 저장
        gameRepository.updateGameRoom(room); //redis에 관련 정보를 저장

        //방 상태 변경 내역을 저장
        //roomRepository.save(rinfo.get());

        log.info("start service에서 리턴하기 전");
        return TurnResponse.builder()
                .remainTurn(turn-1)
                .year(year)
                .participants(parts)
                .stockInfo(stockInfos)
                .build();
    }

    public void selOneItems(List<StockInfoResponse> stockInfos, List<Items1> selectedOne){
        for(int i=0; i<4; i++){
            StockInfoResponse stk = new StockInfoResponse();
            StringBuilder sb = new StringBuilder();
            System.out.println(selectedOne.get(i).toString());
            sb.append("A");
            switch (selectedOne.get(i).toString()){
                case "FOOD":
                    sb.append(" 식품");
                    break;
                case "ENTER":
                    sb.append(" 엔터");
                    break;
                case "TELECOM":
                    sb.append(" 통신");
                    break;
                case "AIR":
                    sb.append(" 항공");
                    break;
                case "CONSTRUCT":
                    sb.append(" 건설");
                    break;
                case "BEAUTY":
                    sb.append(" 뷰티");
                    break;
            }
            stk.setItem(sb.toString());
            stk.setThisCost(createRandCost());
            stockInfos.add(stk);
        }
    }

    public void selTwoItems(List<StockInfoResponse> stockInfos, List<Items2> selectedTwo){
        for(int i=0; i<3; i++){
            System.out.println(selectedTwo.get(i).toString());
            switch (selectedTwo.get(i).toString()){
                case "BIO":
                    addTwoItems(stockInfos, "바이오");
                    break;
                case "IT":
                    addTwoItems(stockInfos, "IT");
                    break;
                case "CHEMICAL":
                    addTwoItems(stockInfos, "화학");
                    break;
                case "CAR":
                    addTwoItems(stockInfos, "자동차");
                    break;
            }
        }
    }

    //해당 종목을 2개씩 추가한다.
    public void addTwoItems(List<StockInfoResponse> stockInfos, String item){
        StockInfoResponse stk = new StockInfoResponse();
        StringBuilder sb = new StringBuilder();
        sb.append("A ");
        sb.append(item);
        stk.setItem(sb.toString());
        stk.setThisCost(createRandCost());
        stockInfos.add(stk);
        sb.setLength(0); //stringbuilder를 비운다.
        stk = new StockInfoResponse();
        sb.append("B ");
        sb.append(item);
        stk.setItem(sb.toString());
        stk.setThisCost(createRandCost());
        stockInfos.add(stk);
    }

    public long createRandCost(){
        int min = 3000;
        int max = 300001;

        // Random 객체를 생성합니다.
        Random random = new Random();

        // 범위 내에서 랜덤 숫자를 생성합니다.
        return (random.nextInt(max - min) + min) / 100;
    }

    //redis에 대한 transactional을 걸게 되면 multi-exec가 걸리게 되므로 중간에 redis에 저장한 값을 메소드가 끝나기전에 get을 해올 수 없다.
    //redis에 저장자체가 메소드가 모두 끝난 후에 실행되므로
    @Transactional
    public TurnResponse getNextStockInfo(Long grId){
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }
        Optional<Room> rm = roomRepository.findById(grId);
        if(rm.isEmpty()) throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        if(rm.get().getEndYear() == room.getYear()){ //게임이 끝났다.
            throw new CustomBadRequestException(ErrorType.END_GAME);
        }
        int turn = room.getRemainTurn() - 1; //턴이 넘어간 후 남은 턴수(0이면 마지막 턴)
        int year = room.getYear() + 1; //턴이 넘어간 후 현재 년도
        //--------------------------------------------------------------
        //전체 시장 상황을 업데이트
        List<StockInfoResponse> stockInfos = new ArrayList<>();
        List<GameMarket> gms = new ArrayList<>();
        for(GameMarket mk : room.getMarket()){
            Long cur; //현재(새로운) 가격
            int size = mk.getCost().size();
            Long last = mk.getCost().get(size-1); //마지막 인덱스에 있는 것이 가장 최근의 가격
            int roi; //수익률
            if(mk.getDependencyInfo() != null){ //사용자들이 이 종목에 대해서 정보를 구매했다.
                Optional<Information> usrBuy = informationRepository.findById(mk.getDependencyInfo());
                if(usrBuy.isEmpty()) throw new CustomBadRequestException(ErrorType.NOT_FOUND_INFO);
                roi = usrBuy.get().getRoi();
                cur = calMarketVal(last, roi);
            }else {
                Long itemId = getIdForItem(mk.getItem()); //각 종목의 아이디값
                List<Information> infos = informationRepository.findByAreaIdAndYear(itemId, room.getYear());
                Random random = new Random();
                int randIdx = random.nextInt(infos.size());
                Information ranInfo = infos.get(randIdx);
                roi = ranInfo.getRoi();
                cur = calMarketVal(last, roi);
            }
            mk.getCost().add(cur); //새로 계산된 가격을 원래의 가격 리스트에 추가
            //redis에 저장될 시장 상황을 업데이트
            gms.add(GameMarket.builder()
                    .item(mk.getItem())
                    .Cost(mk.getCost())
                    .build());

            //응답을 줄 dto에 정보 업데이트
            stockInfos.add(StockInfoResponse.builder()
                            .item(mk.getItem())
                            .lastCost(last)
                            .thisCost(cur)
                            .percent(roi)
                    .build());
        }

        //----------------------------------------------------------------
        //각 유저에 대한 거래내역을 업데이트
        List<ParticipantInfo> parts = new ArrayList<>();
        List<GameUser> gameUserList = new ArrayList<>();
        for(GameUser guser : room.getParticipants()){
            MyTradingInfo myInfo = myTradingInfoRepository.getOneMyTradingInfo(guser.getUserId());
            if(myInfo == null) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            Long remain = myInfo.getRemainVal(); //나의 잔고
            List<BreakDown> bdowns = myInfo.getBreakDowns();
            //List<BreakDown> newbdowns = new ArrayList<>(); //새로운 BreakDown 정보를 저장할 리스트
            Long usrTotal = 0L;
            for(BreakDown bd : bdowns){
                for(StockInfoResponse totalInfo : stockInfos){
                    if(bd.getItem().equals(totalInfo.getItem())){ //내가 산 주식 종목에 해당하는 수익률 정보를 전체 주식 정보에서 얻는다.
                        //산 금액과 이전 턴에서의 금액을 분리??
                        Long nowVal = calMarketVal(bd.getNowVal(), totalInfo.getPercent()); //평가금액을 주식 상황에 맞게 업데이트
                        bd.setNowVal(nowVal);
                        Long buy = bd.getBuyVal();
                        bd.setRoi(calRoiByVal(buy, nowVal));
                        usrTotal += nowVal; //투자한 종목들의 업데이트된 평가 금액의 합
                        break;
                    }
                }
            }

            //MyTradingInfo(redis)에 정보 업데이트
            Long lastmVal = myInfo.getMarketVal(); //작년 전체 평가금액
            Long updatemVal = usrTotal + remain; //업데이트된 전체 평가금액(현재)
            myInfo.setMarketVal(updatemVal);
            myInfo.setInvestVal(usrTotal);
            myInfo.setYoy(updatemVal - lastmVal); //현재 - 작년
            myInfo.setBreakDowns(bdowns);
            myTradingInfoRepository.updateMyTradingInfo(myInfo);

            Optional<User> unick = userRepository.findById(guser.getUserId());
            if(unick.isEmpty()){
                throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            }
            int point = guser.getPoint() + 3;
            parts.add(ParticipantInfo.builder()
                    .userId(guser.getUserId())
                    .userNick(unick.get().getNickName())
                    .totalCost(usrTotal + remain)
                    .point(point)
                    .build()); //참가자들 정보를 저장(응답용)

            //GameUser(참가자)의 상태값을 변경
            guser.setReady(false);
            guser.setTotalCost(usrTotal + remain);
            guser.setPoint(point);
            gameUserList.add(guser);
        }

        //GameRoom(redis)에 정보 업데이트
        room.setRemainTurn(turn); //남은 턴수 설정
        room.setYear(year); //게임의 현재 년도 설정
        room.setParticipants(gameUserList); //상태값이 변경된 새로운 리스트를 저장
        room.setMarket(gms); //새로 생성된 시장 상황을 저장
        gameRepository.updateGameRoom(room); //redis에 관련 정보를 저장

        return TurnResponse.builder()
                .remainTurn(turn)
                .year(year)
                .participants(parts)
                .stockInfo(stockInfos)
                .build();
    }

    public Long calMarketVal(Long cost, int roi){ //수익률로 평가 금액을 계산
        if(roi >= 0){
            System.out.println((long) (cost + (cost * (roi/100d))));
            return (long) (cost + (cost * (roi/100d)));
        }else {
            return (long) (cost - (cost * (roi/100d)));
        }
    }

    public int calRoiByVal(Long last, Long cur){ //지난(매입) 금액과 현재 금액으로 수익률을 계산
        return (int) (Math.abs(1 - (cur/(double)last)) * 100);
    }

    public Long getIdForItem(String itemName){
        if(itemName.contains("IT")){
            return 1L;
        } else if (itemName.contains("자동차")) {
            return 2L;
        } else if (itemName.contains("바이오")) {
            return 3L;
        } else if (itemName.contains("통신")) {
            return 4L;
        } else if (itemName.contains("화학")) {
            return 5L;
        } else if (itemName.contains("엔터")) {
            return 6L;
        } else if (itemName.contains("식품")) {
            return 7L;
        } else if (itemName.contains("항공")) {
            return 8L;
        } else if (itemName.contains("건설")) {
            return 9L;
        } else{
            return 10L;
        }
    }

    public int doingReady(User user, Long grId){
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }

        List<GameUser> gameUserList = new ArrayList<>();
        int cnt = 0;
        int flag = 1;
        for(GameUser guser : room.getParticipants()){
            if(Objects.equals(guser.getUserId(), user.getId())){ //ready를 요청한 사용자이다.
                if(!guser.isReady()){
                    flag = 0; //레디를 한것
                    guser.setReady(true);
                }else{
                    flag = -1; //레디를 취소한것
                    guser.setReady(false);
                }
            }
            if(guser.isReady()){
                cnt++;
            }
            gameUserList.add(guser);
        }

        //GameRoom(redis)에 정보 업데이트
        room.setParticipants(gameUserList);
        gameRepository.updateGameRoom(room);

        if(cnt == room.getParticipants().size()){
            return 1;
        }else { //아직 전체 참여자가 레디를 다 누르지 않았다.
            if(flag == 1) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER_IN_ROOM);
            return flag;
            //return 0;
        }
    }

    @Transactional
    public List<ParticipantInfo> endGame(Long grId) { //게임이 종료되면 순위에 따라 경험치를 적립
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }

        List<ParticipantInfo> parts = new ArrayList<>();
        List<GameUser> gameUserList = new ArrayList<>();
        room.getParticipants().sort(Comparator.reverseOrder()); //totalCost 기준으로 내림차순 정렬
        int i = 1;
        for(GameUser guser : room.getParticipants()){
            Optional<User> unick = userRepository.findById(guser.getUserId());
            if(unick.isEmpty()){
                throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            }

            parts.add(ParticipantInfo.builder()
                    .userId(guser.getUserId())
                    .userNick(unick.get().getNickName())
                    .totalCost(guser.getTotalCost())
                    .build()); //응답용

            rewardByRank(unick.get(), i, guser.getTotalCost()); //순위에 따른 경험치 적립

            RedisUser rdu = redisUserRepository.getOneRedisUser(guser.getUserId());
            if(rdu == null) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
            rdu.setStatus(false); //상태 false가 대기
            redisUserRepository.updateUserStatusGameing(rdu); //각 유저마다의 상태값을 변경

            //나의 거래내역을 삭제한다.
            myTradingInfoRepository.deleteMyTradingInfo(guser.getUserId());

            //GameUser(참가자)의 상태값을 초기화
            guser.setReady(false);
            guser.setTotalCost(0L);
            guser.setPoint(0);
            guser.setBuyInfos(new ArrayList<>()); //초기값을 빈 리스트로 선언
            gameUserList.add(guser);
            i++;
        }
        i = 1;
        for(ParticipantInfo p : parts){ //log찍기(테스트) 용
            System.out.println((i++) + " " + p.getTotalCost());
        }

        Optional<Room> rinfo = roomRepository.findById(grId);
        if(rinfo.isEmpty()){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }
        rinfo.get().updateStatus(0); //방 상태를 대기로 바꾼다.

        //GameRoom(redis)에 정보 초기화(게임이 끝났으므로)
        room.setRemainTurn(0); //남은 턴수 설정
        room.setYear(0); //게임의 현재 년도 설정
        room.setParticipants(gameUserList); //상태값이 변경된 새로운 리스트를 저장
        room.setMarket(null);
        gameRepository.updateGameRoom(room); //redis에 관련 정보를 저장

        return parts;
    }

    //Transactional 을 지워야 되나?
    @Transactional
    public List<ParticipantInfo> exitGame(User user, Long grId) {
        GameRoom room = gameRepository.getOneGameRoom(grId);
        if(room == null){
            throw new CustomBadRequestException(ErrorType.NOT_FOUND_ROOM);
        }

        GameUser gameUser = new GameUser();
        gameUser.setUserId(user.getId()); //탈퇴한 유저의 객체
        int idx = room.getParticipants().indexOf(gameUser);
        if(idx == -1) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
        rewardByRank(user, 4, room.getParticipants().get(idx).getTotalCost()); //4위에 해당하는 패널티 부과
        //userRepository.save(user); //@AuthenticationPrincipal를 통해서 받아온 유저일 경우 .save()를 호출해야 되나??

        room.getParticipants().remove(gameUser);
        //int remainNum = room.getParticipants().size();
        gameRepository.updateGameRoom(room); //redis에 관련 정보를 저장

        redisUserRepository.deleteUser(user.getId()); //대기방도 아니라 광장으로 나가기 때문에 삭제
        //나의 거래내역을 삭제한다.
        myTradingInfoRepository.deleteMyTradingInfo(user.getId());

        return room.getParticipants().stream()
                .map(this::mapToParticipantInfo)
                .collect(Collectors.toList());
    }

    //순위에 따른 경험치 적립(tcost는 이번 게임에서 획득한 금액)
    public void rewardByRank(User user, int rank, Long tcost) {
        long tmp = 0L;
        switch (rank){
            case 1:
                tmp = user.getExp() + tcost; //유저의 원래 경험치 + 이번 게임에서 획득한 금액
                user.updateExp(tmp);
                break;
            case 2:
                tmp = user.getExp() + (tcost/2); //유저의 원래 경험치 + 이번 게임에서 획득한 금액의 절반
                user.updateExp(tmp);
                break;
            case 3:
                tmp = (long) (user.getExp() - (user.getExp() * 0.1));
                user.updateExp(tmp);
                break;
            case 4:
                tmp = (long) (user.getExp() - (user.getExp() * 0.2));
                user.updateExp(tmp);
                break;
        }
    }

    public ParticipantInfo mapToParticipantInfo(GameUser gu){
        Optional<User> user = userRepository.findById(gu.getUserId());
        if(user.isEmpty()) throw new CustomBadRequestException(ErrorType.NOT_FOUND_USER);
        return ParticipantInfo.builder()
                .userId(gu.getUserId())
                .userNick(user.get().getNickName())
                .totalCost(gu.getTotalCost())
                .point(gu.getPoint())
                .build();
    }
}
