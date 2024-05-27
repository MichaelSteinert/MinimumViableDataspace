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

package org.eclipse.edc.mvd.util;

import org.eclipse.edc.mvd.model.Participant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class HashUtil {
    public static String computeHash(List<Participant> participants) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        StringBuilder participantsString = new StringBuilder();
        for (Participant participant : participants) {
            participantsString.append(participant.toString());
        }
        byte[] hash = digest.digest(participantsString.toString().getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
