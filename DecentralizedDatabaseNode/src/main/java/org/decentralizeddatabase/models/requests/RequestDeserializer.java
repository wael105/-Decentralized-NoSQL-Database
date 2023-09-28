package org.decentralizeddatabase.models.requests;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.models.requests.UserRequest;

import java.io.IOException;

public class RequestDeserializer extends JsonDeserializer<Request> {

    @Override
    public Request deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.has("nodeName")) {
            return deserializationContext.readTreeAsValue(node, NodeRequest.class);
        } else {
            return deserializationContext.readTreeAsValue(node, UserRequest.class);
        }
    }
}