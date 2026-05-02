package bookcircle.repo;

import bookcircle.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRoom_IdOrderByCreatedAtAsc(Long roomId);
    void deleteByRoom_Id(Long roomId);
}
