package org.decentralizeddatabase.services;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.commands.CommandFactory;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.nodes.NodeCommunicator;

public class DatabaseFacade {

    private final DatabaseUserState databaseUserState;

    private final DatabaseService databaseService;

    private final CollectionService collectionService;

    private final DocumentService documentService;

    private final ValidationService validationService;

    private final NodeCommunicator nodeCommunicator;

    public DatabaseFacade(DatabaseService databaseService,
                          CollectionService collectionService,
                          DocumentService documentService,
                          ValidationService validationService,
                          NodeCommunicator nodeCommunicator) {
        this.databaseUserState = new DatabaseUserState();
        this.databaseService = databaseService;
        this.collectionService = collectionService;
        this.documentService = documentService;
        this.validationService = validationService;
        this.nodeCommunicator = nodeCommunicator;
    }

    public Object processRequest(Request request) {

        setupNodeRequestState(request);

        Command command = CommandFactory.getCommand(
                request.getOperation(),
                databaseUserState,
                databaseService,
                collectionService,
                documentService,
                validationService,
                nodeCommunicator
        );

        return command.execute(request);
    }

    private void setupNodeRequestState(Request request) {
        if (request instanceof NodeRequest nodeRequest) {
            String databaseName = nodeRequest.getDatabaseName();
            String collectionName = nodeRequest.getCollectionName();
            if (databaseName != null)
                databaseService.setDatabase(databaseName, databaseUserState);
            if (collectionName != null)
                collectionService.setCollection(collectionName, databaseUserState);
        }
    }

}
