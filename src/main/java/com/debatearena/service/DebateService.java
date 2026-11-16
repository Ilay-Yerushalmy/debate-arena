package com.debatearena.service;

import com.debatearena.analysis.ArgumentQualityAssessor;
import com.debatearena.analysis.QualityReport;
import com.debatearena.analysis.ScoreCalculator;
import com.debatearena.analysis.ScoredArgument;
import com.debatearena.domain.Argument;
import com.debatearena.domain.Claim;
import com.debatearena.domain.DebateSession;
import com.debatearena.domain.Participant;
import com.debatearena.domain.Rating;
import com.debatearena.domain.Topic;
import com.debatearena.exception.BusinessRuleException;
import com.debatearena.exception.NotFoundException;
import com.debatearena.repository.ArgumentRepository;
import com.debatearena.repository.ClaimRepository;
import com.debatearena.repository.DebateSessionRepository;
import com.debatearena.repository.ParticipantRepository;
import com.debatearena.repository.RatingRepository;
import com.debatearena.repository.ScoredArgumentQueries;
import com.debatearena.repository.TopicRepository;
import com.debatearena.web.dto.Requests;
import com.debatearena.web.dto.Responses.ArgumentDto;
import com.debatearena.web.dto.Responses.ClaimDto;
import com.debatearena.web.dto.Responses.ParticipantDto;
import com.debatearena.web.dto.Responses.SessionDetailDto;
import com.debatearena.web.dto.Responses.SessionDto;
import com.debatearena.web.dto.Responses.TopicDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Managing debates: participants, topics, claims, sessions, arguments and ratings. */
@Service
@Transactional(readOnly = true)
public class DebateService {

    private final ParticipantRepository participants;
    private final TopicRepository topics;
    private final ClaimRepository claims;
    private final DebateSessionRepository sessions;
    private final ArgumentRepository arguments;
    private final RatingRepository ratings;
    private final ScoredArgumentQueries scoredQueries;
    private final ArgumentQualityAssessor assessor;

    public DebateService(ParticipantRepository participants, TopicRepository topics, ClaimRepository claims,
                         DebateSessionRepository sessions, ArgumentRepository arguments, RatingRepository ratings,
                         ScoredArgumentQueries scoredQueries, ArgumentQualityAssessor assessor) {
        this.participants = participants;
        this.topics = topics;
        this.claims = claims;
        this.sessions = sessions;
        this.arguments = arguments;
        this.ratings = ratings;
        this.scoredQueries = scoredQueries;
        this.assessor = assessor;
    }

    // ---------------------------------------------------------------- participants

    public List<ParticipantDto> listParticipants() {
        return participants.findAllByOrderByNameAsc().stream().map(DebateService::toDto).toList();
    }

    @Transactional
    public ParticipantDto createParticipant(Requests.CreateParticipant req) {
        final String name = req.name().trim();
        if (participants.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException("A participant named '" + name + "' already exists");
        }
        return toDto(participants.save(new Participant(name)));
    }

    // ---------------------------------------------------------------- topics & claims

    public List<TopicDto> listTopics() {
        return topics.findAllByOrderByIdAsc().stream().map(this::toDto).toList();
    }

    public TopicDto getTopic(Long id) {
        return toDto(findTopic(id));
    }

    @Transactional
    public TopicDto createTopic(Requests.CreateTopic req) {
        final String title = req.title().trim();
        if (topics.existsByTitleIgnoreCase(title)) {
            throw new BusinessRuleException("A topic titled '" + title + "' already exists");
        }
        return toDto(topics.save(new Topic(title, req.description())));
    }

    @Transactional
    public ClaimDto addClaim(Long topicId, Requests.CreateClaim req) {
        final Topic topic = findTopic(topicId);
        return toDto(claims.save(new Claim(topic, req.text().trim())));
    }

    // ---------------------------------------------------------------- sessions

    public List<SessionDto> listSessions(Long topicId) {
        final List<DebateSession> list = topicId == null
                ? sessions.findAllByOrderByIdDesc()
                : sessions.findByTopicIdOrderByIdDesc(topicId);
        return list.stream().map(DebateService::toDto).toList();
    }

    @Transactional
    public SessionDto createSession(Requests.CreateSession req) {
        final Topic topic = findTopic(req.topicId());
        return toDto(sessions.save(new DebateSession(topic, req.title().trim())));
    }

