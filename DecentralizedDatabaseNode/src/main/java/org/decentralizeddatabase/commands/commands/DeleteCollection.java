package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.models.DatabaseObject;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.models.requests.UserRequest;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

public class DeleteCollection extends Command {

    public DeleteCollection(DatabaseService databaseService,
                            CollectionService collectionService,
                            DocumentService documentService,
                            ValidationService validationService,
                            DatabaseUserState databaseUserState,
                            NodeCommunicator nodeCommunicator) {
        super(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
    }

    @Override
    public Object execute(Request request) {
        if (!isCurrentDatabaseSet())
            return FailedResponse.DATABASE_NOT_SET.getValue();

        DatabaseObject databaseObject = databaseUserState.getCurrentDatabaseObject();

        String collectionName = (String) request.getData().get("name");

        Response response = collectionService.deleteCollection(collectionName, databaseObject);
        if (request instanceof UserRequest)
            syncWithAllNodes(Operation.DELETE_DATABASE, request.getData(), databaseObject.getDatabaseName(), null, response);

        return response.getValue();
    }
}
