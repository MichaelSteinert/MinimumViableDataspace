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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a whitelist of trusted participants. This class uses the Singleton design pattern
 * to ensure that only one instance of the whitelist is used throughout the application.
 * It provides methods to add, remove, and retrieve trusted participants.
 */
public class TrustedParticipantsWhitelist {

    private static TrustedParticipantsWhitelist instance;
    private final List<String> trustedParticipants;

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
     *
     * @param name The name of the participant to be added.
     */
    public void addTrustedParticipant(String name) {
        trustedParticipants.add(name);
    }

    /**
     * Retrieves the list of all trusted participants.
     *
     * @return A list of trusted participant names.
     */
    public List<String> getTrustedParticipants() {
        return new ArrayList<>(trustedParticipants);
    }

    /**
     * Removes a trusted participant from the whitelist.
     *
     * @param name The name of the participant to be removed.
     */
    public void removeTrustedParticipant(String name) {
        trustedParticipants.remove(name);
    }
}
