package com.ise.platform.modules.file;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 30L * 1024 * 1024;
    private final AtomicLong fileIdGenerator = new AtomicLong(1000);
    private final Map<Long, FileEntity> fileStore = new ConcurrentHashMap<>();

    public FileDto.UploadData upload(CurrentUser user, MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "file is required");
        }
        if (!StringUtils.hasText(bizType)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "bizType is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, "file exceeds 30MB limit");
        }
        try {
            Long fileId = fileIdGenerator.incrementAndGet();
            String fileName = file.getOriginalFilename() == null ? ("upload-" + fileId) : file.getOriginalFilename();
            fileStore.put(fileId, new FileEntity(
                fileId,
                fileName,
                file.getContentType(),
                file.getBytes(),
                bizType,
                user.getId()
            ));
            return new FileDto.UploadData(fileId, fileName, "/api/v1/files/" + fileId + "/download", file.getSize());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "read upload file failed");
        }
    }

    public FileEntity requireFile(CurrentUser user, Long fileId) {
        FileEntity file = fileStore.get(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "file not found");
        }
        // Demo rule: uploader and admins can download.
        boolean isOwner = file.uploadedBy().equals(user.getId());
        boolean isManager = user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
        if (!isOwner && !isManager) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to download this file");
        }
        return file;
    }

    public record FileEntity(Long fileId, String fileName, String contentType, byte[] content, String bizType, Long uploadedBy) {
    }
}
