package bookcircle.service;

import bookcircle.dto.UserDtos;
import bookcircle.entity.User;
import bookcircle.exception.ApiException;
import bookcircle.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UserDtos.UserProfileResponse getMe(Long actorUserId) {
        User user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserDtos.UserProfileResponse updateMe(Long actorUserId, UserDtos.UpdateMeRequest req) {
        User user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<String> changedFields = new ArrayList<>();

        if (req.email() != null) {
            String normalizedEmail = req.email().trim().toLowerCase();
            if (normalizedEmail.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Email cannot be blank");
            }
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
            }
            if (!normalizedEmail.equals(user.getEmail())) {
                user.setEmail(normalizedEmail);
                changedFields.add("email");
            }
        }

        if (req.nickname() != null) {
            String normalizedNickname = req.nickname().trim();
            if (normalizedNickname.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Nickname cannot be blank");
            }
            boolean nicknameChanged = !equalsIgnoreCase(normalizedNickname, user.getNickname());
            if (nicknameChanged && userRepository.existsByNicknameIgnoreCase(normalizedNickname)) {
                throw new ApiException(HttpStatus.CONFLICT, "Nickname already taken");
            }
            if (nicknameChanged) {
                user.setNickname(normalizedNickname);
                changedFields.add("nickname");
            }
        }

        if (req.phoneNumber() != null) {
            String normalizedPhoneNumber = req.phoneNumber().trim();
            if (normalizedPhoneNumber.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Phone number cannot be blank");
            }
            boolean phoneNumberChanged = !normalizedPhoneNumber.equals(user.getPhoneNumber());
            if (phoneNumberChanged && userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
                throw new ApiException(HttpStatus.CONFLICT, "Phone number already taken");
            }
            if (phoneNumberChanged) {
                user.setPhoneNumber(normalizedPhoneNumber);
                changedFields.add("phoneNumber");
            }
        }

        boolean hasCurrentPassword = req.currentPassword() != null && !req.currentPassword().isBlank();
        boolean hasNewPassword = req.newPassword() != null && !req.newPassword().isBlank();
        if (hasCurrentPassword != hasNewPassword) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Both currentPassword and newPassword are required");
        }
        if (hasNewPassword) {
            if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Current password is invalid");
            }
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
            changedFields.add("password");
        }

        if (!changedFields.isEmpty()) {
            userRepository.save(user);
            auditService.log(
                    actorUserId,
                    "USER_PROFILE_UPDATED",
                    "User",
                    user.getId(),
                    "fields=" + String.join(",", changedFields)
            );
        }

        return toResponse(user);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equalsIgnoreCase(b);
    }

    private static UserDtos.UserProfileResponse toResponse(User user) {
        return new UserDtos.UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
