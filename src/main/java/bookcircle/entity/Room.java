package bookcircle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "h3_index", nullable = false)
    private String h3Index;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User owner;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();


}
