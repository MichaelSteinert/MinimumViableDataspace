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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.mvd.model.DataTrusteeRequest;
import org.eclipse.edc.mvd.model.NegotiationRequest;
import org.eclipse.edc.mvd.model.NegotiationResponse;
import org.eclipse.edc.mvd.model.Participant;
import org.eclipse.edc.spi.monitor.Monitor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * The RegionConstraintFunction class implements the AtomicConstraintFunction interface
 * for handling region-based constraints in permission evaluations.
 * This class can be used to determine whether certain region-specific conditions
 * are met based on policy rules and context.
 */
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/trusted-participants")
public class TrustedParticipantsWhitelistApiController {

    private final Monitor monitor;
    private final TrustedParticipantsWhitelist trustedList;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for TrustedParticipantsWhitelistApiController.
     *
     * @param monitor The monitor used for logging and monitoring.
     */
    public TrustedParticipantsWhitelistApiController(Monitor monitor) {
        this.monitor = monitor;
        this.trustedList = TrustedParticipantsWhitelist.getInstance();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Checks the health of the service.
     *
     * @return A string indicating the health status.
     */
    @GET
    @Path("health")
    public String checkHealth() {
        monitor.info("Received a health request");
        return "{\"response\":\"Web server running on Connector and ready for requests\"}";
    }

    /**
     * Adds a trusted participant to the whitelist.
     *
     * @return A response indicating the outcome.
     */
    @POST
    @Path("add")
    public String addTrustedParticipant(Participant participant) {
        monitor.info("Adding trusted participant: " + participant.name());
        boolean isAdded = trustedList.addTrustedParticipant(participant);
        if (isAdded) {
            return "{\"response\":\"Participant added successfully\"}";
        } else {
            return "{\"response\":\"Participant already exists\"}";
        }
    }

    /**
     * Retrieves a list of trusted participants.
     *
     * @return A list of trusted participants.
     */
    @GET
    @Path("list")
    public List<Participant> getTrustedParticipants() {
        monitor.info("Retrieving trusted participants");
        return trustedList.getTrustedParticipants();
    }

    /**
     * Removes a trusted participant from the whitelist.
     *
     * @return A response indicating the outcome.
     */
    @DELETE
    @Path("remove")
    public String removeTrustedParticipant(Participant participant) {
        monitor.info("Removing trusted participant: " + participant.name());
        if (trustedList.removeTrustedParticipant(participant)) {
            return "{\"response\":\"Participant removed successfully\"}";
        } else {
            return "{\"response\":\"Participant not found\"}";
        }
    }

    /**
     * Initiates a negotiation with another system to determine common trusted participants.
     *
     * @param counterPartyUrl The URL of the counterparty to negotiate with.
     * @return The result of the negotiation.
     */
    @POST
    @Path("negotiate")
    public String initiateNegotiation(@QueryParam("id") String counterPartyUrl) {
        try {
            List<Participant> participants = trustedList.getTrustedParticipants();
            String requestBody = objectMapper.writeValueAsString(participants);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(counterPartyUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            monitor.info("Negotiation initiated with: " + counterPartyUrl + "; Response: " + response.body());
            return response.body();
        } catch (Exception e) {
            monitor.warning("Failed to initiate negotiation with " + counterPartyUrl + ": " + e.getMessage());
            return "{\"error\":\"Failed to send negotiation request: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Receives a negotiation request from another participant, matches trusted participants, and chooses one for data transfer.
     *
     * @param negotiationRequest The list of trusted participants from the negotiation initiator.
     * @return A response with matched participants and the chosen participant.
     */
    @POST
    @Path("receive-negotiation")
    public String receiveNegotiation(NegotiationRequest negotiationRequest) {
        monitor.info("Received negotiation request");
        List<Participant> matches = trustedList.getTrustedParticipants().stream()
                .filter(negotiationRequest.trustedDataTrustees()::contains)
                .toList();
        // Select the first matched participant for simplicity, can implement a better selection logic
        Participant chosenDataTrustee = matches.isEmpty() ? null : matches.get(0);
        if (chosenDataTrustee != null && chosenDataTrustee.url().isPresent()) {
            try {
                String notificationUrl = chosenDataTrustee.url() + "/notify";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(notificationUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(new DataTrusteeRequest(
                                        negotiationRequest.dataSource(),
                                        negotiationRequest.dataSink(),
                                        negotiationRequest.assets()
                                ))))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                monitor.info("Notification sent to " + chosenDataTrustee.name() + "; Response: " + response.body());
            } catch (Exception e) {
                monitor.warning("Failed to send notification to " + chosenDataTrustee.name() + ": " + e.getMessage());
            }
            var negotiationResponse = new NegotiationResponse(
                    negotiationRequest.dataSource(),
                    negotiationRequest.dataSink(),
                    chosenDataTrustee,
                    negotiationRequest.assets()
            );
            return "{\"dataSource\":" + negotiationResponse.dataSource() +
                    ", \"dataSink\":\"" + negotiationResponse.dataSource() +
                    ", \"trustedDataTrustee\":\"" + chosenDataTrustee +
                    ", \"assets\":\"" + negotiationResponse.assets() +
                    "\"}";
        } else {
            return "{\"trustedDataTrustee\":[], \"message\":\"No commonly trusted data trustee found\"}";
        }
    }

    @POST
    @Path("notify")
    public void receiveNotification(String notification) {
        monitor.info("Received notification: " + notification);
    }
}