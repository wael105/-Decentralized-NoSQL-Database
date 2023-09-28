package org.decentralizeddatabase.models.requests;

import lombok.extern.jackson.Jacksonized;
import org.decentralizeddatabase.constants.Operation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@Jacksonized
public class UserRequest implements Request{

    private Operation operation;

    private Map<String, Object> data;

}