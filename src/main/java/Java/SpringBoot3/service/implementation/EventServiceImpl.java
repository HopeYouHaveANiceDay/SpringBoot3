package Java.SpringBoot3.service.implementation;

import Java.SpringBoot3.domain.UserEvent;
import Java.SpringBoot3.enumeration.EventType;
import Java.SpringBoot3.repository.EventRepository;
import Java.SpringBoot3.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service //...Listener.NewUserEventListener required a bean of type '...service.EventService' that could not be found.
@RequiredArgsConstructor //...Listener.NewUserEventListener required a bean of type '...service.EventService' that could not be found.
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository; //...Listener.NewUserEventListener required a bean of type '...service.EventService' that could not be found.

    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return eventRepository.getEventsByUserId(userId);//...Listener.NewUserEventListener required a bean of type '...service.EventService' that could not be found.
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        eventRepository.addUserEvent(email, eventType, device, ipAddress);//...Listener.NewUserEventListener required a bean of type '...service.EventService' that could not be found.
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
