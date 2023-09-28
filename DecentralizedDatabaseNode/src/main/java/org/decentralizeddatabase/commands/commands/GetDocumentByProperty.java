package org.decentralizeddatabase.commands.commands;

import org.decentralizeddatabase.DatabaseUserState;
import org.decentralizeddatabase.commands.Command;
import org.decentralizeddatabase.constants.FailedResponse;
import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.requests.Request;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.CollectionService;
import org.decentralizeddatabase.services.DatabaseService;
import org.decentralizeddatabase.services.DocumentService;
import org.decentralizeddatabase.services.ValidationService;

public class GetDocumentByProperty extends Command {

    public GetDocumentByProperty(DatabaseService databaseService,
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

        String field = String.valueOf(request.getData().get("field"));
        String value = String.valueOf(request.getData().get("value"));
        return documentService.getDocumentsByProperty(collection, field, value);
    }
}
