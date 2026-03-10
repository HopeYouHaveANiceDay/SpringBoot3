package Java.SpringBoot3.repository.implementation;


import Java.SpringBoot3.domain.UserEvent;
import Java.SpringBoot3.enumeration.EventType;
import Java.SpringBoot3.repository.EventRepository;
import Java.SpringBoot3.rowmapper.UserEventRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static Java.SpringBoot3.query.EventQuery.INSERT_EVENT_BY_USER_EMAIL_QUERY;
import static Java.SpringBoot3.query.EventQuery.SELECT_EVENTS_BY_USER_ID_QUERY;
import static java.util.Map.*;

//明確分層：Controller → Service → Repository → Database。

@Repository//用途：標記這個類別是 資料存取層 (DAO/Repository)。Spring 會自動偵測並註冊它成為 Spring Bean，讓你可以在 Service 裡注入使用。
@RequiredArgsConstructor//用途：自動生成一個建構子，包含所有 final 欄位或帶有 @NonNull 的欄位。
@Slf4j

public class EventRepositoryImpl implements EventRepository {
    private final NamedParameterJdbcTemplate jdbc; //used for DB

    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, of("id", userId), new UserEventRowMapper());
    }

    @Override //(1)email give us user_id (2)event_id (3)device (4)ip_address come from "UserEvents table". created_at by itself
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        jdbc.update(INSERT_EVENT_BY_USER_EMAIL_QUERY, of("email", email, "type", eventType.toString(), "device", device, "ipAddress", ipAddress));
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