    /** A session with all its arguments, their quality and their peer-rating aggregates. */
    public SessionDetailDto getSession(Long id) {
        final DebateSession session = findSession(id);
        final Map<Long, ScoredArgument> scores = scoredQueries.bySession(id).stream().collect(Collectors.toMap(ScoredArgument::argumentId, Function.identity()));

        final List<ArgumentDto> argumentDtos = arguments.findBySessionIdOrderByIdAsc(id).stream()
                .map(a -> toDto(a, scores.get(a.getId())))
                .toList();
        final List<ClaimDto> claimDtos = claims.findByTopicIdOrderByIdAsc(session.getTopic().getId()).stream()
                .map(DebateService::toDto).toList();
        return new SessionDetailDto(toDto(session), claimDtos, argumentDtos);
    }

    // ---------------------------------------------------------------- arguments & ratings

    public QualityReport assess(String text) {
        return assessor.assess(text);
    }

    @Transactional
    public ArgumentDto addArgument(Requests.CreateArgument req) {
        final DebateSession session = findSession(req.sessionId());
        final Claim claim = claims.findById(req.claimId())
                .orElseThrow(() -> new NotFoundException("Claim", req.claimId()));
        final Participant author = findParticipant(req.participantId());

        if (!claim.getTopic().getId().equals(session.getTopic().getId())) {
            throw new BusinessRuleException("The claim does not belong to this session's topic");
        }

        final String content = req.content().trim();
        final QualityReport quality = assessor.assess(content);
        final Argument saved = arguments.save(new Argument(session, claim, author, req.stance(), content, quality));
        return toDto(saved, null);
    }

    @Transactional
    public void rateArgument(Long argumentId, Requests.RateArgument req) {
        final Argument argument = arguments.findById(argumentId)
                .orElseThrow(() -> new NotFoundException("Argument", argumentId));
        final Participant rater = findParticipant(req.raterId());

        if (argument.getParticipant().getId().equals(rater.getId())) {
            throw new BusinessRuleException("Participants cannot rate their own arguments");
        }
        if (ratings.existsByArgumentIdAndRaterId(argumentId, rater.getId())) {
            throw new BusinessRuleException(rater.getName() + " already rated this argument");
        }
        ratings.save(new Rating(argument, rater, req.score()));
    }

    // ---------------------------------------------------------------- lookups

    // Public (not package-private) so calls through the Spring transactional proxy work.
    public Topic findTopic(Long id) {
        return topics.findById(id).orElseThrow(() -> new NotFoundException("Topic", id));
    }

    public DebateSession findSession(Long id) {
        return sessions.findById(id).orElseThrow(() -> new NotFoundException("Session", id));
    }

    public Participant findParticipant(Long id) {
        return participants.findById(id).orElseThrow(() -> new NotFoundException("Participant", id));
    }

    // ---------------------------------------------------------------- mapping

    private static ParticipantDto toDto(Participant p) {
        return new ParticipantDto(p.getId(), p.getName());
    }

    private static ClaimDto toDto(Claim c) {
        return new ClaimDto(c.getId(), c.getTopic().getId(), c.getText());
    }

    private TopicDto toDto(Topic t) {
        final List<ClaimDto> claimDtos = claims.findByTopicIdOrderByIdAsc(t.getId()).stream()
                .map(DebateService::toDto).toList();
        return new TopicDto(t.getId(), t.getTitle(), t.getDescription(), claimDtos);
    }

    private static SessionDto toDto(DebateSession s) {
        return new SessionDto(s.getId(), s.getTopic().getId(), s.getTopic().getTitle(), s.getTitle(), s.getCreatedAt());
    }

    private static ArgumentDto toDto(Argument a, ScoredArgument score) {
        final Double avgRating = score == null ? null : score.avgRating();
        final int ratingCount = score == null ? 0 : score.ratingCount();
        final double combined = score == null
                ? ScoreCalculator.combinedScore(a.getQualityScore(), null)
                : score.combinedScore();
        return new ArgumentDto(a.getId(), a.getSession().getId(), a.getClaim().getId(), a.getClaim().getText(),
                a.getParticipant().getId(), a.getParticipant().getName(), a.getStance(), a.getContent(),
                a.getQualityScore(), a.hasExample(), a.hasStatistic(), a.hasSource(), avgRating, ratingCount,
                Math.round(combined * 100.0) / 100.0);
    }
}
