package com.ise.platform.modules.honor;

public final class HonorDto {

    private HonorDto() {
    }

    public static class HonorView {
        private Long id;
        private String title;
        private String owner;
        private String year;
        private String categoryLabel;
        private String story;
        private String honorScope;

        public HonorView(Long id,
                         String title,
                         String owner,
                         String year,
                         String categoryLabel,
                         String story,
                         String honorScope) {
            this.id = id;
            this.title = title;
            this.owner = owner;
            this.year = year;
            this.categoryLabel = categoryLabel;
            this.story = story;
            this.honorScope = honorScope;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getOwner() {
            return owner;
        }

        public String getYear() {
            return year;
        }

        public String getCategoryLabel() {
            return categoryLabel;
        }

        public String getStory() {
            return story;
        }

        public String getHonorScope() {
            return honorScope;
        }
    }
}
