-- Demo seed data (idempotent thanks to INSERT OR IGNORE + fixed ids).
-- Statements use a double-semicolon separator (see application.properties).
-- Quality fields match what ArgumentQualityAssessor computes for each text.

INSERT OR IGNORE INTO participants (id, name, created_at) VALUES
    (1, 'Ilay', '2024-03-01T10:00:00'),
    (2, 'Dana', '2024-03-01T10:00:00'),
    (3, 'Noam', '2024-03-01T10:00:00'),
    (4, 'Maya', '2024-03-01T10:00:00');;

INSERT OR IGNORE INTO topics (id, title, description, created_at) VALUES
    (1, 'Remote work', 'Should companies move to remote-first work?', '2024-03-01T10:00:00'),
    (2, 'AI in education', 'Should students be allowed to use AI assistants?', '2024-03-01T10:00:00');;

INSERT OR IGNORE INTO claims (id, topic_id, text, created_at) VALUES
    (1, 1, 'Remote work increases productivity', '2024-03-01T10:00:00'),
    (2, 1, 'Remote work harms team culture', '2024-03-01T10:00:00'),
    (3, 2, 'AI assistants improve learning outcomes', '2024-03-01T10:00:00'),
    (4, 2, 'AI use in homework should be banned', '2024-03-01T10:00:00');;

INSERT OR IGNORE INTO debate_sessions (id, topic_id, title, created_at) VALUES
    (1, 1, 'Remote work - round 1', '2024-03-05T18:00:00'),
    (2, 1, 'Remote work - round 2', '2024-03-12T18:00:00'),
    (3, 2, 'AI in education - opening debate', '2024-03-19T18:00:00');;

INSERT OR IGNORE INTO arguments
    (id, session_id, claim_id, participant_id, stance, content, has_example, has_statistic, has_source, quality_score, created_at)
VALUES
    (1, 1, 1, 1, 'PRO', 'According to a Stanford study, remote employees were 13% more productive, for example they took fewer sick days and shorter breaks.', 1, 1, 1, 100, '2024-03-05T18:10:00'),
    (2, 1, 1, 2, 'CON', 'Many people get distracted at home, for instance by family members and chores, so output drops.', 1, 0, 0, 65, '2024-03-05T18:15:00'),
    (3, 1, 2, 3, 'PRO', 'Teams that never meet in person struggle to build trust and informal relationships between colleagues.', 0, 0, 0, 45, '2024-03-05T18:20:00'),
    (4, 2, 1, 4, 'PRO', 'Remote work saves around 40% of commute time which people reinvest in deep, focused work every day.', 0, 1, 0, 30, '2024-03-12T18:05:00'),
    (5, 2, 2, 1, 'CON', 'Culture depends on rituals, not offices. For example GitLab has a strong culture with zero offices, as described in their published handbook.', 1, 0, 1, 85, '2024-03-12T18:12:00'),
    (6, 2, 1, 2, 'CON', 'Microsoft research published in Nature Human Behaviour found remote work made collaboration more siloed, which reduces productivity on complex projects.', 0, 0, 1, 65, '2024-03-12T18:20:00'),
    (7, 3, 3, 3, 'PRO', 'Students get instant feedback, for example a personal tutor that explains a calculus step at 2am when no teacher is available.', 1, 0, 0, 65, '2024-03-19T18:10:00'),
    (8, 3, 4, 4, 'PRO', 'If AI writes the homework, the student practices nothing and learning stops.', 0, 0, 0, 30, '2024-03-19T18:18:00');;

INSERT OR IGNORE INTO ratings (id, argument_id, rater_id, score, created_at) VALUES
    (1,  1, 2, 4, '2024-03-05T19:00:00'),
    (2,  1, 3, 5, '2024-03-05T19:00:00'),
    (3,  1, 4, 5, '2024-03-05T19:00:00'),
    (4,  2, 1, 3, '2024-03-05T19:00:00'),
    (5,  2, 3, 3, '2024-03-05T19:00:00'),
    (6,  3, 1, 2, '2024-03-05T19:00:00'),
    (7,  3, 4, 3, '2024-03-05T19:00:00'),
    (8,  4, 1, 3, '2024-03-12T19:00:00'),
    (9,  4, 2, 2, '2024-03-12T19:00:00'),
    (10, 5, 2, 4, '2024-03-12T19:00:00'),
    (11, 5, 3, 5, '2024-03-12T19:00:00'),
    (12, 6, 1, 4, '2024-03-12T19:00:00'),
    (13, 6, 4, 4, '2024-03-12T19:00:00'),
    (14, 7, 1, 4, '2024-03-19T19:00:00'),
    (15, 7, 4, 3, '2024-03-19T19:00:00'),
    (16, 8, 2, 3, '2024-03-19T19:00:00'),
    (17, 8, 3, 2, '2024-03-19T19:00:00');;
