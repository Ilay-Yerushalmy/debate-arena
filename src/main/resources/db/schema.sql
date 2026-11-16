-- Debate Arena - relational schema (SQLite)
-- Idempotent: executed on every application start.
-- NOTE: statements end with a double semicolon because trigger bodies contain single ones.

CREATE TABLE IF NOT EXISTS participants (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    created_at  TEXT    NOT NULL
);;

CREATE TABLE IF NOT EXISTS topics (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    title        TEXT    NOT NULL UNIQUE,
    description  TEXT,
    created_at   TEXT    NOT NULL
);;

-- A claim belongs to a topic and can be debated in many sessions.
CREATE TABLE IF NOT EXISTS claims (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id    INTEGER NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    text        TEXT    NOT NULL,
    created_at  TEXT    NOT NULL,
    UNIQUE (topic_id, text)
);;

CREATE TABLE IF NOT EXISTS debate_sessions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id    INTEGER NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    title       TEXT    NOT NULL,
    created_at  TEXT    NOT NULL
);;

-- An argument is made by a participant, in a session, for or against a claim.
CREATE TABLE IF NOT EXISTS arguments (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id      INTEGER NOT NULL REFERENCES debate_sessions(id) ON DELETE CASCADE,
    claim_id        INTEGER NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    participant_id  INTEGER NOT NULL REFERENCES participants(id),
    stance          TEXT    NOT NULL CHECK (stance IN ('PRO', 'CON')),
    content         TEXT    NOT NULL,
    has_example     INTEGER NOT NULL DEFAULT 0 CHECK (has_example IN (0, 1)),
    has_statistic   INTEGER NOT NULL DEFAULT 0 CHECK (has_statistic IN (0, 1)),
    has_source      INTEGER NOT NULL DEFAULT 0 CHECK (has_source IN (0, 1)),
    quality_score   INTEGER NOT NULL CHECK (quality_score BETWEEN 0 AND 100),
    created_at      TEXT    NOT NULL
);;

-- Peer ratings: every participant can rate another participant's argument once.
CREATE TABLE IF NOT EXISTS ratings (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    argument_id  INTEGER NOT NULL REFERENCES arguments(id) ON DELETE CASCADE,
    rater_id     INTEGER NOT NULL REFERENCES participants(id),
    score        INTEGER NOT NULL CHECK (score BETWEEN 1 AND 5),
    created_at   TEXT    NOT NULL,
    UNIQUE (argument_id, rater_id)
);;

CREATE INDEX IF NOT EXISTS idx_claims_topic          ON claims(topic_id);;
CREATE INDEX IF NOT EXISTS idx_sessions_topic        ON debate_sessions(topic_id);;
CREATE INDEX IF NOT EXISTS idx_arguments_session     ON arguments(session_id);;
CREATE INDEX IF NOT EXISTS idx_arguments_claim       ON arguments(claim_id);;
CREATE INDEX IF NOT EXISTS idx_arguments_participant ON arguments(participant_id);;
CREATE INDEX IF NOT EXISTS idx_ratings_argument      ON ratings(argument_id);;

-- Data-integrity rule enforced in the database itself (the service checks it too).
CREATE TRIGGER IF NOT EXISTS trg_ratings_no_self_rating
BEFORE INSERT ON ratings
WHEN NEW.rater_id = (SELECT participant_id FROM arguments WHERE id = NEW.argument_id)
BEGIN
    SELECT RAISE(ABORT, 'Participants cannot rate their own arguments');
END;;

-- Average peer rating per argument (NULL when nobody rated yet).
CREATE VIEW IF NOT EXISTS argument_scores AS
SELECT a.id             AS argument_id,
       AVG(r.score)     AS avg_rating,
       COUNT(r.id)      AS rating_count
  FROM arguments a
  LEFT JOIN ratings r ON r.argument_id = a.id
 GROUP BY a.id;;
