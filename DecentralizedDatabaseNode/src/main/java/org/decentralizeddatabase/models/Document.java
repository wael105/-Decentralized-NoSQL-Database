package org.decentralizeddatabase.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Document {
    private Map<String, Object> value;
}
