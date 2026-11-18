package com.brandon.finance.user.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;

@Component
class AvatarImageValidator {
    private static final Set<String> FORMATS = Set.of("jpeg", "png", "webp");
    private static final int MAX_DIMENSION = 2048;
    private final long maxBytes;

    AvatarImageValidator(@Value("${spring.servlet.multipart.max-file-size:2MB}") String maxSize) {
        this.maxBytes = DataSize.parse(maxSize).toBytes();
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("O limite de upload deve ser positivo");
        }
    }

    Image normalize(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "A imagem é obrigatória");
        }
        if (file.getSize() > maxBytes) {
            throw new MaxUploadSizeExceededException(maxBytes);
        }

        try (var source = file.getInputStream(); var input = new MemoryCacheImageInputStream(source)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            var reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!FORMATS.contains(format)) {
                    throw invalidImage();
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "A imagem deve ter no máximo 2048 × 2048 pixels");
                }
                var warning = new AtomicBoolean();
                reader.addIIOReadWarningListener((readerWithWarning, message) -> warning.set(true));
                var decoded = reader.read(0);
                if (warning.get()) {
                    throw invalidImage();
                }
                return encode(decoded, "jpeg".equals(format));
            } finally {
                reader.dispose();
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw invalidImage();
        }
    }

    private Image encode(BufferedImage decoded, boolean jpeg) throws IOException {
        String format = jpeg ? "jpeg" : "png";
        var normalized = new BufferedImage(decoded.getWidth(), decoded.getHeight(),
            jpeg ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.drawImage(decoded, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        var output = new ByteArrayOutputStream();
        if (!ImageIO.write(normalized, format, output)) {
            throw new IllegalStateException("Codificador de avatar indisponível");
        }
        return new Image(output.toByteArray(), "image/" + format);
    }

    private BusinessException invalidImage() {
        return new BusinessException(ErrorCode.VALIDATION_ERROR, "Envie uma imagem JPEG, PNG ou WebP válida");
    }

    record Image(byte[] bytes, String contentType) {}
}
