package com.debatearena.web;

import com.debatearena.analysis.QualityReport;
import com.debatearena.service.DebateService;
import com.debatearena.web.dto.Requests;
import com.debatearena.web.dto.Responses.ArgumentDto;
import com.debatearena.web.dto.Responses.ClaimDto;
import com.debatearena.web.dto.Responses.ParticipantDto;
import com.debatearena.web.dto.Responses.SessionDetailDto;
import com.debatearena.web.dto.Responses.SessionDto;
import com.debatearena.web.dto.Responses.TopicDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API for managing debates. */
@RestController
@RequestMapping("/api")
public class DebateController {

    private final DebateService service;

    public DebateController(DebateService service) {
        this.service = service;
    }

    // ---- participants
    @GetMapping("/participants")
    public List<ParticipantDto> participants() {
        return service.listParticipants();
    }

    @PostMapping("/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantDto createParticipant(@Valid @RequestBody Requests.CreateParticipant req) {
        return service.createParticipant(req);
    }

    // ---- topics & claims
    @GetMapping("/topics")
    public List<TopicDto> topics() {
        return service.listTopics();
    }

    @GetMapping("/topics/{id}")
    public TopicDto topic(@PathVariable Long id) {
        return service.getTopic(id);
    }

    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicDto createTopic(@Valid @RequestBody Requests.CreateTopic req) {
        return service.createTopic(req);
    }

    @PostMapping("/topics/{id}/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimDto addClaim(@PathVariable Long id, @Valid @RequestBody Requests.CreateClaim req) {
        return service.addClaim(id, req);
    }

    // ---- sessions
    @GetMapping("/sessions")
    public List<SessionDto> sessions(@RequestParam(required = false) Long topicId) {
        return service.listSessions(topicId);
    }

    @GetMapping("/sessions/{id}")
    public SessionDetailDto session(@PathVariable Long id) {
        return service.getSession(id);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto createSession(@Valid @RequestBody Requests.CreateSession req) {
        return service.createSession(req);
    }

    // ---- arguments & ratings
    @PostMapping("/arguments")
    @ResponseStatus(HttpStatus.CREATED)
    public ArgumentDto addArgument(@Valid @RequestBody Requests.CreateArgument req) {
        return service.addArgument(req);
    }

    /** Live preview of the technical assessment - nothing is saved. */
    @PostMapping("/arguments/assess")
    public QualityReport assess(@Valid @RequestBody Requests.AssessText req) {
        return service.assess(req.text());
    }

    @PostMapping("/arguments/{id}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public void rate(@PathVariable Long id, @Valid @RequestBody Requests.RateArgument req) {
        service.rateArgument(id, req);
    }
}
