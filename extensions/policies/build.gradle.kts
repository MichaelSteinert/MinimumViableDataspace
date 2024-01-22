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
 *       Microsoft Corporation - initial implementation
 *
 */

plugins {
    `java-library`
}

dependencies {
    api(libs.edc.spi.contract)
    api(libs.edc.spi.policy.engine)
//    api(edc.core.connector)
    implementation(libs.ih.spi.core)
    implementation(project(":extensions:trusted-participants-whitelist"))

    testImplementation(libs.edc.core.policy.engine)
}
