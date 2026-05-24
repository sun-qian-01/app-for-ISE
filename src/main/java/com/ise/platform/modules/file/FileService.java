package com.ise.platform.modules.file;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 30L * 1024 * 1024;
    private static final Map<Long, SeededFileRef> SEEDED_FILE_REFS = Map.of(
        12001L, new SeededFileRef("国家奖学金评定办法.pdf", "downloads/student/scholarship-policy.pdf", "application/pdf"),
        12002L, new SeededFileRef("学籍异动办理指南.docx", "downloads/student/student-status-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        12003L, new SeededFileRef("党员发展材料清单.xlsx", "downloads/student/party-materials-checklist.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        12004L, new SeededFileRef("学生证明办理指南.pdf", "downloads/student/student-certificate-guide.pdf", "application/pdf"),
        12005L, new SeededFileRef("毕业生就业信息补录通知.docx", "downloads/student/employment-registration-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        13001L, new SeededFileRef("在读证明申请模板.docx", "downloads/student/student-certificate-application-template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        13002L, new SeededFileRef("国家奖学金材料清单模板.xlsx", "downloads/student/scholarship-materials-checklist.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        13003L, new SeededFileRef("思想汇报撰写模板.docx", "downloads/student/party-report-template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        13004L, new SeededFileRef("就业信息补录说明模板.docx", "downloads/student/employment-registration-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    );

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
        if (file != null) {
            // Demo rule: uploader and admins can download uploaded attachments.
            boolean isOwner = file.uploadedBy().equals(user.getId());
            boolean isManager = user.getRoles().stream().anyMatch(role ->
                "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
            if (!isOwner && !isManager) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to download this file");
            }
            return file;
        }

        FileEntity seededFile = loadSeededFile(fileId, user);
        if (seededFile != null) {
            return seededFile;
        }

        // Last-resort fallback so unknown demo IDs still remain downloadable in preview.
        String placeholderName = "demo-file-" + fileId + ".txt";
        byte[] content = ("该文件为演示占位内容，fileId=" + fileId).getBytes();
        return new FileEntity(fileId, placeholderName, "text/plain;charset=UTF-8", content, "demo_seeded", user.getId());
    }

    private FileEntity loadSeededFile(Long fileId, CurrentUser user) {
        SeededFileRef ref = SEEDED_FILE_REFS.get(fileId);
        if (ref == null) {
            return null;
        }

        Path path = Path.of(ref.relativePath());
        try {
            if (!Files.exists(path)) {
                return null;
            }
            byte[] content = Files.readAllBytes(path);
            String contentType = ref.contentType();
            if (!StringUtils.hasText(contentType)) {
                contentType = Files.probeContentType(path);
            }
            if (!StringUtils.hasText(contentType)) {
                contentType = "application/octet-stream";
            }
            return new FileEntity(fileId, ref.fileName(), contentType, content, "demo_seeded", user.getId());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "read demo file failed");
        }
    }

    public record FileEntity(Long fileId, String fileName, String contentType, byte[] content, String bizType, Long uploadedBy) {
    }

    private record SeededFileRef(String fileName, String relativePath, String contentType) {
    }
}
