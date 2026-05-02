package bookcircle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ProgressDtos {
    public record UpdateProgressRequest(
            @NotNull Long roomId,
            @Min(1) int chapterNumber
    ) {}

    public record ProgressResponse(
            Long roomId,
            Long userId,
            int chapterNumber
    ) {}
}
