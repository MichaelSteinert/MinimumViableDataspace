/*
 *  Copyright (c) 2022 Microsoft Corporation
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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TrustedParticipantsConstraintFunction implements AtomicConstraintFunction<Permission> {

    private static final Logger LOGGER = Logger.getLogger(TrustedParticipantsConstraintFunction.class.getName());

    private static final String TRUSTED_PARTICIPANTS_KEY = "trusted_participants";

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var claims = context.getParticipantAgent().getClaims();
        var trustedParticipants = extractTrustedParticipantsFromClaims(claims);
        LOGGER.log(Level.INFO, "Trusted Participants:" + trustedParticipants);
        if (!(rightValue instanceof Collection<?> rightValueCollection)) {
            LOGGER.log(Level.WARNING, "rightValue contains non-String elements");
            return false; // rightValue is not a Collection, cannot proceed
        }
        LOGGER.log(Level.INFO, "Right Value Collection:" + rightValueCollection);
        return switch (operator) {
            case EQ -> trustedParticipants.containsAll(rightValueCollection);
            case NEQ -> !trustedParticipants.containsAll(rightValueCollection);
            case IN -> trustedParticipants.stream().anyMatch(rightValueCollection::contains);
            default -> false;
        };
    }

    private List<String> extractTrustedParticipantsFromClaims(Map<String, Object> claims) {
        return claims.values().stream()
                .filter(Credential.class::isInstance)
                .map(credential -> (Credential) credential)
                .map(this::extractTrustedParticipantsFromCredential)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Nullable
    private List<String> extractTrustedParticipantsFromCredential(Credential credential) {
        var claims = credential.getCredentialSubject().getClaims();
        LOGGER.log(Level.INFO, "Received Claims: " + claims);

        var trustedParticipants = claims.get(TRUSTED_PARTICIPANTS_KEY);
        LOGGER.log(Level.INFO, "Received trusted_participants: " + trustedParticipants);

        if (trustedParticipants instanceof List) {
            return (List<String>) trustedParticipants;
        } else {
            LOGGER.log(Level.WARNING, "trusted_participants is not a list");
            return Collections.emptyList();
        }
    }
}
