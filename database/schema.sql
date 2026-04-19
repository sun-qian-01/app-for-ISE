-- 学院学生综合服务与党团管理平台
-- 核心数据库表结构草案
-- 面向 Kingbase / PostgreSQL 兼容语法

create table if not exists sys_user (
    id bigserial primary key,
    username varchar(64) not null unique,
    password_hash varchar(255) not null,
    real_name varchar(64) not null,
    user_type varchar(32) not null,
    student_id bigint,
    employee_no varchar(64),
    phone varchar(32),
    email varchar(128),
    status varchar(32) not null default 'enabled',
    last_login_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists sys_role (
    id bigserial primary key,
    role_code varchar(64) not null unique,
    role_name varchar(64) not null,
    data_scope varchar(32) not null default 'self',
    description varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists sys_permission (
    id bigserial primary key,
    permission_code varchar(128) not null unique,
    permission_name varchar(128) not null,
    permission_type varchar(32) not null,
    parent_id bigint,
    path varchar(255),
    method varchar(16),
    sort_no integer not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists sys_user_role (
    id bigserial primary key,
    user_id bigint not null,
    role_id bigint not null,
    created_at timestamp not null default current_timestamp,
    unique (user_id, role_id)
);

create table if not exists sys_role_permission (
    id bigserial primary key,
    role_id bigint not null,
    permission_id bigint not null,
    created_at timestamp not null default current_timestamp,
    unique (role_id, permission_id)
);

create table if not exists stu_student (
    id bigserial primary key,
    student_no varchar(32) not null unique,
    name varchar(64) not null,
    gender varchar(16),
    id_card_no varchar(128),
    phone varchar(32),
    email varchar(128),
    grade varchar(16) not null,
    major varchar(64) not null,
    class_name varchar(64) not null,
    degree_level varchar(32),
    counselor_name varchar(64),
    political_status varchar(32),
    enrollment_date date,
    status varchar(32) not null default 'active',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists stu_student_ext (
    id bigserial primary key,
    student_id bigint not null unique,
    native_place varchar(128),
    dormitory varchar(64),
    tutor_name varchar(64),
    family_economic_level varchar(32),
    employment_intention varchar(128),
    remark text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists stu_tag (
    id bigserial primary key,
    tag_code varchar(64) not null unique,
    tag_name varchar(64) not null,
    tag_type varchar(32) not null,
    description varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists stu_student_tag (
    id bigserial primary key,
    student_id bigint not null,
    tag_id bigint not null,
    created_at timestamp not null default current_timestamp,
    unique (student_id, tag_id)
);

create table if not exists stu_change_log (
    id bigserial primary key,
    student_id bigint not null,
    change_type varchar(32) not null,
    before_status varchar(32),
    after_status varchar(32),
    change_date date not null,
    reason varchar(255),
    operator_user_id bigint,
    created_at timestamp not null default current_timestamp
);

create table if not exists kb_category (
    id bigserial primary key,
    category_name varchar(64) not null,
    parent_id bigint,
    sort_no integer not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_article (
    id bigserial primary key,
    category_id bigint not null,
    title varchar(255) not null,
    summary varchar(500),
    content text not null,
    keywords varchar(255),
    source_type varchar(32) not null default 'manual',
    source_url varchar(500),
    publish_status varchar(32) not null default 'draft',
    view_count integer not null default 0,
    created_by bigint not null,
    published_by bigint,
    published_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_attachment (
    id bigserial primary key,
    article_id bigint not null,
    file_name varchar(255) not null,
    file_url varchar(500) not null,
    file_size bigint,
    created_at timestamp not null default current_timestamp
);

create table if not exists msg_notice (
    id bigserial primary key,
    title varchar(255) not null,
    content text not null,
    notice_type varchar(32) not null,
    status varchar(32) not null default 'draft',
    publish_at timestamp,
    expire_at timestamp,
    created_by bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists msg_notice_scope (
    id bigserial primary key,
    notice_id bigint not null,
    scope_type varchar(32) not null,
    scope_value varchar(128) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists msg_notice_user (
    id bigserial primary key,
    notice_id bigint not null,
    user_id bigint not null,
    read_status varchar(32) not null default 'unread',
    read_at timestamp,
    delivery_status varchar(32) not null default 'delivered',
    created_at timestamp not null default current_timestamp,
    unique (notice_id, user_id)
);

create table if not exists flow_party_progress (
    id bigserial primary key,
    student_id bigint not null unique,
    current_stage varchar(32) not null,
    current_status varchar(32) not null default 'processing',
    branch_name varchar(128),
    owner_user_id bigint,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists flow_party_record (
    id bigserial primary key,
    progress_id bigint not null,
    stage_name varchar(64) not null,
    stage_order integer not null,
    status varchar(32) not null,
    submit_time timestamp,
    complete_time timestamp,
    reviewer_user_id bigint,
    review_comment varchar(500),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists cert_template (
    id bigserial primary key,
    template_code varchar(64) not null unique,
    template_name varchar(128) not null,
    template_type varchar(32) not null,
    template_content text not null,
    status varchar(32) not null default 'enabled',
    created_by bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists cert_application (
    id bigserial primary key,
    application_no varchar(64) not null unique,
    template_id bigint not null,
    student_id bigint not null,
    purpose varchar(255),
    status varchar(32) not null default 'submitted',
    current_approver_id bigint,
    submitted_at timestamp not null default current_timestamp,
    finished_at timestamp,
    revoke_reason varchar(255),
    extra_data text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists cert_approval_record (
    id bigserial primary key,
    application_id bigint not null,
    approver_user_id bigint not null,
    action_type varchar(32) not null,
    action_comment varchar(500),
    action_time timestamp not null default current_timestamp
);

create table if not exists audit_log (
    id bigserial primary key,
    user_id bigint,
    module_code varchar(64) not null,
    action_code varchar(64) not null,
    target_type varchar(64),
    target_id bigint,
    request_method varchar(16),
    request_path varchar(255),
    request_ip varchar(64),
    result_code varchar(32),
    result_message varchar(255),
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_sys_user_student_id on sys_user(student_id);
create index if not exists idx_student_grade_major_class on stu_student(grade, major, class_name);
create index if not exists idx_kb_article_category_status on kb_article(category_id, publish_status);
create index if not exists idx_msg_notice_status_publish on msg_notice(status, publish_at);
create index if not exists idx_msg_notice_user_user_read on msg_notice_user(user_id, read_status);
create index if not exists idx_party_record_progress_order on flow_party_record(progress_id, stage_order);
create index if not exists idx_cert_application_student_status on cert_application(student_id, status);
create index if not exists idx_cert_application_approver_status on cert_application(current_approver_id, status);
create index if not exists idx_audit_log_user_created on audit_log(user_id, created_at);

alter table sys_user
    add constraint fk_sys_user_student
    foreign key (student_id) references stu_student(id);

alter table sys_user_role
    add constraint fk_user_role_user
    foreign key (user_id) references sys_user(id);

alter table sys_user_role
    add constraint fk_user_role_role
    foreign key (role_id) references sys_role(id);

alter table sys_role_permission
    add constraint fk_role_permission_role
    foreign key (role_id) references sys_role(id);

alter table sys_role_permission
    add constraint fk_role_permission_permission
    foreign key (permission_id) references sys_permission(id);

alter table stu_student_ext
    add constraint fk_student_ext_student
    foreign key (student_id) references stu_student(id);

alter table stu_student_tag
    add constraint fk_student_tag_student
    foreign key (student_id) references stu_student(id);

alter table stu_student_tag
    add constraint fk_student_tag_tag
    foreign key (tag_id) references stu_tag(id);

alter table stu_change_log
    add constraint fk_change_log_student
    foreign key (student_id) references stu_student(id);

alter table kb_article
    add constraint fk_kb_article_category
    foreign key (category_id) references kb_category(id);

alter table kb_attachment
    add constraint fk_kb_attachment_article
    foreign key (article_id) references kb_article(id);

alter table msg_notice_scope
    add constraint fk_notice_scope_notice
    foreign key (notice_id) references msg_notice(id);

alter table msg_notice_user
    add constraint fk_notice_user_notice
    foreign key (notice_id) references msg_notice(id);

alter table msg_notice_user
    add constraint fk_notice_user_user
    foreign key (user_id) references sys_user(id);

alter table flow_party_progress
    add constraint fk_party_progress_student
    foreign key (student_id) references stu_student(id);

alter table flow_party_record
    add constraint fk_party_record_progress
    foreign key (progress_id) references flow_party_progress(id);

alter table cert_application
    add constraint fk_cert_application_template
    foreign key (template_id) references cert_template(id);

alter table cert_application
    add constraint fk_cert_application_student
    foreign key (student_id) references stu_student(id);

alter table cert_approval_record
    add constraint fk_cert_approval_application
    foreign key (application_id) references cert_application(id);
