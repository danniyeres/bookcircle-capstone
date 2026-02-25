package bookcircle.service;

import bookcircle.events.CommentCreatedEvent;
import bookcircle.events.ProgressUpdatedEvent;
import bookcircle.entity.RoomActivityStats;
import bookcircle.repo.RoomActivityStatsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class StatsEventListener {

    private final RoomActivityStatsRepository statsRepository;

    public StatsEventListener(RoomActivityStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @EventListener
    public void onProgressUpdated(ProgressUpdatedEvent e) {
        RoomActivityStats s = statsRepository.findById(e.roomId()).orElseGet(() -> {
            RoomActivityStats ns = new RoomActivityStats();
            ns.setRoomId(e.roomId());
            return ns;
        });
        s.setProgressUpdatesCount(s.getProgressUpdatesCount() + 1);
        s.setLastEventAt(Instant.now());
        statsRepository.save(s);
    }

    @EventListener
    public void onCommentCreated(CommentCreatedEvent e) {
        RoomActivityStats s = statsRepository.findById(e.roomId()).orElseGet(() -> {
            RoomActivityStats ns = new RoomActivityStats();
            ns.setRoomId(e.roomId());
            return ns;
        });
        s.setCommentsCount(s.getCommentsCount() + 1);
        s.setLastEventAt(Instant.now());
        statsRepository.save(s);
    }
}
