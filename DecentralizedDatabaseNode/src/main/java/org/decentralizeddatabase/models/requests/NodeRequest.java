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
public class NodeRequest implements Request{

    private Operation operation;

    private Map<String, Object> data;

    private String nodeName;

    private String databaseName;

    private String collectionName;

    @Override
    public String toString() {
        return "NodeRequest(operation=" + this.getOperation() + ", data=" + this.getData() + ", nodeName=" + this.getNodeName() + ", databaseName=" + this.getDatabaseName() + ", collectionName=" + this.getCollectionName() + ")";
    }
}
