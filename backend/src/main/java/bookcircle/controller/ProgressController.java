package bookcircle.controller;

import bookcircle.dto.ProgressDtos;
import bookcircle.service.ProgressService;
import bookcircle.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;


    @PostMapping
    public ProgressDtos.ProgressResponse update(@Valid @RequestBody ProgressDtos.UpdateProgressRequest req) {
        return progressService.updateProgress(AuthUtil.principal().userId(), req);
    }
}
