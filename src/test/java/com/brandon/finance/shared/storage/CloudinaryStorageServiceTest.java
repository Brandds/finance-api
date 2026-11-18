package com.brandon.finance.shared.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

class CloudinaryStorageServiceTest {
    private static final String PUBLIC_ID = "avatars/42/photo";
    private static final byte[] IMAGE = {1, 2, 3};

    private final Cloudinary client = mock(Cloudinary.class);
    private final Uploader uploader = mock(Uploader.class);
    private final CloudinaryStorageService storage = new CloudinaryStorageService(client);

    @Test
    void generatesPublicHttpsCdnUrlWithoutCredentialsOrNetworkCall() {
        var configured = new CloudinaryStorageService("test-cloud", "test-key", "test-secret");

        assertThat(configured.readUrl(PUBLIC_ID))
            .isEqualTo("https://res.cloudinary.com/test-cloud/image/upload/v1/avatars/42/photo");
    }

    @Test
    void uploadsNormalizedBytesAsPublicImageWithServerGeneratedId() throws IOException {
        when(client.uploader()).thenReturn(uploader);
        var options = Map.of("public_id", PUBLIC_ID, "resource_type", "image", "type", "upload",
            "overwrite", false, "filename", "avatar.png");
        when(uploader.upload(IMAGE, options)).thenReturn(Map.of("public_id", PUBLIC_ID));

        storage.upload(PUBLIC_ID, IMAGE, "image/png");

        verify(uploader).upload(IMAGE, options);
    }

    @Test
    void treatsMissingRemoteImageAsDeletedAndRequestsCdnInvalidation() throws IOException {
        when(client.uploader()).thenReturn(uploader);
        var options = Map.of("resource_type", "image", "type", "upload", "invalidate", true);
        when(uploader.destroy(PUBLIC_ID, options)).thenReturn(Map.of("result", "not found"));

        storage.delete(PUBLIC_ID);

        verify(uploader).destroy(PUBLIC_ID, options);
    }

    @Test
    void sanitizesProviderExceptionsAndRejectsUnsuccessfulResponses() throws IOException {
        when(client.uploader()).thenReturn(uploader);
        when(uploader.upload(org.mockito.ArgumentMatchers.eq(IMAGE), org.mockito.ArgumentMatchers.anyMap()))
            .thenThrow(new IOException("internal provider details"));
        when(uploader.destroy(org.mockito.ArgumentMatchers.eq(PUBLIC_ID), org.mockito.ArgumentMatchers.anyMap()))
            .thenReturn(Map.of("result", "error"));

        assertThatThrownBy(() -> storage.upload(PUBLIC_ID, IMAGE, "image/jpeg"))
            .isInstanceOf(StorageUnavailableException.class)
            .hasNoCause()
            .hasMessageNotContaining("internal provider details");
        assertThatThrownBy(() -> storage.delete(PUBLIC_ID)).isInstanceOf(StorageUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenCredentialsAreAbsent() {
        var unconfigured = new CloudinaryStorageService("", "", "");
        assertThatThrownBy(() -> unconfigured.readUrl(PUBLIC_ID))
            .isInstanceOf(StorageUnavailableException.class);
    }
}
