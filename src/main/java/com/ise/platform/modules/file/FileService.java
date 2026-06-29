package com.ise.platform.modules.file;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 30L * 1024 * 1024;
    private static final Map<Long, SeededFileRef> SEEDED_FILE_REFS = Map.ofEntries(
        Map.entry(12001L, new SeededFileRef("国家奖学金评定办法.pdf", "downloads/student/scholarship-policy.pdf", "application/pdf")),
        Map.entry(12002L, new SeededFileRef("学籍异动办理指南.docx", "downloads/student/student-status-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(12003L, new SeededFileRef("党员发展材料清单.xlsx", "downloads/student/party-materials-checklist.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
        Map.entry(12004L, new SeededFileRef("学生证明办理指南.pdf", "downloads/student/student-certificate-guide.pdf", "application/pdf")),
        Map.entry(12005L, new SeededFileRef("毕业生就业信息补录通知.docx", "downloads/student/employment-registration-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(12006L, new SeededFileRef("2024级大类培养方案（含辅修）.pdf", "downloads/student/2024级大类培养方案（含辅修）.pdf", "application/pdf")),
        Map.entry(12007L, new SeededFileRef("2025级大类培养方案.pdf", "downloads/student/2025级大类培养方案.pdf", "application/pdf")),
        Map.entry(12008L, new SeededFileRef("中国人民大学信息学院2025年综合类.pdf", "downloads/student/中国人民大学信息学院2025年综合类.pdf", "application/pdf")),
        Map.entry(12009L, new SeededFileRef("党员证明模板.docx", "downloads/student/党员证明模板.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(12010L, new SeededFileRef("团员证明模板.docx", "downloads/student/团员证明模板.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(13001L, new SeededFileRef("在读证明申请模板.docx", "downloads/student/student-certificate-application-template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(13002L, new SeededFileRef("国家奖学金材料清单模板.xlsx", "downloads/student/scholarship-materials-checklist.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
        Map.entry(13003L, new SeededFileRef("思想汇报撰写模板.docx", "downloads/student/party-report-template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(13004L, new SeededFileRef("就业信息补录说明模板.docx", "downloads/student/employment-registration-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(13005L, new SeededFileRef("党员证明模板.docx", "downloads/student/党员证明模板.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
        Map.entry(13006L, new SeededFileRef("团员证明模板.docx", "downloads/student/团员证明模板.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
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
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if ("party_material".equals(bizType) && !isPartyMaterialFile(fileName)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "party material only supports doc, docx or pdf files");
        }
        try {
            Long fileId = fileIdGenerator.incrementAndGet();
            String storedFileName = StringUtils.hasText(fileName) ? fileName : ("upload-" + fileId);
            fileStore.put(fileId, new FileEntity(
                fileId,
                storedFileName,
                file.getContentType(),
                file.getBytes(),
                bizType,
                user.getId()
            ));
            return new FileDto.UploadData(fileId, storedFileName, "/api/v1/files/" + fileId + "/download", file.getSize());
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
            if (!matchesDeclaredFileType(ref.fileName(), content)) {
                content = buildDemoFileContent(ref.fileName());
            }
            return new FileEntity(fileId, ref.fileName(), contentType, content, "demo_seeded", user.getId());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "read demo file failed");
        }
    }

    private boolean isPartyMaterialFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".doc") || lowerName.endsWith(".docx") || lowerName.endsWith(".pdf");
    }

    private boolean matchesDeclaredFileType(String fileName, byte[] content) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".docx") || lowerName.endsWith(".xlsx")) {
            return content.length >= 4
                && content[0] == 'P'
                && content[1] == 'K'
                && content[2] == 3
                && content[3] == 4;
        }
        if (lowerName.endsWith(".pdf")) {
            return content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
        }
        return true;
    }

    private byte[] buildDemoFileContent(String fileName) throws IOException {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".docx")) {
            return buildDocx(fileName);
        }
        if (lowerName.endsWith(".xlsx")) {
            return buildXlsx(fileName);
        }
        if (lowerName.endsWith(".pdf")) {
            return buildPdf(fileName);
        }
        return ("演示文件：" + fileName).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildDocx(String fileName) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addZipEntry(zip, "[Content_Types].xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                </Types>
                """);
            addZipEntry(zip, "_rels/.rels", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
                </Relationships>
                """);
            addZipEntry(zip, "word/document.xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:body>
                    <w:p><w:r><w:t>%s</w:t></w:r></w:p>
                    <w:p><w:r><w:t>该文件为学院平台演示模板，请按实际业务要求补充内容。</w:t></w:r></w:p>
                  </w:body>
                </w:document>
                """.formatted(escapeXml(fileName)));
        }
        return output.toByteArray();
    }

    private byte[] buildXlsx(String fileName) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addZipEntry(zip, "[Content_Types].xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """);
            addZipEntry(zip, "_rels/.rels", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """);
            addZipEntry(zip, "xl/workbook.xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets><sheet name="模板" sheetId="1" r:id="rId1"/></sheets>
                </workbook>
                """);
            addZipEntry(zip, "xl/_rels/workbook.xml.rels", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                </Relationships>
                """);
            addZipEntry(zip, "xl/worksheets/sheet1.xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <sheetData>
                    <row r="1"><c r="A1" t="inlineStr"><is><t>%s</t></is></c></row>
                    <row r="2"><c r="A2" t="inlineStr"><is><t>该文件为学院平台演示模板，请按实际业务要求补充内容。</t></is></c></row>
                  </sheetData>
                </worksheet>
                """.formatted(escapeXml(fileName)));
        }
        return output.toByteArray();
    }

    private byte[] buildPdf(String fileName) {
        String content = "%PDF-1.4\n"
            + "1 0 obj <</Type /Catalog /Pages 2 0 R>> endobj\n"
            + "2 0 obj <</Type /Pages /Kids [3 0 R] /Count 1>> endobj\n"
            + "3 0 obj <</Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources <</Font <</F1 5 0 R>>>>>> endobj\n"
            + "4 0 obj <</Length 83>> stream\n"
            + "BT /F1 18 Tf 72 720 Td (ISE demo template file) Tj 0 -28 Td (" + toPdfAscii(fileName) + ") Tj ET\n"
            + "endstream endobj\n"
            + "5 0 obj <</Type /Font /Subtype /Type1 /BaseFont /Helvetica>> endobj\n"
            + "trailer <</Root 1 0 R>>\n"
            + "%%EOF\n";
        return content.getBytes(StandardCharsets.US_ASCII);
    }

    private void addZipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String escapeXml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private String toPdfAscii(String value) {
        return value.replaceAll("[^\\x20-\\x7E]", "?").replace("(", "\\(").replace(")", "\\)");
    }

    public record FileEntity(Long fileId, String fileName, String contentType, byte[] content, String bizType, Long uploadedBy) {
    }

    private record SeededFileRef(String fileName, String relativePath, String contentType) {
    }
}
