package org.decentralizeddatabase.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.commands.*;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

public class CommandFactory {

    private CommandFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static Command getCommand(Operation operation,
                                     DatabaseUserState databaseUserState,
                                     DatabaseService databaseService,
                                     CollectionService collectionService,
                                     DocumentService documentService,
                                     ValidationService validationService,
                                     NodeCommunicator nodeCommunicator) {
        Command command;

        switch (operation) {
            case CREATE_DATABASE -> command = new CreateDatabase(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case SET_DATABASE -> command = new SetDatabase(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case DELETE_DATABASE -> command = new DeleteDatabase(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case CREATE_COLLECTION -> command = new CreateCollection(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case SET_COLLECTION -> command = new SetCollection(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case DELETE_COLLECTION -> command = new DeleteCollection(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case INSERT_DOCUMENT -> command = new InsertDocument(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case UPDATE_DOCUMENT -> command = new UpdateDocument(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case GET_DOCUMENT_BY_ID -> command = new GetDocumentById(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case GET_DOCUMENTS_BY_FIELD -> command = new GetDocumentByProperty(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            case DELETE_DOCUMENT -> command = new DeleteDocument(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
            // should never happen unless new operations are added
            default -> throw new IllegalStateException("Unexpected value: " + operation);
        }

        return command;
    }
}
