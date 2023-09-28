package org.decentralizeddatabase.constants;

public enum FailedResponse implements Response {
    DATABASE_ALREADY_EXISTS("Database already exists"),
    DATABASE_DOES_NOT_EXIST("Database does not exist"),
    DATABASE_NOT_SET("The database is not set"),
    COLLECTION_ALREADY_EXISTS("Collection already exists"),
    COLLECTION_DOES_NOT_EXIST("Collection does not exist"),
    COLLECTION_NOT_SET("The collection is not set"),
    DOCUMENT_DOES_NOT_EXIST("Document does not exist"),
    DOCUMENT_ALREADY_EXISTS("Document already exists"),
    DOCUMENTS_NOT_FOUND("No Document satisfies the property"),
    DOCUMENT_UPDATE_FAILED("Document update failed"),
    INVALID_REQUEST_TYPE("Invalid request type"),
    CONCURRENCY_CONFLICT("Concurrency conflict"),
    VALUE_DOES_NOT_MATCH_SCHEMA("Value does not match schema"),;

    private final String value;

    FailedResponse(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
