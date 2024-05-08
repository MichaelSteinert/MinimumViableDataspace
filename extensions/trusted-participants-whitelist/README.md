# Trusted Participants Whitelist API

| HTTP Request                        | Description                                                                                                                               |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `GET /health`                       | Checks the health of the whitelist and returns its status.                                                                                |
| `POST /add`                         | Adds a trusted participant to the whitelist and returns the outcome.                                                                      |
| `GET /list`                         | Retrieves a list of trusted participants.                                                                                                 |
| `DELETE /remove`                    | Removes a trusted participant from the whitelist and returns the outcome.                                                                 |
| `POST /negotiate/{counterPartyUrl}` | Initiates a negotiation with another whitelist to determine common trusted participants. Expects path variable with the counterparty URL. |
| `POST /receive-negotiation`         | Handles incoming negotiation requests, matches trusted participants, and returns the negotiation outcome.                                 |
| `POST /notify`                      | Receives notifications related to data trustee selection.                                                                                 |
