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
        FileDto.UploadData data = fileService.upload(uploader, file, "party_material");
        FileService.FileEntity entity = fileService.requireFile(uploader, data.getFileId());
        assertThat(entity.fileName()).isEqualTo("a.txt");
    }

    @Test
    void otherStudentCannotDownloadWithoutPermission() {
        CurrentUser uploader = studentUser(1L, "20220001");
        CurrentUser other = studentUser(2L, "20220018");
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "abc".getBytes());
        FileDto.UploadData data = fileService.upload(uploader, file, "party_material");

        assertThatThrownBy(() -> fileService.requireFile(other, data.getFileId()))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
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
}
