package bookcircle.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminDtos {

    public record UpdateUserRoleRequest(
            @NotBlank String role
    ) {}

    public record UserRoleResponse(
            Long userId,
            String email,
            String role
    ) {}
}
