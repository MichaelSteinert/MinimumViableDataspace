#!/bin/bash

# stop on error
set -euo pipefail

function registerParticipant() {
  local participantName="$1"
  local participantDid="$2"

  echo "Registering $participantName"
  java -jar registration-service-cli.jar \
              -d="did:web:did-server:registration-service" \
              --http-scheme \
              -k=/resources/vault/$participantName/private-key.pem \
              -c="$participantDid" \
               participants add
}

function seedVerifiedCredentials() {
  local participantName="$1"
  local participantDid="$2"
  local region="$3"
  local trustedParticipants="$4"

  # Convert comma-separated trustedParticipants into a JSON array
    local jsonTrustedParticipantsArray="["
    local first=true
    IFS=',' read -ra ADDR <<< "$trustedParticipants"
    for i in "${ADDR[@]}"; do
      if [ "$first" = true ]; then
        jsonTrustedParticipantsArray+="\"$i\""
        first=false
      else
        jsonTrustedParticipantsArray+=", \"$i\""
      fi
    done
    jsonTrustedParticipantsArray+="]"

  # Seeding VC for region and trusted participants
    local claims="{\"region\": \"$region\", \"trusted_participants\": $jsonTrustedParticipantsArray}"
    echo "Seeding VC for $participantName with claims: $claims"

    java -jar identity-hub-cli.jar \
             -s="http://$participantName:7171/api/identity/identity-hub" \
             vc add \
             -c="$claims" \
             -b="$participantDid" \
             -i="did:web:did-server:gaia-x" \
             -k="/resources/vault/gaia-x/private-key.pem"
}

function seedAndRegisterParticipant() {
  local participantName="$1"
  local region="$2"
  local trustedParticipants="$3"
  local participantDid="did:web:did-server:$participantName"

  # seed vc for participant
  seedVerifiedCredentials "$participantName" "$participantDid" "$region" "$trustedParticipants"

  # Register dataspace participants
  registerParticipant "$participantName" "$participantDid"
}

function awaitParticipantRegistration() {
  local participantName="$1"
  local participantDid="did:web:did-server:$participantName"

  cmd="java -jar registration-service-cli.jar \
                  -d=did:web:did-server:registration-service \
                  --http-scheme \
                  -k=/resources/vault/$participantName/private-key.pem \
                  -c=$participantDid \
                  participants get"

  # Wait for participant registration.
  ./validate_onboarding.sh "$participantDid" "$cmd"
}

# Read participants from participants.json file.
# $participants will contain participants, regions and trusted participants in a shell readable format e.g.:
participants=$(jq -r '.include | map([.participant, .region, (.trusted_participants | join(","))])[] | @sh' /common-resources/participants.json)

# Seed VCs and register participants.
while read -r participantName region trustedParticipants; do
  # shellcheck disable=SC2086 # disable IDE warning: allow word splitting on jq @sh output
  eval seedAndRegisterParticipant "$participantName" "$region" "$trustedParticipants"
done <<< "$participants"

# Seed VCs and register participants.
echo "Starting to seed and register participants..."
while read -r participantName; do
  echo "Processing participant: $participantName, Region: $region, Trusted Participants: $trustedParticipants"
  # shellcheck disable=SC2086 # disable IDE warning: allow word splitting on jq @sh output
  eval awaitParticipantRegistration $participantName
done <<< "$participants"
echo "Completed seeding and registering participants."

# flag for healthcheck by Docker
echo "finished" > finished.flag
echo "Finished successfully! Keep the container running."

# keep the container running
sleep infinity
