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
import org.eclipse.edc.spi.agent.ParticipantAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * TrustedParticipantsConstraintFunction is used to assess whether
 * the participants involved in a transfer are trusted, based on a
 * predefined set of trusted participants.
 * <p>
 * This class implements the AtomicConstraintFunction interface
 * and provides the logic to validate the trustworthiness of
 * participants against the set policies.
 */
public class TrustedParticipantsConstraintFunction implements AtomicConstraintFunction<Permission> {

    private static final Logger LOGGER = Logger.getLogger(TrustedParticipantsConstraintFunction.class.getName());

    private static final String TRUSTED_PARTICIPANTS_KEY = "trusted_participants";

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var claims = context.getContextData(ParticipantAgent.class).getClaims();
        var trustedParticipantsList = extractTrustedParticipantsFromClaims(claims);
        Set<String> trustedParticipants = new HashSet<>(trustedParticipantsList);
        LOGGER.log(Level.INFO, "Trusted Participants:" + trustedParticipants);
        if (!(rightValue instanceof Collection<?> rightValueCollection)) {
            LOGGER.log(Level.WARNING, "rightValue contains non-String elements");
            return false; // rightValue is not a Collection, cannot proceed
        }
        Set<String> rightValueSet = rightValueCollection.stream()
                .map(obj -> (String) obj)
                .collect(Collectors.toSet());
        LOGGER.log(Level.INFO, "Right Value Collection:" + rightValueSet);
        return switch (operator) {
            case EQ -> trustedParticipants.containsAll(rightValueSet);
            case NEQ -> !trustedParticipants.containsAll(rightValueSet);
            case IN -> rightValueSet.stream().anyMatch(trustedParticipants::contains);
            default -> false;
        };
    }

    private List<String> extractTrustedParticipantsFromClaims(Map<String, Object> claims) {
        return claims.values().stream()
                .filter(Credential.class::isInstance)
                .map(Credential.class::cast)
                .map(this::extractTrustedParticipantsFromCredential)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractTrustedParticipantsFromCredential(Credential credential) {
        var claims = credential.getCredentialSubject().getClaims();
        LOGGER.log(Level.INFO, "Received Claims: " + claims);
        var trustedParticipantsObject = claims.get(TRUSTED_PARTICIPANTS_KEY);
        LOGGER.log(Level.INFO, "Received trusted_participants: " + trustedParticipantsObject);
        if (trustedParticipantsObject instanceof List<?> rawList) {
            List<String> trustedParticipants = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof String) {
                    trustedParticipants.add((String) item);
                } else {
                    LOGGER.log(Level.WARNING, "trusted_participant is not a String: " + item);
                }
            }
            return trustedParticipants;
        } else {
            LOGGER.log(Level.WARNING, "trusted_participants is not a List");
            return Collections.emptyList();
        }
    }
}
