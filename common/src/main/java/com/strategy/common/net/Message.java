package com.strategy.common.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {
    private String type;      // e.g., "LOGIN", "MOVE", "ERROR"
    private JsonNode payload; // The actual data (flexible content)

    // Jackson needs a default constructor
    public Message() {}

    public Message(String type, JsonNode payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }

    // Helper to create a message easily
    public static Message create(String type, Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return new Message(type, mapper.valueToTree(data));
    }
}