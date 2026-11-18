package com.brandon.finance.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.brandon.finance.shared.base.excepetion.BusinessException;

class AvatarImageValidatorTest {
    private final AvatarImageValidator validator = new AvatarImageValidator("2MB");

    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png", "webp"})
    void detectsRealFormatAndReencodesIndependentlyOfReportedContentType(String format) throws IOException {
        byte[] input = format.equals("webp")
            ? Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEAAUAmJaQAA3AA/v89WAAAAA==")
            : image(format, 1);
        var result = validator.normalize(new MockMultipartFile("image", "../photo.gif", "text/plain", input));

        assertThat(result.contentType()).isEqualTo(format.equals("jpeg") ? "image/jpeg" : "image/png");
        assertThat(ImageIO.read(new ByteArrayInputStream(result.bytes())).getWidth()).isEqualTo(1);
    }

    @Test
    void rejectsEmptySpoofedTruncatedAndOversizedDimensions() throws IOException {
        byte[] png = image("png", 8);
        for (byte[] invalid : new byte[][]{new byte[0], new byte[]{1, 2, 3},
                java.util.Arrays.copyOf(png, 30), image("png", 2049)}) {
            assertThatThrownBy(() -> validator.normalize(new MockMultipartFile("image", "avatar.png", "image/png", invalid)))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Test
    void appliesConfiguredByteLimitBeforeDecoding() {
        var smallLimit = new AvatarImageValidator("2B");
        assertThatThrownBy(() -> smallLimit.normalize(new MockMultipartFile("image", new byte[3])))
            .isInstanceOf(MaxUploadSizeExceededException.class);
    }

    private byte[] image(String format, int width) throws IOException {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB), format, bytes);
        return bytes.toByteArray();
    }
}
