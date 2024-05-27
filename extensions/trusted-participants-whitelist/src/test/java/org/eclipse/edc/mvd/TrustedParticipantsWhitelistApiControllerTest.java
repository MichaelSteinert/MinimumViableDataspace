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
import org.eclipse.edc.mvd.model.TrustedParticipantsResponse;
import org.eclipse.edc.mvd.util.HashUtil;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrustedParticipantsWhitelistApiControllerTest {

    private AutoCloseable closeable;
    @Mock
    private Monitor monitor;
    @Mock
    private TrustedParticipantsWhitelist trustedList;
    private TrustedParticipantsWhitelistApiController controller;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        // Use reflection to inject the mocked singleton instance
        Field instance = TrustedParticipantsWhitelist.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, trustedList);
        // Now when the controller calls getInstance, it will receive the mock
        controller = new TrustedParticipantsWhitelistApiController(monitor);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testCheckHealth() {
        String healthStatus = controller.checkHealth();
        assertEquals("{\"response\":\"Web server running on Connector and ready for requests\"}", healthStatus);
        verify(monitor).info("Received a health request");
    }

    @Test
    void testAddTrustedParticipant() {
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        when(trustedList.addTrustedParticipant(participant)).thenReturn(true);
        String response = controller.addTrustedParticipant(participant);
        verify(trustedList).addTrustedParticipant(participant);
        verify(monitor).info("Adding trusted participant: " + participant.name());
        assertEquals("{\"response\":\"Participant added successfully\"}", response);
    }

    @Test
    void testAddExistingTrustedParticipant() {
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        when(trustedList.addTrustedParticipant(participant)).thenReturn(false);
        String response = controller.addTrustedParticipant(participant);
        verify(trustedList).addTrustedParticipant(participant);
        verify(monitor).info("Adding trusted participant: " + participant.name());
        assertEquals("{\"response\":\"Participant already exists\"}", response);
    }

    @Test
    void testGetTrustedParticipants() throws NoSuchAlgorithmException {
        List<Participant> expectedParticipants = List.of(
                new Participant("did:example:123456789abcdefghi", "testParticipant1", Optional.empty()),
                new Participant("did:example:123456789jklmnopqr", "testParticipant2", Optional.empty())
        );
        when(trustedList.getTrustedParticipants()).thenReturn(expectedParticipants);
        TrustedParticipantsResponse response = controller.getTrustedParticipants();
        String expectedHash = HashUtil.computeHash(expectedParticipants);
        verify(trustedList).getTrustedParticipants();
        verify(monitor).info("Retrieving trusted participants");
        assertEquals(expectedParticipants, response.participants());
        assertEquals(expectedHash, response.hash());
    }

    @Test
    void testRemoveTrustedParticipant() {
        Participant participant = new Participant("did:example:123456789abcdefghi", "testParticipant", Optional.empty());
        when(trustedList.removeTrustedParticipant(participant)).thenReturn(true);
        String response = controller.removeTrustedParticipant(participant);
        verify(trustedList).removeTrustedParticipant(participant);
        verify(monitor).info("Removing trusted participant: " + participant.name());
        assertEquals("{\"response\":\"Participant removed successfully\"}", response);
    }
}
