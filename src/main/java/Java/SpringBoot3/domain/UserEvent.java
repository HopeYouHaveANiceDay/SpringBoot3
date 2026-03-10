package Java.SpringBoot3.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

/*
為什麼放在 domain 資料夾？
    Domain (領域模型)：在 DDD（Domain-Driven Design）或一般分層架構裡，domain 資料夾通常用來放置 核心業務模型。
    UserEvent 就是系統裡的一個核心業務物件，對應到資料庫的 UserEvents 表（你提到的 schema.sql）。
    放在 domain 裡可以清楚區分：
        domain → 業務模型（User、UserEvent、Order…）
        service → 業務邏輯（EventService、UserService…）
        repository → 資料存取（UserRepository、EventRepository…）
        controller → API 入口（UserController、EventController…）*/
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)

// all come from UserEvents and Events(type) in schema.sql
public class UserEvent {
    private Long id;
    private String type; //→ in the "Events table" => 事件類型: LOGIN_ATTEMPT, LOGIN_ATTEMPT_SUCCESS
    private String description; //→ in the "Events table" => "You tried to log in", "You tried to log in and you succeeded"
    private String device; //device → 使用者操作的裝置（例如 "Chrome - Apple xxx"）。
    private String ipAddress; //0:0:0:0:0:0:0:1
    private LocalDateTime createdAt;
}
