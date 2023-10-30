package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.models.requests.UserRequest;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

import java.util.Map;

public class InsertDocument extends Command {

        public InsertDocument(DatabaseService databaseService,
                              CollectionService collectionService,
                              DocumentService documentService,
                              ValidationService validationService,
                              DatabaseUserState databaseUserState,
                              NodeCommunicator nodeCommunicator) {
            super(databaseService, collectionService, documentService, validationService, databaseUserState, nodeCommunicator);
        }

        @Override
        public Object execute(Request request) {
            if (!isCurrentCollectionSet())
                return FailedResponse.COLLECTION_NOT_SET.getValue();

            Collection collection = databaseUserState.getCurrentCollection();

            Map<String, Object> data = request.getData();

            if(!validationService.isDocumentValid(collection, data))
                return FailedResponse.VALUE_DOES_NOT_MATCH_SCHEMA.getValue();

            Response result;

            if (request instanceof NodeRequest nodeRequest)
                result = documentService.createDocumentAndAddAffinity(collection, data, nodeRequest.getNodeName(), (int) data.get("id"));
            else if (request instanceof UserRequest){
                result = documentService.createDocumentWithoutAddingAffinity(collection, data);
                syncWithAllNodes(Operation.INSERT_DOCUMENT, data, collection.getDatabaseName(), collection.getCollectionName(), result);
            } else
                throw new RuntimeException("Invalid request type");

            return result.getValue();
        }
}
