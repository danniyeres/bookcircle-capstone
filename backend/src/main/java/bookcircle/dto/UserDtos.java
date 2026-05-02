package bookcircle.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UserDtos {

    public record UpdateMeRequest(
            @Email String email,
            @Size(max = 64) String nickname,
            @Pattern(regexp = "^\\+?[0-9]{7,20}$") String phoneNumber,
            String currentPassword,
            @Size(min = 6, max = 128) String newPassword
    ) {}

    public record UserProfileResponse(
            Long userId,
            String email,
            String nickname,
            String phoneNumber,
            String role,
            Instant createdAt
    ) {}
}
