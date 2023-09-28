package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.models.requests.UserRequest;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

public class DeleteDatabase extends Command {

        public DeleteDatabase(DatabaseService databaseService,
                              CollectionService collectionService,
                              DocumentService documentService,
                              ValidationService validationService,
                              DatabaseUserState databaseUserState,
                              NodeCommunicator nodeCommunicator) {
            super(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
        }

        @Override
        public Object execute(Request request) {
            String databaseName = (String) request.getData().get("name");
            Response response = databaseService.deleteDatabase(databaseName);
            if (request instanceof UserRequest)
                syncWithAllNodes(Operation.DELETE_DATABASE, request.getData(), null, null, response);
            return response.getValue();
        }
}
