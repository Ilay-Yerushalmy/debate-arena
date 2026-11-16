package com.debatearena.analysis;

import java.util.List;

/**
 * How well a claim holds up across all debate sessions.
 *
 * @param strength 0-100: 100 = only strong PRO arguments, 0 = only strong CON arguments, 50 = balanced
 * @param verdict  STRONG / CONTESTED / WEAK / NOT_DEBATED
 */
public record ClaimStrength(double strength, String verdict, int proCount, int conCount,
                            List<SessionBreakdown> sessions) {

    public record SessionBreakdown(long sessionId, String sessionTitle, double strength, int argumentCount) {
    }
}
