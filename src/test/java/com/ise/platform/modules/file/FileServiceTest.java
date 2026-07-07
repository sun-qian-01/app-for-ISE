package com.ise.platform.modules.file;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceTest {

    private final FileService fileService = new FileService();

    @Test
    void uploaderCanDownloadOwnFile() {
        CurrentUser uploader = studentUser(1L, "20220001");
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "abc".getBytes());
        FileDto.UploadData data = fileService.upload(uploader, file, "application_attachment");
        FileService.FileEntity entity = fileService.requireFile(uploader, data.getFileId());
        assertThat(entity.fileName()).isEqualTo("a.txt");
    }

    @Test
    void otherStudentCannotDownloadWithoutPermission() {
        CurrentUser uploader = studentUser(1L, "20220001");
        CurrentUser other = studentUser(2L, "20220018");
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "abc".getBytes());
        FileDto.UploadData data = fileService.upload(uploader, file, "application_attachment");

        assertThatThrownBy(() -> fileService.requireFile(other, data.getFileId()))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void knowledgeTemplateUploadShouldBeDownloadableByStudents() {
        CurrentUser teacher = teacherUser();
        CurrentUser student = studentUser(1L, "20220001");
        MockMultipartFile file = new MockMultipartFile("file", "template.pdf", "application/pdf", "%PDF".getBytes());

        FileDto.UploadData data = fileService.upload(teacher, file, "kb_template");
        FileService.FileEntity entity = fileService.requireFile(student, data.getFileId());

        assertThat(entity.fileName()).isEqualTo("template.pdf");
    }

    @Test
    void seededDocxTemplateShouldDownloadAsOfficeFile() {
        FileService.FileEntity entity = fileService.requireFile(studentUser(1L, "20220001"), 13001L);

        assertThat(entity.fileName()).endsWith(".docx");
        assertThat(entity.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(entity.content()).startsWith(new byte[] { 'P', 'K', 3, 4 });
    }

    @Test
    void seededXlsxTemplateShouldDownloadAsOfficeFile() {
        FileService.FileEntity entity = fileService.requireFile(studentUser(1L, "20220001"), 13002L);

        assertThat(entity.fileName()).endsWith(".xlsx");
        assertThat(entity.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(entity.content()).startsWith(new byte[] { 'P', 'K', 3, 4 });
    }

    @Test
    void seededPdfSourceShouldDownloadAsPdfFile() {
        FileService.FileEntity entity = fileService.requireFile(studentUser(1L, "20220001"), 12001L);

        assertThat(entity.fileName()).endsWith(".pdf");
        assertThat(entity.contentType()).isEqualTo("application/pdf");
        assertThat(entity.content()).startsWith(new byte[] { '%', 'P', 'D', 'F' });
    }

    @Test
    void partyMaterialShouldOnlyAllowWordOrPdfFiles() {
        CurrentUser uploader = studentUser(1L, "20220001");
        MockMultipartFile pdf = new MockMultipartFile("file", "思想汇报.pdf", "application/pdf", "%PDF".getBytes());
        MockMultipartFile docx = new MockMultipartFile(
            "file",
            "思想汇报.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[] { 'P', 'K', 3, 4 }
        );
        MockMultipartFile txt = new MockMultipartFile("file", "思想汇报.txt", "text/plain", "abc".getBytes());

        assertThat(fileService.upload(uploader, pdf, "party_material").getFileName()).isEqualTo("思想汇报.pdf");
        assertThat(fileService.upload(uploader, docx, "party_material").getFileName()).isEqualTo("思想汇报.docx");
        assertThatThrownBy(() -> fileService.upload(uploader, txt, "party_material"))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    private CurrentUser studentUser(Long userId, String username) {
        return new CurrentUser(
            userId,
            username,
            "测试用户",
            "student",
            List.of("student"),
            List.of("file:upload"),
            List.of(new DataScope("self", String.valueOf(userId))),
            userId
        );
    }

    private CurrentUser teacherUser() {
        return new CurrentUser(
            8L,
            "teacher001",
            "李老师",
            "teacher",
            List.of("teacher_admin"),
            List.of("file:upload"),
            List.of(new DataScope("class", "软件工程2班")),
            null
        );
    }
}
