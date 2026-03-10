package Java.SpringBoot3.service;


import Java.SpringBoot3.domain.UserEvent;
import Java.SpringBoot3.enumeration.EventType;

import java.util.Collection;

// EventService.java => domain<UserEvent>
// EventType => enum


//EventService 是事件服務的介面，定義了取得和新增事件的方法。
// EventService → 提供操作這些事件的功能（新增、查詢）。
//這個 EventService 介面是你 Spring Boot 專案裡的一個 服務層契約，它的作用是定義「事件服務」應該具備哪些功能，但不包含具體的實作。

public interface EventService {

    //getEventsByUserId(Long userId)
    //→ 根據使用者 ID 取得該使用者的所有事件紀錄（例如登入、修改資料、更新圖片等）。
    //return a list of a user event with that user
    //UserEvent 是事件的資料模型，對應到資料庫的 UserEvents 表。
    //放在 domain 資料夾是因為它屬於系統的核心業務模型，這樣分層更清晰，維護性更好。
    Collection<UserEvent> getEventsByUserId(Long userId);

    //→ 新增一筆事件，使用者透過 email 來識別。常見於使用者還沒查到 ID，但有 email 的情況。
    void addUserEvent(String email, EventType eventType, String device, String ipAddress);

    //→ 新增一筆事件，使用者透過 userId 來識別。常見於已經查到使用者 ID 的情況。
    void addUserEvent(Long userId, EventType eventType, String device, String ipAddress);
}
