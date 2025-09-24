package com.example.learning.storage;

import com.example.learning.common.exception.BizException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;

/**
 * 文件存储服务：将上传文件保存到本地磁盘，并返回可访问路径。
 */
@Service
public class FileStorageService {

    private final Path root;
    private final String publicPrefix;
    private final Set<String> whitelist = Set.of("mp4", "pdf", "docx", "png", "jpg", "jpeg");
    private final DataSize maxSize;

    public FileStorageService(@Value("${storage.local-path}") String root,
                              @Value("${storage.public-prefix}") String publicPrefix,
                              @Value("${spring.servlet.multipart.max-file-size}") DataSize maxSize) {
        this.root = Paths.get(root);
        this.publicPrefix = publicPrefix;
        this.maxSize = maxSize;
    }

    /**
     * 保存文件并返回访问 URL。
     */
    public StoredFile store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException(400, "文件不能为空");
        }
        if (file.getSize() > maxSize.toBytes()) {
            throw new BizException(400, "文件过大");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (ext == null || !whitelist.contains(ext.toLowerCase())) {
            throw new BizException(400, "文件类型不允许");
        }
        try {
            LocalDate today = LocalDate.now();
            Path dir = root.resolve(today.toString());
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            file.transferTo(target);
            String url = publicPrefix + today + "/" + filename;
            return new StoredFile(url, file.getSize(), ext);
        } catch (IOException e) {
            throw new BizException(500, "文件保存失败");
        }
    }

    /**
     * 存储结果对象。
     */
    public record StoredFile(String url, long size, String type) {
    }
}