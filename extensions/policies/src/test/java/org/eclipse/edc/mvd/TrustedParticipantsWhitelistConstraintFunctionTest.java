/*
 *  Copyright (c) 2024 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial implementation
 *
 */

package org.eclipse.edc.mvd;

import org.eclipse.edc.identityhub.spi.credentials.model.Credential;
import org.eclipse.edc.identityhub.spi.credentials.model.CredentialSubject;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedParticipantsWhitelistConstraintFunctionTest {

    private static final TrustedParticipantsWhitelistConstraintFunction CONSTRAINT_FUNCTION = new TrustedParticipantsWhitelistConstraintFunction();
    private static final Permission PERMISSION = Permission.Builder.newInstance().build();
    private static final String PARTICIPANT_KEY = "participant";

    @BeforeEach
    void setUp() {
        // Clear whitelist before each test
        TrustedParticipantsWhitelist.getInstance().clear();
    }

    @Test
    void verifyPolicy_validParticipantInTrustedList() {
        String participant = "trustedParticipant";
        TrustedParticipantsWhitelist.getInstance().addTrustedParticipant(participant);
        var claims = toCredentialsMap(PARTICIPANT_KEY, participant);
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.EQ, "true", PERMISSION, policyContext)).isTrue();
    }

    @Test
    void verifyPolicy_invalidParticipantNotInTrustedList() {
        var claims = toCredentialsMap(PARTICIPANT_KEY, "untrustedParticipant");
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.EQ, "true", PERMISSION, policyContext)).isFalse();
    }

    @Test
    void verifyPolicy_NonBooleanRightValue() {
        var claims = toCredentialsMap(PARTICIPANT_KEY, "trustedParticipant");
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.EQ, "nonBooleanValue", PERMISSION, policyContext)).isFalse();
    }

    @Test
    void verifyPolicy_UnsupportedOperator() {
        var claims = toCredentialsMap(PARTICIPANT_KEY, "trustedParticipant");
        var policyContext = toPolicyContext(claims);

        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.GT, "true", PERMISSION, policyContext)).isFalse();
    }

    @Test
    void verifyPolicy_NeqOperatorWithParticipantInTrustedList() {
        String participant = "trustedParticipant";
        TrustedParticipantsWhitelist.getInstance().addTrustedParticipant(participant);
        var claims = toCredentialsMap(PARTICIPANT_KEY, participant);
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.NEQ, "true", PERMISSION, policyContext)).isFalse();
    }

    @Test
    void verifyPolicy_InOperatorWithPartialMatchInTrustedList() {
        String trustedParticipant = "trustedParticipant";
        TrustedParticipantsWhitelist.getInstance().addTrustedParticipant(trustedParticipant);
        var claims = toCredentialsMap(PARTICIPANT_KEY, trustedParticipant);
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.IN, "true", PERMISSION, policyContext)).isTrue();
    }

    @Test
    void verifyPolicy_InvalidClaimFormat() {
        var claims = Map.of(UUID.randomUUID().toString(), (Object) UUID.randomUUID().toString());
        var policyContext = toPolicyContext(claims);
        assertThat(CONSTRAINT_FUNCTION.evaluate(Operator.EQ, "true", PERMISSION, policyContext)).isFalse();
    }

    private PolicyContext toPolicyContext(Map<String, Object> claims) {
        PolicyContextImpl policyContext = new PolicyContextImpl();
        ParticipantAgent participantAgent = new ParticipantAgent(claims, Map.of());
        policyContext.putContextData(ParticipantAgent.class, participantAgent);
        return policyContext;
    }

    private Map<String, Object> toCredentialsMap(String key, Object value) {
        var credentialId = UUID.randomUUID().toString();
        var credential = Credential.Builder.newInstance()
                .id("test")
                .context("test")
                .type("VerifiableCredential")
                .issuer("did:web:" + UUID.randomUUID())
                .issuanceDate(Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS)))
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .id("test")
                        .claim(key, value)
                        .build())
                .build();
        return Map.of(credentialId, credential);
    }
}
