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

import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
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
        String participantName = "testParticipant";
        when(trustedList.addTrustedParticipant(participantName)).thenReturn(true);
        String response = controller.addTrustedParticipant(participantName);
        verify(trustedList).addTrustedParticipant(participantName);
        verify(monitor).info("Adding trusted participant: " + participantName);
        assertEquals("{\"response\":\"Participant added\"}", response);
    }

    @Test
    void testAddExistingTrustedParticipant() {
        String participantName = "existingParticipant";
        when(trustedList.addTrustedParticipant(participantName)).thenReturn(false);
        String response = controller.addTrustedParticipant(participantName);
        verify(trustedList).addTrustedParticipant(participantName);
        verify(monitor).info("Adding trusted participant: " + participantName);
        assertEquals("{\"response\":\"Participant already exists\"}", response);
    }

    @Test
    void testGetTrustedParticipants() {
        when(trustedList.getTrustedParticipants()).thenReturn(List.of("participant1", "participant2"));
        var participants = controller.getTrustedParticipants();
        verify(trustedList).getTrustedParticipants();
        verify(monitor).info("Retrieving trusted participants");
        assertEquals(List.of("participant1", "participant2"), participants);
    }

    @Test
    void testRemoveTrustedParticipant() {
        String participantName = "participantToRemove";
        doNothing().when(trustedList).removeTrustedParticipant(participantName);
        String response = controller.removeTrustedParticipant(participantName);
        verify(trustedList).removeTrustedParticipant(participantName);
        verify(monitor).info("Removing trusted participant: " + participantName);
        assertEquals("{\"response\":\"Participant removed\"}", response);
    }
}
