package com.brandon.finance.shared.storage;

public interface StorageService {
    void upload(String objectId, byte[] image, String contentType);

    void delete(String objectId);

    String readUrl(String objectId);
}
