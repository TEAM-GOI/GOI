package ssafy.GeniusOfInvestment.game.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ssafy.GeniusOfInvestment._common.entity.User;
import ssafy.GeniusOfInvestment._common.redis.MyTradingInfo;
import ssafy.GeniusOfInvestment._common.response.SuccessResponse;
import ssafy.GeniusOfInvestment._common.response.SuccessType;
import ssafy.GeniusOfInvestment.game.dto.BuyInfoResponse;
import ssafy.GeniusOfInvestment.game.dto.BuySellRequest;
import ssafy.GeniusOfInvestment.game.dto.ChartResponse;
import ssafy.GeniusOfInvestment.game.dto.MyItemInfo;
import ssafy.GeniusOfInvestment.game.service.StockService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    //나의 주식 거래 내역들
    @GetMapping("")
    public SuccessResponse<MyTradingInfo> getTradingInfo(@AuthenticationPrincipal User user){
        MyTradingInfo tinfo = stockService.getTradingInfo(user);
        return SuccessResponse.of(SuccessType.TRADING_INFO_SUCCESSFULLY, tinfo);
    }

    @GetMapping("/{item}/{rId}")
    public SuccessResponse<MyItemInfo> getInfoByItem(@AuthenticationPrincipal User user, @PathVariable("item") String item, @PathVariable("rId") Long rId){
        MyItemInfo itemInfo = stockService.getInfoByItem(user, item, rId);
        return SuccessResponse.of(SuccessType.ITEM_INFO_SUCCESSFULLY, itemInfo);
    }

    //정보를 구매한다.
    @GetMapping("/info")
    public SuccessResponse<BuyInfoResponse> buyInformation(@AuthenticationPrincipal User user, @RequestParam("id") Long grId,
                                          @RequestParam("item") String item,
                                          @RequestParam("level") int level){
        BuyInfoResponse buyInfo = stockService.buyInformation(user, grId, item, level);
        return SuccessResponse.of(SuccessType.BUY_INFO_SUCCESSFULLY, buyInfo);
    }

    //내가 구매한 정보 목록들
    @GetMapping("/infolist")
    public SuccessResponse<List<BuyInfoResponse>> getMyOwnInfoList(@AuthenticationPrincipal User user, @RequestParam("id") Long grId){
        List<BuyInfoResponse> buyInfoList = stockService.getMyOwnInfoList(user, grId);
        return SuccessResponse.of(SuccessType.BUY_INFO_LIST_SUCCESSFULLY, buyInfoList);
    }

    //종목별 차트 정보
    @GetMapping("/chart")
    public SuccessResponse<List<ChartResponse>> getItemChart(@RequestParam("id") Long grId, @RequestParam("item") String item){
        List<ChartResponse> chartResponses = stockService.getItemChart(grId, item);
        return SuccessResponse.of(SuccessType.CHART_INFO_SUCCESSFULLY, chartResponses);
    }

    //주식 매수 기능
    @PutMapping("/buy")
    public SuccessResponse<MyTradingInfo> buyStockItem(@AuthenticationPrincipal User user, @RequestBody BuySellRequest buyInfo){
        MyTradingInfo tinfo = stockService.buyStockItem(user, buyInfo.grId(), buyInfo.item(), buyInfo.share());
        return SuccessResponse.of(SuccessType.BUY_STOCK_SUCCESSFULLY, tinfo);
    }

    //주식 매도 기능
    @PutMapping("/sell")
    public SuccessResponse<MyTradingInfo> sellStockItem(@AuthenticationPrincipal User user, @RequestBody BuySellRequest buyInfo){
        MyTradingInfo tinfo = stockService.sellStockItem(user, buyInfo.grId(), buyInfo.item(), buyInfo.share());
        return SuccessResponse.of(SuccessType.SELL_STOCK_SUCCESSFULLY, tinfo);
    }
}
