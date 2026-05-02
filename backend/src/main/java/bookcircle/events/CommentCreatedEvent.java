package bookcircle.events;

public record CommentCreatedEvent(Long roomId, Long userId) {}
