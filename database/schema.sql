-- 学院学生综合服务与党团管理平台
-- Kingbase / PostgreSQL 兼容数据库基线
-- 约定：
-- 1. 所有核心业务表使用 bigint 主键、created_at、updated_at、is_deleted。
-- 2. 枚举值由后端常量统一约束，数据库使用 varchar 便于 Kingbase 兼容和后续扩展。
-- 3. 敏感字段可以在应用层加密后写入数据库，前端默认只展示脱敏值。

create table if not exists sys_user (
    id bigserial primary key,
    username varchar(64) not null unique,
    password_hash varchar(255) not null,
    real_name varchar(64) not null,
    user_type varchar(32) not null,
    student_id bigint,
    employee_no varchar(64),
    phone varchar(128),
    email varchar(128),
    avatar_url varchar(500),
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
    user_id bigint not null references sys_user(id),
    role_id bigint not null references sys_role(id),
    created_at timestamp not null default current_timestamp,
    unique (user_id, role_id)
);

create table if not exists sys_role_permission (
    id bigserial primary key,
    role_id bigint not null references sys_role(id),
    permission_id bigint not null references sys_permission(id),
    created_at timestamp not null default current_timestamp,
    unique (role_id, permission_id)
);

create table if not exists sys_user_scope (
    id bigserial primary key,
    user_id bigint not null references sys_user(id),
    scope_type varchar(32) not null,
    scope_value varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    unique (user_id, scope_type, scope_value)
);

