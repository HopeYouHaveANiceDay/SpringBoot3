package Java.SpringBoot3.event;

import Java.SpringBoot3.enumeration.EventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
//搭配 ApplicationEventPublisher 發佈，@EventListener 監聽，就能用事件驅動的方式處理邏輯。
//publish email, event type
public class NewUserEvent extends ApplicationEvent {
    private EventType type; //→ 事件類型（例如 LOGIN_ATTEMPT、PROFILE_UPDATE）。
    private String email;//→ 事件相關的使用者 email。

    public NewUserEvent(String email, EventType type) {
        super(email);// → 呼叫父類別 ApplicationEvent 的建構子，通常需要一個「事件來源 (source)」物件。這裡用 email 當作來源。
        this.type = type;//→ 初始化事件的屬性。
        this.email = email;//→ 初始化事件的屬性。
    }
}
/*
這個 NewUserEvent 類別是你在 Spring 框架裡自訂的一個 應用事件 (ApplicationEvent)。我來逐步解釋它的作用：
1. extends ApplicationEvent
     ApplicationEvent 是 Spring 提供的一個基底類別，用來建立事件物件。
     當你繼承它，就可以在 Spring 的事件機制裡使用 ApplicationEventPublisher 發佈事件，並由 @EventListener 或 ApplicationListener 來接收事件。
     好處是：你可以在系統裡用「事件驅動」的方式解耦邏輯，例如：使用者註冊後自動觸發某些操作（寄信、記錄 log、建立預設設定）。
     搭配 ApplicationEventPublisher 發佈，@EventListener 監聽，就能用事件驅動的方式處理邏輯。
 */
