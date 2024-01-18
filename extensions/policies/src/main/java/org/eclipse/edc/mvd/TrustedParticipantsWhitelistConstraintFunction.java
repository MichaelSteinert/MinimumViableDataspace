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
import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements the AtomicConstraintFunction interface and is used for evaluating
 * permissions based on a whitelist of trusted participants.
 * It checks if the participants involved in a policy context are present in the trusted
 * participants whitelist, applying different operators like EQ, NEQ, and IN.
 */
public class TrustedParticipantsWhitelistConstraintFunction implements AtomicConstraintFunction<Permission> {

    private static final String PARTICIPANT_KEY = "participant";

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var trustedParticipants = TrustedParticipantsWhitelist.getInstance();
        trustedParticipants.addTrustedParticipant("company1");
        var participants = extractParticipants(context.getContextData(ParticipantAgent.class).getClaims());
        boolean rightValueBoolean = rightValue instanceof String && Boolean.parseBoolean((String) rightValue);
        if (rightValueBoolean) {
            var trustedParticipantsSet = new HashSet<>(trustedParticipants.getTrustedParticipants());
            var participantsSet = new HashSet<>(participants);
            return switch (operator) {
                case EQ -> trustedParticipantsSet.equals(participantsSet);
                case NEQ -> !trustedParticipantsSet.equals(participantsSet);
                case IN -> participantsSet.stream().anyMatch(trustedParticipantsSet::contains);
                default -> false;
            };
        }
        return false;
    }

    private List<String> extractParticipants(Map<String, Object> claims) {
        return claims.values().stream()
                .filter(Credential.class::isInstance)
                .map(Credential.class::cast)
                .map(this::getParticipant)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private String getParticipant(Credential credential) {
        var claims = credential.getCredentialSubject().getClaims();
        return Optional.ofNullable(claims.get(PARTICIPANT_KEY))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse(null);
    }
}
