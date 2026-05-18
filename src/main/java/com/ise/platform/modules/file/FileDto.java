package com.ise.platform.modules.file;

public final class FileDto {

    private FileDto() {
    }

    public static class UploadData {
        private Long fileId;
        private String fileName;
        private String fileUrl;
        private long fileSize;

        public UploadData(Long fileId, String fileName, String fileUrl, long fileSize) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.fileSize = fileSize;
        }

        public Long getFileId() {
            return fileId;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public long getFileSize() {
            return fileSize;
        }
    }
}
