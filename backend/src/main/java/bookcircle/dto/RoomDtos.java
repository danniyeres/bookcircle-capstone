package bookcircle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public class RoomDtos {

    public record CreateRoomRequest(

            @NotBlank
            String name,
            @NotNull
            @Positive
            Long bookId,
            // Either provide h3Index directly OR provide lat/lon.
            String h3Index,
            Double lat,
            Double lon,
            Integer resolution
    ) {}

    public record RoomResponse(
            Long id,
            String name,
            Long bookId,
            String bookTitle,
            String h3Index,
            Long ownerId
    ) {}

    public record RoomMemberProgressResponse(
            Long userId,
            String email,
            String nickname,
            String roomRole,
            int chapterNumber,
            Instant progressUpdatedAt
    ) {}

    public record RoomMembersProgressListResponse(
            Long roomId,
            List<RoomMemberProgressResponse> members
    ) {}
}
