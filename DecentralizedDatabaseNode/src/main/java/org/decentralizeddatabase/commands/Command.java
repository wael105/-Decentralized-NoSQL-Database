package org.decentralizeddatabase.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.constants.SuccessfulResponse;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

import java.util.Map;

public abstract class Command {

    protected final DatabaseService databaseService;

    protected final CollectionService collectionService;

    protected final DocumentService documentService;

    protected final ValidationService validationService;

    protected final DatabaseUserState databaseUserState;

    protected final NodeCommunicator nodeCommunicator;

    protected Command(DatabaseService databaseService,
                      CollectionService collectionService,
                      DocumentService documentService,
                      ValidationService validationService,
                      DatabaseUserState databaseUserState,
                      NodeCommunicator nodeCommunicator) {
        this.databaseService = databaseService;
        this.collectionService = collectionService;
        this.documentService = documentService;
        this.validationService = validationService;
        this.databaseUserState = databaseUserState;
        this.nodeCommunicator = nodeCommunicator;
    }

    public abstract Object execute(Request request);

    protected void syncWithAllNodes(Operation operation, Map<String, Object> data, String databaseName, String collectionName, Response response) {
        if (response instanceof SuccessfulResponse)
            nodeCommunicator.syncWithAllNodes(operation, data, databaseName, collectionName);
    }

    protected boolean isCurrentDatabaseSet() {
        return databaseUserState.getCurrentDatabaseObject() != null;
    }

    protected boolean isCurrentCollectionSet() {
        return isCurrentDatabaseSet() && databaseUserState.getCurrentCollection() != null;
    }
}
