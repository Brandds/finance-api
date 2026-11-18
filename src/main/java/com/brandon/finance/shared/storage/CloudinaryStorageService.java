package com.brandon.finance.shared.storage;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cloudinary.Cloudinary;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CloudinaryStorageService implements StorageService {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryStorageService(
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
        this(StringUtils.hasText(cloudName) && StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret)
            ? new Cloudinary(Map.of("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret,
                "secure", true, "timeout", 10, "analytics", false))
            : null);
    }

    CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public void upload(String publicId, byte[] image, String contentType) {
        var client = requireConfigured();
        String filename = "image/jpeg".equals(contentType) ? "avatar.jpg" : "avatar.png";
        Map<?, ?> response;
        try {
            response = client.uploader().upload(image, Map.of("public_id", publicId, "resource_type", "image",
                "type", "upload", "overwrite", false, "filename", filename));
        } catch (IOException | RuntimeException ex) {
            throw unavailable("upload", ex.getClass().getSimpleName());
        }

        if (!publicId.equals(response.get("public_id")) || Boolean.TRUE.equals(response.get("existing"))
                || response.containsKey("error")) {
            throw unavailable("upload", "resposta inesperada");
        }
    }

    @Override
    public void delete(String publicId) {
        var client = requireConfigured();
        Map<?, ?> response;
        try {
            response = client.uploader().destroy(publicId, Map.of("resource_type", "image",
                "type", "upload", "invalidate", true));
        } catch (IOException | RuntimeException ex) {
            throw unavailable("exclusão", ex.getClass().getSimpleName());
        }

        Object result = response.get("result");
        if (!"ok".equals(result) && !"not found".equals(result)) {
            throw unavailable("exclusão", "resposta inesperada");
        }
    }

    @Override
    public String readUrl(String publicId) {
        return requireConfigured().url().secure(true).resourceType("image")
            .type("upload").forceVersion(true).generate(publicId);
    }

    private Cloudinary requireConfigured() {
        if (cloudinary == null) {
            throw new StorageUnavailableException();
        }
        return cloudinary;
    }

    private StorageUnavailableException unavailable(String operation, String reason) {
        log.warn("Falha de {} no Cloudinary ({})", operation, reason);
        return new StorageUnavailableException();
    }
}
