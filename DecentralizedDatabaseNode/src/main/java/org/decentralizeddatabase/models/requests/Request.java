package org.decentralizeddatabase.models.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.decentralizeddatabase.constants.Operation;

import java.util.Map;

@JsonDeserialize(using = RequestDeserializer.class)
public interface Request {

    Operation getOperation();

    Map<String, Object> getData();

}
