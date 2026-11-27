package vn.edu.iuh.keymech.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.exception.BusinessException;

@Component
@Slf4j
public class ProductImageStorage {
    private static final String URL_PREFIX = "/uploads/";
    private static final Pattern GENERATED_IMAGE = Pattern.compile(
            "/uploads/[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.(jpg|png|webp)");
    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private final Path directory;

    public ProductImageStorage(AppProperties properties) {
        directory = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image, String previousUrl) {
        if (image.getSize() > MAX_BYTES) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "Dung lượng ảnh tối đa là 2 MB.");
        }

        Path target = null;
        try {
            byte[] bytes = image.getBytes();
            String extension = extension(image.getContentType(), bytes);
            String name = UUID.randomUUID() + extension;
            Files.createDirectories(directory);
            Path destination = directory.resolve(name);
            Files.createFile(destination);
            target = destination;
            Files.write(target, bytes, StandardOpenOption.WRITE);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) delete(destination);
                    if (status == STATUS_COMMITTED && previousUrl != null
                            && GENERATED_IMAGE.matcher(previousUrl).matches()) {
                        delete(directory.resolve(previousUrl.substring(URL_PREFIX.length())));
                    }
                }
            });
            return URL_PREFIX + name;
        } catch (IOException e) {
            if (target != null) delete(target);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_STORAGE_ERROR", "Không thể lưu ảnh sản phẩm.");
        }
    }

    private String extension(String type, byte[] bytes) {
        if ("image/jpeg".equals(type) && matches(bytes, 0, 0xff, 0xd8, 0xff)) return ".jpg";
        if ("image/png".equals(type) && matches(bytes, 0, 0x89, 'P', 'N', 'G', 13, 10, 26, 10)) return ".png";
        if ("image/webp".equals(type) && matches(bytes, 0, 'R', 'I', 'F', 'F')
                && matches(bytes, 8, 'W', 'E', 'B', 'P')) return ".webp";
        throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "Ảnh phải có định dạng JPG, PNG hoặc WebP.");
    }

    private boolean matches(byte[] bytes, int offset, int... signature) {
        if (bytes.length < offset + signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if (Byte.toUnsignedInt(bytes[offset + i]) != signature[i]) return false;
        }
        return true;
    }

    private void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Không thể dọn ảnh sản phẩm {}", path.getFileName(), e);
        }
    }
}
