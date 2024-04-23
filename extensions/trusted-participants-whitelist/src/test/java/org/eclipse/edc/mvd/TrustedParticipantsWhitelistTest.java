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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        boolean added = whitelist.addTrustedParticipant(participant);
        assertThat(added).isTrue();
        assertThat(whitelist.containsTrustedParticipant(participant)).isTrue();
    }

    @Test
    void shouldNotAddDuplicateTrustedParticipant() {
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        whitelist.addTrustedParticipant(participant);
        boolean addedAgain = whitelist.addTrustedParticipant(participant);
        assertThat(addedAgain).isFalse();
    }

    @Test
    void shouldRemoveTrustedParticipant() {
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        whitelist.addTrustedParticipant(participant);
        whitelist.removeTrustedParticipant(participant);
        assertThat(whitelist.containsTrustedParticipant(participant)).isFalse();
    }
}
