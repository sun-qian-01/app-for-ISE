package com.ise.platform.modules.notice;

import java.util.List;

public final class NoticeDto {

    private NoticeDto() {
    }

    public static class NoticeView {
        private Long id;
        private String title;
        private String content;
        private List<String> tags;
        private String publishAt;
        private String readStatus;

        public NoticeView(Long id, String title, String content, List<String> tags, String publishAt, String readStatus) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.tags = tags;
            this.publishAt = publishAt;
            this.readStatus = readStatus;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getPublishAt() {
            return publishAt;
        }

        public String getReadStatus() {
            return readStatus;
        }
    }
}
