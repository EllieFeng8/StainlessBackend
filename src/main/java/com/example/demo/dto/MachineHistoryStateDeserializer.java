package com.example.demo.dto;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Reject empty strings and fractional values instead of silently coercing them. */
public class MachineHistoryStateDeserializer extends ValueDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.isExpectedNumberIntToken()) {
            return context.reportInputMismatch(Integer.class, "state must be a JSON integer from 0 to 3");
        }
        int state = parser.getIntValue();
        if (state < 0 || state > 3) {
            return context.reportInputMismatch(Integer.class, "state must be a JSON integer from 0 to 3");
        }
        return state;
    }
}
