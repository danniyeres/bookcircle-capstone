package bookcircle.controller;

import bookcircle.dto.UserDtos;
import bookcircle.service.UserService;
import bookcircle.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDtos.UserProfileResponse me() {
        return userService.getMe(AuthUtil.principal().userId());
    }

    @PatchMapping("/me")
    public UserDtos.UserProfileResponse updateMe(@Valid @RequestBody UserDtos.UpdateMeRequest req) {
        return userService.updateMe(AuthUtil.principal().userId(), req);
    }
}
