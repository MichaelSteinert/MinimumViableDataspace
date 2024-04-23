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

import org.eclipse.edc.mvd.model.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a whitelist of trusted participants. This class uses the Singleton design pattern
 * to ensure that only one instance of the whitelist is used throughout the application.
 * It provides methods to add, remove, and retrieve trusted participants.
 */
public class TrustedParticipantsWhitelist {

    private static TrustedParticipantsWhitelist instance;
    private final List<Participant> trustedParticipants;

    private TrustedParticipantsWhitelist() {
        this.trustedParticipants = new ArrayList<>();
    }

    /**
     * Retrieves the singleton instance of TrustedParticipantsWhitelist.
     *
     * @return The singleton instance of TrustedParticipantsWhitelist.
     */
    public static synchronized TrustedParticipantsWhitelist getInstance() {
        if (instance == null) {
            instance = new TrustedParticipantsWhitelist();
        }
        return instance;
    }

    /**
     * Adds a trusted participant to the whitelist.
     * This method checks if the participant is already in the list before adding them,
     * to prevent duplicates.
     *
     * @return true if the participant was added, false if the participant already exists.
     */
    public boolean addTrustedParticipant(Participant participant) {
        if (!containsTrustedParticipant(participant)) {
            trustedParticipants.add(participant);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the list of all trusted participants.
     *
     * @return A list of trusted participant names.
     */
    public List<Participant> getTrustedParticipants() {
        return new ArrayList<>(trustedParticipants);
    }

    /**
     * Removes a trusted participant from the whitelist.
     *
     * @return true if the participant is removed.
     *
     */
    public boolean removeTrustedParticipant(Participant participant) {
        return trustedParticipants.remove(participant);
    }

    /**
     * Checks if a participant is already in the whitelist.
     *
     * @return true if the participant is already in the whitelist, false otherwise.
     */
    public boolean containsTrustedParticipant(Participant participant) {
        return trustedParticipants.contains(participant);
    }

    /**
     * Clears all trusted participants from the whitelist.
     * This method is particularly useful for resetting the whitelist state during testing.
     */
    public void clear() {
        trustedParticipants.clear();
    }
}
