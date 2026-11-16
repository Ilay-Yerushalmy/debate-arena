package com.debatearena.repository;

import com.debatearena.analysis.ScoredArgument;
import com.debatearena.domain.Stance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Hand-written analytics SQL (plain JDBC via JdbcTemplate).
 *
 * <p>Joins arguments with the {@code argument_scores} view (average peer rating per argument)
 * and returns flat {@link ScoredArgument} rows for {@code ScoreCalculator}.
 * JdbcTemplate joins the surrounding JPA transaction, so it reuses the same connection.
 */
@Repository
public class ScoredArgumentQueries {

    private static final String SELECT = """
            SELECT a.id, a.session_id, s.title, a.claim_id,
                   a.participant_id, p.name, a.stance, a.quality_score,
                   sc.avg_rating, sc.rating_count
              FROM arguments a
              JOIN debate_sessions s  ON s.id = a.session_id
              JOIN participants p     ON p.id = a.participant_id
              JOIN argument_scores sc ON sc.argument_id = a.id
            """;

    private static final RowMapper<ScoredArgument> MAPPER = (rs, rowNum) -> new ScoredArgument(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getLong(4),
            rs.getLong(5),
            rs.getString(6),
            Stance.valueOf(rs.getString(7)),
            rs.getInt(8),
            nullableDouble(rs, 9),
            rs.getInt(10));

    private final JdbcTemplate jdbc;

    public ScoredArgumentQueries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ScoredArgument> byClaim(long claimId) {
        return jdbc.query(SELECT + " WHERE a.claim_id = ? ORDER BY a.session_id, a.id", MAPPER, claimId);
    }

    public List<ScoredArgument> bySession(long sessionId) {
        return jdbc.query(SELECT + " WHERE a.session_id = ? ORDER BY a.id", MAPPER, sessionId);
    }

    public List<ScoredArgument> byParticipant(long participantId) {
        return jdbc.query(SELECT + " WHERE a.participant_id = ? ORDER BY a.id", MAPPER, participantId);
    }

    public List<ScoredArgument> all() {
        return jdbc.query(SELECT + " ORDER BY a.id", MAPPER);
    }

    private static Double nullableDouble(ResultSet rs, int column) throws SQLException {
        final double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
