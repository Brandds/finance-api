package com.brandon.finance.shared.storage;

public class StorageUnavailableException extends RuntimeException {
    public StorageUnavailableException() {
        super("Armazenamento de avatar temporariamente indisponível");
    }
}
