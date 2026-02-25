package bookcircle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CommentDtos {

    public record CreateCommentRequest(
            @NotNull Long roomId,
            @Min(1) int chapterNumber,
            @NotBlank String content
    ) {}

    public record CommentResponse(
            Long id,
            Long roomId,
            Long authorId,
            int chapterNumber,
            String content,
            Instant createdAt
    ) {}
}
