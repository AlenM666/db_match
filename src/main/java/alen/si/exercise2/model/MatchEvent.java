package alen.si.exercise2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "match_events")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int matchId;
    private int marketId;
    private int outcomeId;
    private String specifiers;
    private Instant dateInsert;

    public MatchEvent() {
        this.dateInsert = Instant.now(); // Automatically set timestamp
    }

    public MatchEvent(int matchId, int marketId, int outcomeId, String specifiers) {
        this.matchId = matchId;
        this.marketId = marketId;
        this.outcomeId = outcomeId;
        this.specifiers = specifiers;
        this.dateInsert = Instant.now();
    }

}
