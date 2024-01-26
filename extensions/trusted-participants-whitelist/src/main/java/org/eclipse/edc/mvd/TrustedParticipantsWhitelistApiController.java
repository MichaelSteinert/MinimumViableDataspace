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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.spi.monitor.Monitor;

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

    /**
     * Constructor for TrustedParticipantsWhitelistApiController.
     *
     * @param monitor The monitor used for logging and monitoring.
     */
    public TrustedParticipantsWhitelistApiController(Monitor monitor) {
        this.monitor = monitor;
        this.trustedList = TrustedParticipantsWhitelist.getInstance();
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
     * @param name The name of the participant to add.
     * @return A response indicating the outcome.
     */
    @POST
    @Path("add")
    public String addTrustedParticipant(@QueryParam("name") String name) {
        monitor.info("Adding trusted participant: " + name);
        boolean isAdded = trustedList.containsTrustedParticipant(name);
        if (isAdded) {
            return "{\"response\":\"Participant added\"}";
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
    public List<String> getTrustedParticipants() {
        monitor.info("Retrieving trusted participants");
        return trustedList.getTrustedParticipants();
    }

    /**
     * Removes a trusted participant from the whitelist.
     *
     * @param name The name of the participant to remove.
     * @return A response indicating the outcome.
     */
    @DELETE
    @Path("remove")
    public String removeTrustedParticipant(@QueryParam("name") String name) {
        monitor.info("Removing trusted participant: " + name);
        trustedList.removeTrustedParticipant(name);
        return "{\"response\":\"Participant removed\"}";
    }
}