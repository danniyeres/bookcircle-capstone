package bookcircle.dto;

import java.time.Instant;

public record RoomStatsResponse(
        Long roomId,
        long commentsCount,
        long progressUpdatesCount,
        Instant lastEventAt
) {}