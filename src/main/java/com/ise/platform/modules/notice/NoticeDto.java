package com.ise.platform.modules.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public final class NoticeDto {

    private NoticeDto() {
    }

    public static class NoticeView {
        private Long id;
        private String title;
        private String content;
        private List<String> tags;
        private List<String> channelLabels;
        private String audience;
        private int deliveredCount;
        private int readCount;
        private int unreadCount;
        private String publishAt;
        private String readStatus;

        public NoticeView(Long id,
                          String title,
                          String content,
                          List<String> tags,
                          List<String> channelLabels,
                          String audience,
                          int deliveredCount,
                          int readCount,
                          String publishAt,
                          String readStatus) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.tags = tags;
            this.channelLabels = channelLabels;
            this.audience = audience;
            this.readCount = Math.max(readCount, 0);
            this.deliveredCount = Math.max(deliveredCount, this.readCount);
            this.unreadCount = this.deliveredCount - this.readCount;
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

        public List<String> getChannelLabels() {
            return channelLabels;
        }

        public String getAudience() {
            return audience;
        }

        public int getDeliveredCount() {
            return deliveredCount;
        }

        public int getReadCount() {
            return readCount;
        }

        public int getUnreadCount() {
            return unreadCount;
        }

        public String getPublishAt() {
            return publishAt;
        }

        public String getReadStatus() {
            return readStatus;
        }
    }

    public static class CreateNoticeRequest {
        @NotBlank(message = "title is required")
        private String title;

        @NotBlank(message = "content is required")
        private String content;

        @NotBlank(message = "audience is required")
        private String audience;

        @NotEmpty(message = "channelLabels is required")
        private List<String> channelLabels;

        private List<String> tags;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public List<String> getChannelLabels() {
            return channelLabels;
        }

        public void setChannelLabels(List<String> channelLabels) {
            this.channelLabels = channelLabels;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
}
