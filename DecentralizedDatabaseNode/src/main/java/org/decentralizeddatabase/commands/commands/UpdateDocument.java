package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.constants.Operation;
import org.decentralizeddatabase.constants.Response;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

import java.util.Map;

public class UpdateDocument extends Command {

    public UpdateDocument(DatabaseService databaseService,
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

        if(!validationService.validateDocument(collection, data))
            return FailedResponse.VALUE_DOES_NOT_MATCH_SCHEMA.getValue();

        int id = (int) data.get("id");

        if (collection.hasAffinity(id)) {
            Response res = documentService.updateDocument(collection, data);
            syncWithAllNodes(Operation.UPDATE_DOCUMENT, data, collection.getDatabaseName(), collection.getCollectionName(), res);
            return res.getValue();
        }

        if (request instanceof NodeRequest)
            return documentService.updateDocument(collection, data).getValue();

        return nodeCommunicator.redirectRequest(
                Operation.UPDATE_DOCUMENT,
                data,
                collection.getDatabaseName(),
                collection.getCollectionName(),
                collection.getAffinityNode(id));
    }
}
