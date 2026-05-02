package bookcircle.controller;

import bookcircle.dto.CommentDtos;
import bookcircle.service.CommentService;
import bookcircle.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentDtos.CommentResponse create(@Valid @RequestBody CommentDtos.CreateCommentRequest req) {
        return commentService.create(AuthUtil.principal().userId(), req);
    }

    @GetMapping
    public List<CommentDtos.CommentResponse> visible(@RequestParam Long roomId) {
        return commentService.getVisibleComments(AuthUtil.principal().userId(), roomId);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public void delete(@PathVariable Long commentId) {
        commentService.deleteComment(AuthUtil.principal().userId(), commentId);
    }
}
