package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

public class SetDatabase extends Command {

    public SetDatabase(DatabaseService databaseService,
                       CollectionService collectionService,
                       DocumentService documentService,
                       ValidationService validationService,
                       DatabaseUserState databaseUserState,
                       NodeCommunicator nodeCommunicator) {
        super(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
    }

    @Override
    public Object execute(Request request) {
        Response result;

        if (request instanceof NodeRequest nodeRequest)
            result = databaseService.setDatabase(nodeRequest.getDatabaseName(), databaseUserState);
        else
            result = databaseService.setDatabase((String) request.getData().get("name"), databaseUserState);

        return result.getValue();
    }
}
