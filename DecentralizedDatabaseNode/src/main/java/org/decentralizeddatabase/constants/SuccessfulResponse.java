package org.decentralizeddatabase.constants;

public enum SuccessfulResponse implements Response {
    DATABASE_CREATED("Database created successfully"),
    DATABASE_DELETED("Database deleted successfully"),
    DATABASE_SET("Database set successfully"),
    COLLECTION_CREATED("Collection created successfully"),
    COLLECTION_DELETED("Collection deleted successfully"),
    COLLECTION_SET("Collection set successfully"),
    DOCUMENT_INSERTED("Document inserted successfully"),
    DOCUMENT_UPDATED("Document updated successfully"),
    DOCUMENT_DELETED("Document deleted successfully"),;

    private final String value;

    SuccessfulResponse(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
