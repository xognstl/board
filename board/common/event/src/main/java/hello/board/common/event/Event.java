package hello.board.common.event;

import hello.board.common.dataserializer.DataSerializer;
import lombok.Getter;

@Getter
public class Event <T extends EventPayload> {    // event 통신을 위한 class

    private Long eventId;   // 이벤트에 대하여 고유한 아이디를 가져서 이벤트를 식별할 수 있도록 한다.
    private EventType type;
    private T payload; // 어떤 데이터를 가지고 있는지

    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    // 이벤트를 kafka 로 전달할 때 json 으로 변환하고 다시 역직렬화
    // event 클래스를 json 문자열로 변경해주는 메서드
    public String toJson() {
        return DataSerializer.serialize(this);
    }

    // json 을 받아서 이벤트로 반환해주는 메서드
    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if(eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    // type 에 따라서 payload 가 어떤 클래스 타입인지 다다르다. 그걸 알기위해 처음에 일단 스트링이랑 오브젝트 타입으로 받아서 처리
    @Getter
    private static class EventRaw{
        private Long eventId;
        private String type;
        private Object payload;
    }
}
