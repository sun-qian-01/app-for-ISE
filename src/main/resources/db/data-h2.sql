merge into stu_student (
    id, student_no, name, phone, email, grade, major, class_name, political_status, status
) key(id) values
    (1, '20220001', '赵晨曦', '13800181234', 'zhaochenxi@example.edu.cn', '2022', '软件工程', '软件工程2班', '预备党员', 'active'),
    (2, '20220018', '陈一诺', '13900188818', 'chenyinuo@example.edu.cn', '2022', '软件工程', '软件工程2班', '发展对象', 'active');

merge into sys_user (
    id, username, password_hash, real_name, user_type, student_id, status
) key(id) values
    (1, '20220001', '{demo}123456', '赵晨曦', 'student', 1, 'enabled'),
    (2, '20220018', '{demo}123456', '陈一诺', 'student', 2, 'enabled'),
    (8, 'teacher001', '{demo}123456', '李老师', 'teacher', null, 'enabled'),
    (18, 'leader001', '{demo}123456', '王院长', 'leader', null, 'enabled');

merge into cert_template (
    id, template_code, template_name, template_type, status, created_by
) key(id) values
    (1, 'student_status', '在读证明', 'certificate', 'enabled', 8);

merge into biz_application (
    id, application_no, application_type, template_id, applicant_user_id, student_id,
    title, purpose, form_data_json, status, current_approver_id, submitted_at,
    created_at, updated_at, is_deleted
) key(id) values
    (1, 'APP20260418001', 'certificate', 1, 1, 1,
     '在读证明申请', '实习单位提交材料',
     '{"receiveOrg":"星河智造科技有限公司","deliveryMode":"电子版"}',
     'reviewing', 8, timestamp '2026-04-18 14:30:00',
     timestamp '2026-04-18 14:30:00', timestamp '2026-04-18 14:30:00', 0);

merge into biz_approval_record (
    id, application_id, approver_user_id, action_type, action_comment,
    before_status, after_status, action_time
) key(id) values
    (1, 1, 1, 'submit', '提交院内证明申请', null, 'submitted', timestamp '2026-04-18 14:30:00'),
    (2, 1, 8, 'pending', '待处理', 'submitted', 'reviewing', timestamp '2026-04-18 14:31:00');
