package org.decentralizeddatabase;

import org.decentralizeddatabase.models.Collection;
import org.decentralizeddatabase.models.DatabaseObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseUserState {

    private DatabaseObject currentDatabaseObject;

    private Collection currentCollection;

}