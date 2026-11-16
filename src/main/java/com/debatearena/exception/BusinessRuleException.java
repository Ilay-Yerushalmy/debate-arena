package com.debatearena.exception;

/** A request that is well-formed but breaks a domain rule. Mapped to HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