create table if not exists file_resource (
    id bigserial primary key,
    biz_type varchar(64) not null,
    biz_id bigint,
    file_name varchar(255) not null,
    original_name varchar(255) not null,
    file_ext varchar(32),
    mime_type varchar(128),
    file_size bigint not null,
    file_url varchar(500) not null,
    sha256 varchar(128),
    uploaded_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists stu_student (
    id bigserial primary key,
    student_no varchar(32) not null unique,
    name varchar(64) not null,
    gender varchar(16),
    nation varchar(32),
    id_card_no varchar(255),
    phone varchar(128),
    email varchar(128),
    grade varchar(16) not null,
    major varchar(64) not null,
    class_name varchar(64) not null,
    degree_level varchar(32),
    political_status varchar(32),
    enrollment_date date,
    graduation_year varchar(16),
    counselor_user_id bigint references sys_user(id),
    status varchar(32) not null default 'active',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists stu_student_ext (
    id bigserial primary key,
    student_id bigint not null unique references stu_student(id),
    native_place varchar(255),
    household_address varchar(255),
    dormitory varchar(64),
    tutor_name varchar(64),
    family_economic_level varchar(32),
    employment_intention varchar(128),
    study_change_record text,
    sensitive_remark text,
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
    student_id bigint not null references stu_student(id),
    tag_id bigint not null references stu_tag(id),
    source_type varchar(32) not null default 'manual',
    created_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    unique (student_id, tag_id)
);

create table if not exists stu_growth_record (
    id bigserial primary key,
    student_id bigint not null references stu_student(id),
    record_type varchar(32) not null,
    title varchar(255) not null,
    org_name varchar(128),
    role_name varchar(128),
    start_date date,
    end_date date,
    description text,
    proof_file_id bigint references file_resource(id),
    audit_status varchar(32) not null default 'approved',
    created_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists stu_import_task (
    id bigserial primary key,
    task_no varchar(64) not null unique,
    import_type varchar(64) not null,
    file_id bigint references file_resource(id),
    status varchar(32) not null default 'pending',
    total_count integer not null default 0,
    success_count integer not null default 0,
    fail_count integer not null default 0,
    error_file_id bigint references file_resource(id),
    created_by bigint not null references sys_user(id),
    created_at timestamp not null default current_timestamp,
    finished_at timestamp
);

create table if not exists honor_record (
    id bigserial primary key,
    honor_type varchar(32) not null,
    honor_category varchar(64) not null,
    title varchar(255) not null,
    award_year varchar(16) not null,
    owner_type varchar(32) not null,
    owner_student_id bigint references stu_student(id),
    owner_name varchar(128) not null,
    story text,
    cover_file_id bigint references file_resource(id),
    public_status varchar(32) not null default 'hidden',
    display_order integer not null default 0,
    display_start_at timestamp,
    display_end_at timestamp,
    created_by bigint not null references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_category (
    id bigserial primary key,
    category_name varchar(64) not null,
    parent_id bigint references kb_category(id),
    sort_no integer not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_article (
    id bigserial primary key,
    category_id bigint not null references kb_category(id),
    title varchar(255) not null,
    summary varchar(500),
    standard_answer text,
    content text not null,
    keywords varchar(255),
    source_type varchar(32) not null default 'manual',
    source_file_id bigint references file_resource(id),
    source_url varchar(500),
    version_no varchar(32) not null default 'v1',
    publish_status varchar(32) not null default 'draft',
    effective_at timestamp,
    expire_at timestamp,
    view_count integer not null default 0,
    created_by bigint not null references sys_user(id),
    published_by bigint references sys_user(id),
    published_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_article_version (
    id bigserial primary key,
    article_id bigint not null references kb_article(id),
    version_no varchar(32) not null,
    title varchar(255) not null,
    summary varchar(500),
    standard_answer text,
    content text not null,
    source_file_id bigint references file_resource(id),
    change_note varchar(500),
    created_by bigint not null references sys_user(id),
    created_at timestamp not null default current_timestamp,
    unique (article_id, version_no)
);

create table if not exists kb_template (
    id bigserial primary key,
    template_code varchar(64) not null unique,
    template_name varchar(128) not null,
    template_type varchar(32) not null,
    category_id bigint references kb_category(id),
    file_id bigint not null references file_resource(id),
    description varchar(500),
    status varchar(32) not null default 'enabled',
    download_count integer not null default 0,
    created_by bigint not null references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists kb_question_log (
    id bigserial primary key,
    user_id bigint references sys_user(id),
    question text not null,
    answer text,
    source_article_ids varchar(500),
    confidence numeric(5, 2),
    feedback varchar(32),
    created_at timestamp not null default current_timestamp
);

create table if not exists flow_definition (
    id bigserial primary key,
    flow_code varchar(64) not null unique,
    flow_name varchar(128) not null,
    flow_type varchar(32) not null,
    status varchar(32) not null default 'enabled',
    created_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists flow_stage_definition (
    id bigserial primary key,
    flow_id bigint not null references flow_definition(id),
    stage_code varchar(64) not null,
    stage_name varchar(128) not null,
    stage_order integer not null,
    required_days integer,
    required_material_desc text,
    task_desc text,
    reminder_days_before integer,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique (flow_id, stage_code),
    unique (flow_id, stage_order)
);

create table if not exists flow_instance (
    id bigserial primary key,
    flow_id bigint not null references flow_definition(id),
    student_id bigint not null references stu_student(id),
    current_stage_id bigint references flow_stage_definition(id),
    instance_status varchar(32) not null default 'processing',
    branch_name varchar(128),
    owner_user_id bigint references sys_user(id),
    started_at timestamp not null default current_timestamp,
    finished_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists flow_stage_record (
    id bigserial primary key,
    instance_id bigint not null references flow_instance(id),
    stage_id bigint not null references flow_stage_definition(id),
    stage_status varchar(32) not null default 'pending',
    due_at timestamp,
    submitted_at timestamp,
    reviewed_at timestamp,
    reviewer_user_id bigint references sys_user(id),
    review_comment varchar(500),
    returned_reason varchar(500),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique (instance_id, stage_id)
);

create table if not exists flow_material (
    id bigserial primary key,
    stage_record_id bigint not null references flow_stage_record(id),
    material_name varchar(128) not null,
    file_id bigint references file_resource(id),
    submitter_user_id bigint references sys_user(id),
    submit_status varchar(32) not null default 'submitted',
    review_status varchar(32) not null default 'pending',
    review_comment varchar(500),
    submitted_at timestamp not null default current_timestamp,
    reviewed_at timestamp,
    is_deleted smallint not null default 0
);

create table if not exists flow_action_log (
    id bigserial primary key,
    instance_id bigint not null references flow_instance(id),
    stage_record_id bigint references flow_stage_record(id),
    operator_user_id bigint not null references sys_user(id),
    action_type varchar(32) not null,
    action_comment varchar(500),
    before_status varchar(32),
    after_status varchar(32),
    action_time timestamp not null default current_timestamp
);

create table if not exists flow_exam_question (
    id bigserial primary key,
    question_type varchar(32) not null,
    question_text text not null,
    options_json text,
    answer_text text not null,
    analysis text,
    status varchar(32) not null default 'enabled',
    created_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists msg_notice (
    id bigserial primary key,
    title varchar(255) not null,
    content text not null,
    notice_type varchar(32) not null,
    tags varchar(255),
    status varchar(32) not null default 'draft',
    publish_at timestamp,
    expire_at timestamp,
    created_by bigint not null references sys_user(id),
    published_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists msg_notice_attachment (
    id bigserial primary key,
    notice_id bigint not null references msg_notice(id),
    file_id bigint not null references file_resource(id),
    created_at timestamp not null default current_timestamp
);

create table if not exists msg_notice_scope (
    id bigserial primary key,
    notice_id bigint not null references msg_notice(id),
    scope_type varchar(32) not null,
    scope_value varchar(128) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists msg_notice_user (
    id bigserial primary key,
    notice_id bigint not null references msg_notice(id),
    user_id bigint not null references sys_user(id),
    read_status varchar(32) not null default 'unread',
    read_at timestamp,
    delivery_status varchar(32) not null default 'pending',
    created_at timestamp not null default current_timestamp,
    unique (notice_id, user_id)
);

create table if not exists msg_delivery_record (
    id bigserial primary key,
    notice_id bigint not null references msg_notice(id),
    user_id bigint not null references sys_user(id),
    channel varchar(32) not null,
    send_status varchar(32) not null default 'pending',
    provider_message_id varchar(128),
    error_message varchar(500),
    sent_at timestamp,
    created_at timestamp not null default current_timestamp
);

create table if not exists cert_template (
    id bigserial primary key,
    template_code varchar(64) not null unique,
    template_name varchar(128) not null,
    template_type varchar(32) not null,
    file_id bigint references file_resource(id),
    template_content text,
    form_schema_json text,
    flow_rule_json text,
    status varchar(32) not null default 'enabled',
    created_by bigint not null references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists biz_application (
    id bigserial primary key,
    application_no varchar(64) not null unique,
    application_type varchar(32) not null,
    template_id bigint references cert_template(id),
    applicant_user_id bigint not null references sys_user(id),
    student_id bigint references stu_student(id),
    title varchar(255) not null,
    purpose varchar(255),
    form_data_json text,
    generated_file_id bigint references file_resource(id),
    status varchar(32) not null default 'submitted',
    current_approver_id bigint references sys_user(id),
    submitted_at timestamp not null default current_timestamp,
    finished_at timestamp,
    revoke_deadline_at timestamp,
    revoke_reason varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists biz_approval_record (
    id bigserial primary key,
    application_id bigint not null references biz_application(id),
    approver_user_id bigint not null references sys_user(id),
    action_type varchar(32) not null,
    action_comment varchar(500),
    before_status varchar(32),
    after_status varchar(32),
    action_time timestamp not null default current_timestamp
);

create table if not exists academic_program (
    id bigserial primary key,
    grade varchar(16) not null,
    major varchar(64) not null,
    program_name varchar(128) not null,
    requirement_json text not null,
    status varchar(32) not null default 'enabled',
    created_by bigint references sys_user(id),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists academic_warning (
    id bigserial primary key,
    student_id bigint not null references stu_student(id),
    warning_type varchar(32) not null,
    warning_level varchar(32) not null,
    summary varchar(500) not null,
    source_file_id bigint references file_resource(id),
    status varchar(32) not null default 'open',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted smallint not null default 0
);

create table if not exists audit_log (
    id bigserial primary key,
    user_id bigint references sys_user(id),
    module_code varchar(64) not null,
    action_code varchar(64) not null,
    target_type varchar(64),
    target_id bigint,
    request_method varchar(16),
    request_path varchar(255),
    request_ip varchar(64),
    request_body_digest varchar(128),
    result_code varchar(32),
    result_message varchar(255),
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_sys_user_student_id on sys_user(student_id);
create index if not exists idx_sys_user_type_status on sys_user(user_type, status);
create index if not exists idx_user_scope_user_type on sys_user_scope(user_id, scope_type);
create index if not exists idx_file_biz on file_resource(biz_type, biz_id);
create index if not exists idx_student_grade_major_class on stu_student(grade, major, class_name);
create index if not exists idx_student_political_status on stu_student(political_status);
create index if not exists idx_student_tag_student on stu_student_tag(student_id);
create index if not exists idx_growth_student_type on stu_growth_record(student_id, record_type);
create index if not exists idx_honor_public_year on honor_record(public_status, award_year);
create index if not exists idx_kb_article_category_status on kb_article(category_id, publish_status);
create index if not exists idx_kb_article_keywords on kb_article(keywords);
create index if not exists idx_flow_instance_student on flow_instance(student_id, instance_status);
create index if not exists idx_flow_stage_record_instance on flow_stage_record(instance_id, stage_status);
create index if not exists idx_flow_material_record on flow_material(stage_record_id, review_status);
create index if not exists idx_msg_notice_status_publish on msg_notice(status, publish_at);
create index if not exists idx_msg_notice_user_user_read on msg_notice_user(user_id, read_status);
create index if not exists idx_delivery_notice_channel on msg_delivery_record(notice_id, channel);
create index if not exists idx_biz_application_student_status on biz_application(student_id, status);
create index if not exists idx_biz_application_approver_status on biz_application(current_approver_id, status);
create index if not exists idx_audit_log_user_created on audit_log(user_id, created_at);
create index if not exists idx_audit_log_target on audit_log(target_type, target_id);
