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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedParticipantsWhitelistTest {

    private TrustedParticipantsWhitelist whitelist;

    @BeforeEach
    void setUp() {
        whitelist = TrustedParticipantsWhitelist.getInstance();
        // Ensure the whitelist is empty before each test
        whitelist.clear();
    }

    @Test
    void shouldBeSingleton() {
        TrustedParticipantsWhitelist anotherInstance = TrustedParticipantsWhitelist.getInstance();
        assertThat(whitelist).isSameAs(anotherInstance);
    }

    @Test
    void shouldAddTrustedParticipant() {
        boolean added = whitelist.addTrustedParticipant("participant1");
        assertThat(added).isTrue();
        assertThat(whitelist.containsTrustedParticipant("participant1")).isTrue();
    }

    @Test
    void shouldNotAddDuplicateTrustedParticipant() {
        whitelist.addTrustedParticipant("participant1");
        boolean addedAgain = whitelist.addTrustedParticipant("participant1");
        assertThat(addedAgain).isFalse();
    }

    @Test
    void shouldRemoveTrustedParticipant() {
        whitelist.addTrustedParticipant("participant1");
        whitelist.removeTrustedParticipant("participant1");
        assertThat(whitelist.containsTrustedParticipant("participant1")).isFalse();
    }

    @Test
    void shouldClearTrustedParticipants() {
        whitelist.addTrustedParticipant("participant1");
        whitelist.clear();
        assertThat(whitelist.getTrustedParticipants()).isEmpty();
    }
}
