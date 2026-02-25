package bookcircle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
}
