package Java.SpringBoot3.listener;

import Java.SpringBoot3.event.NewUserEvent;
import Java.SpringBoot3.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static Java.SpringBoot3.utils.RequestUtils.getDevice;
import static Java.SpringBoot3.utils.RequestUtils.getIpAddress;


/*
✅ 總結 :
    NewUserEventListener 是一個 Spring Bean，用來監聽並處理 NewUserEvent。
    它利用 @EventListener 自動接收事件，並透過 eventService 把事件紀錄存到資料庫。
    @Component、@RequiredArgsConstructor、@Slf4j 分別讓它能被 Spring 管理、支援建構子注入、方便記錄日誌。
 */

@Component //把這個類別註冊成 Spring Bean，讓 Spring 容器能自動管理它。這樣 Spring 才能自動偵測並呼叫它的事件監聽方法。
@RequiredArgsConstructor // used for final. 自動生成一個建構子，包含所有 final 欄位。在這裡，eventService 和 request 都是 final，所以 Spring 可以用建構子注入它們。
//@Slf4j// used for log.info()→ 自動生成一個 Logger (log)，方便你用 log.info(...)、log.error(...) 記錄日誌。
//這個 NewUserEventListener 類別是一個 Spring 事件監聽器，它的作用是接收並處理你在系統裡發佈的 NewUserEvent。
public class NewUserEventListener {
    private final EventService eventService; //eventService → 事件服務，用來新增事件紀錄到資料庫。
    private final HttpServletRequest request; //request → HTTP 請求物件，可以用來取得使用者的 IP、User-Agent 等資訊。
/*
@EventListener
→ 告訴 Spring 這個方法要監聽 NewUserEvent。
→ 當 NewUserEvent 被發佈時，Spring 會自動呼叫這個方法。 */
    @EventListener
    public void onNewUserEvent(NewUserEvent event) {
        //log.info("NewUserEvent is fired");//→ 記錄事件被觸發。
        /*
        eventService.addUserEvent(...) → 把事件存到資料庫。
            使用 event.getEmail() → 事件的 email。
            使用 event.getType() → 事件的類型 (enum)。
            "Device" 和 "IP address" → 目前是硬編碼，實務上可以從 request 取得真實的裝置資訊和 IP。
         */
        eventService.addUserEvent(event.getEmail(), event.getType(), getDevice(request), getIpAddress(request));
    }
}
