package com.ise.platform.modules.audit;

public class AuditLogDto {

    public static class AuditLogView {
        private Long id;
        private String actor;
        private String module;
        private String action;
        private String time;
        private String result;

        public AuditLogView(Long id, String actor, String module, String action, String time, String result) {
            this.id = id;
            this.actor = actor;
            this.module = module;
            this.action = action;
            this.time = time;
            this.result = result;
        }

        public Long getId() {
            return id;
        }

        public String getActor() {
            return actor;
        }

        public String getModule() {
            return module;
        }

        public String getAction() {
            return action;
        }

        public String getTime() {
            return time;
        }

        public String getResult() {
            return result;
        }
    }
}
