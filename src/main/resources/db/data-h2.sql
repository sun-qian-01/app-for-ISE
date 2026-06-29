merge into stu_student (
    id, student_no, name, phone, email, grade, major, class_name, political_status, status
) key(id) values
    (1, '20220001', '赵晨曦', '13800181234', 'zhaochenxi@example.edu.cn', '2022', '软件工程', '软件工程2班', '预备党员', 'active'),
    (2, '20220018', '陈一诺', '13900188818', 'chenyinuo@example.edu.cn', '2022', '软件工程', '软件工程2班', '发展对象', 'active'),
    (3, '20260031', '林嘉禾', '13700000631', 'linjiahe@example.edu.cn', '2026', '数据科学', '数据科学1班', '共青团员', 'active'),
    (4, '20230007', '周明远', '13600003007', 'zhoumingyuan@example.edu.cn', '2023', '软件工程', '软件工程1班', '共青团员', 'warning');

merge into sys_user (
    id, username, password_hash, real_name, user_type, role_code, student_id, status
) key(id) values
    (1, '20220001', '{demo}123456', '赵晨曦', 'student', 'student', 1, 'enabled'),
    (2, '20220018', '{demo}123456', '陈一诺', 'student', 'class_cadre', 2, 'enabled'),
    (8, 'teacher001', '{demo}123456', '李老师', 'teacher', 'teacher_admin', null, 'enabled'),
    (18, 'leader001', '{demo}123456', '王院长', 'leader', 'college_leader', null, 'enabled');

merge into cert_template (
    id, template_code, template_name, template_type, status, created_by
) key(id) values
    (1, 'student_status', '在读证明', 'certificate', 'enabled', 8),
    (2, 'grade_report', '成绩证明', 'certificate', 'enabled', 8),
    (3, 'party_stamp', '党团材料盖章', 'certificate', 'enabled', 8);

merge into biz_application (
    id, application_no, application_type, template_id, applicant_user_id, student_id,
    title, purpose, form_data_json, status, current_approver_id, submitted_at,
    created_at, updated_at, is_deleted
) key(id) values
    (1, 'APP20260418001', 'certificate', 1, 1, 1,
     '在读证明申请', '实习单位提交材料',
     '{"receiveOrg":"星河智造科技有限公司","deliveryMode":"电子版"}',
     'reviewing', 8, timestamp '2026-04-18 14:30:00',
     timestamp '2026-04-18 14:30:00', timestamp '2026-04-18 14:30:00', 0),
    (2, 'APP20260410002', 'certificate', 2, 1, 1,
     '成绩证明申请', '交换项目报名',
     '{"receiveOrg":"北桥大学国际交流处","deliveryMode":"纸质版"}',
     'approved', null, timestamp '2026-04-10 09:12:00',
     timestamp '2026-04-10 09:12:00', timestamp '2026-04-10 09:12:00', 0),
    (3, 'APP20260405003', 'certificate', 3, 2, 2,
     '党团材料盖章申请', '支部季度材料归档',
     '{"receiveOrg":"本科生第一党支部","deliveryMode":"纸质版"}',
     'rejected', null, timestamp '2026-04-05 16:08:00',
     timestamp '2026-04-05 16:08:00', timestamp '2026-04-05 16:08:00', 0),
    (4, 'APP20260421004', 'certificate', 1, 2, 2,
     '在读证明申请', '校外竞赛资格审核',
     '{"receiveOrg":"华东创新挑战赛组委会","deliveryMode":"电子版"}',
     'submitted', 8, timestamp '2026-04-21 11:20:00',
     timestamp '2026-04-21 11:20:00', timestamp '2026-04-21 11:20:00', 0);

merge into biz_approval_record (
    id, application_id, approver_user_id, action_type, action_comment,
    before_status, after_status, action_time
) key(id) values
    (1, 1, 1, 'submit', '提交院内证明申请', null, 'submitted', timestamp '2026-04-18 14:30:00'),
    (2, 1, 8, 'pending', '待处理', 'submitted', 'reviewing', timestamp '2026-04-18 14:31:00'),
    (3, 2, 1, 'submit', '提交成绩证明申请', null, 'submitted', timestamp '2026-04-10 09:12:00'),
    (4, 2, 8, 'approve', '材料齐全，同意通过', 'reviewing', 'approved', timestamp '2026-04-10 10:05:00'),
    (5, 3, 2, 'submit', '提交党团材料盖章申请', null, 'submitted', timestamp '2026-04-05 16:08:00'),
    (6, 3, 8, 'reject', '缺少支部意见页，请补充后重提', 'reviewing', 'rejected', timestamp '2026-04-05 17:10:00'),
    (7, 4, 2, 'submit', '提交在读证明申请', null, 'submitted', timestamp '2026-04-21 11:20:00');

merge into kb_article (
    id, title, summary, category_label, publish_status, version_no, standard_answer,
    source_file_name, source_file_id, keywords_text, view_count, created_at, updated_at, is_deleted
) key(id) values
    (1, '国家奖学金评定流程说明', '包含申请资格、名额分配、材料清单和公示流程。', '奖助', 'published', 'v3',
     '国家奖学金通常需要提交申请表、成绩证明、综测排名证明和获奖佐证材料，具体时间以学院通知为准。',
     '国家奖学金评定办法.pdf', 12001, '奖学金,国家奖学金,评定,材料', 426,
     timestamp '2026-03-12 10:00:00', timestamp '2026-04-20 09:00:00', 0),
    (2, '休学与复学办理指引', '说明休学申请条件、复学材料和学院审核路径。', '学籍', 'published', 'v2',
     '休学需提供申请书和相关证明材料，复学按学院通知提交复学申请并完成教务审核。',
     '学籍异动办理指南.docx', 12002, '休学,复学,学籍,办理', 311,
     timestamp '2026-02-18 15:00:00', timestamp '2026-04-16 11:20:00', 0),
    (3, '党员发展阶段材料清单', '汇总积极分子、发展对象、预备党员各阶段所需材料。', '党团', 'published', 'v4',
     '党员发展阶段需按节点提交思想汇报、谈话记录、培训记录和志愿服务证明等材料。',
     '党员发展材料清单.xlsx', 12003, '党员,思想汇报,发展对象,预备党员', 287,
     timestamp '2026-01-09 09:30:00', timestamp '2026-04-22 14:12:00', 0),
    (4, '在读证明与成绩证明办理说明', '说明在读证明、成绩证明申请场景和附件要求。', '证明', 'published', 'v2',
     '在读证明和成绩证明均需填写用途并确认收件单位，审批通过后可下载电子版证明文件。',
     '学生证明办理指南.pdf', 12004, '在读证明,成绩证明,申请,附件', 198,
     timestamp '2026-03-25 14:10:00', timestamp '2026-04-18 13:45:00', 0),
    (5, '毕业生就业信息登记补录说明', '说明毕业生就业信息补录时间、材料要求和联系方式核验规则。', '就业', 'published', 'v1',
     '毕业生需在规定时间内完成就业信息补录，并核验联系方式与签约材料信息。',
     '毕业生就业信息补录通知.docx', 12005, '就业,毕业生,补录,信息登记', 142,
     timestamp '2026-04-06 08:35:00', timestamp '2026-04-23 16:20:00', 0),
    (6, '2024级大类培养方案（含辅修）说明', '覆盖培养目标、课程结构、学分要求与辅修路径。', '培养方案', 'published', 'v1',
     '2024级培养方案包含主修与辅修课程规划，建议结合学院教务通知与个人培养计划同步执行。',
     '2024级大类培养方案（含辅修）.pdf', 12006, '培养方案,2024级,辅修,学分', 36,
     timestamp '2026-05-25 09:20:00', timestamp '2026-05-25 09:20:00', 0),
    (7, '2025级大类培养方案解读', '面向 2025 级学生，梳理课程地图与选课建议。', '培养方案', 'published', 'v1',
     '2025级培养方案强调基础课程与方向模块衔接，建议在导师指导下规划选课路径。',
     '2025级大类培养方案.pdf', 12007, '培养方案,2025级,课程地图,选课', 29,
     timestamp '2026-05-25 09:35:00', timestamp '2026-05-25 09:35:00', 0),
    (8, '信息学院 2025 年综合类政策摘要', '汇总学院 2025 年综合类通知重点与办理窗口。', '学院政策', 'published', 'v1',
     '综合类政策涉及培养、事务办理与相关时间节点，请以学院正式通知为最终依据。',
     '中国人民大学信息学院2025年综合类.pdf', 12008, '学院政策,综合类,办理窗口,通知', 24,
     timestamp '2026-05-25 10:05:00', timestamp '2026-05-25 10:05:00', 0),
    (9, '党员证明开具说明', '介绍党员证明模板用途、填写要点与申请流程。', '党团', 'published', 'v1',
     '党员证明需按学院模板填写并经支部审核后提交，建议同步准备身份信息与用途说明。',
     '党员证明模板.docx', 12009, '党员证明,党团,模板,开具', 18,
     timestamp '2026-05-25 10:30:00', timestamp '2026-05-25 10:30:00', 0),
    (10, '团员证明开具说明', '介绍团员证明模板填写规范与常见使用场景。', '党团', 'published', 'v1',
     '团员证明建议填写完整用途、接收单位与日期，提交前确认模板字段无遗漏。',
     '团员证明模板.docx', 12010, '团员证明,党团,模板,开具', 15,
     timestamp '2026-05-25 10:45:00', timestamp '2026-05-25 10:45:00', 0);

merge into kb_template (
    id, template_name, category_label, file_type, description, file_id, updated_at, is_deleted
) key(id) values
    (1, '在读证明申请模板', '证明', 'docx', '用于校外实习、报名或签证材料准备。', 13001, timestamp '2026-04-18 11:00:00', 0),
    (2, '国家奖学金材料清单模板', '奖助', 'xlsx', '包含成绩、综测、获奖和附件核对项。', 13002, timestamp '2026-04-16 15:40:00', 0),
    (3, '思想汇报撰写模板', '党团', 'docx', '适用于积极分子、发展对象和预备党员阶段。', 13003, timestamp '2026-04-12 09:30:00', 0),
    (4, '就业信息补录说明模板', '就业', 'docx', '用于毕业生就业信息补录说明与常见问题答疑。', 13004, timestamp '2026-04-20 10:10:00', 0),
    (5, '党员证明模板', '党团', 'docx', '用于党员身份相关场景的证明开具。', 13005, timestamp '2026-05-25 10:30:00', 0),
    (6, '团员证明模板', '党团', 'docx', '用于团员身份相关场景的证明开具。', 13006, timestamp '2026-05-25 10:45:00', 0);

merge into biz_notice (
    id, title, content, audience, channel_labels, tag_labels, delivered_count, read_count, status, publish_at, created_by, created_at, updated_at, is_deleted
) key(id) values
    (1, '2026 年春季学期奖学金材料提交通知',
     '请于 4 月 24 日前完成材料提交，逾期系统将自动关闭入口。',
     '2022级 + 奖学金关注', '站内,邮件,微信', '奖助,材料提交',
     268, 201, 'published', timestamp '2026-04-18 12:00:00', 8,
     timestamp '2026-04-18 11:40:00', timestamp '2026-04-18 12:00:00', 0),
    (2, '预备党员季度思想汇报提醒',
     '你所在支部需于本周内补齐季度思想汇报，请及时上传。',
     '党员发展对象', '站内,微信', '党团,材料提醒',
     71, 46, 'published', timestamp '2026-04-17 16:30:00', 8,
     timestamp '2026-04-17 16:10:00', timestamp '2026-04-17 16:30:00', 0),
    (3, '毕业生就业信息登记更新说明',
     '就业去向信息已开放二次更新，请在学院平台完成信息校验。',
     '2026届毕业生', '站内,邮件', '就业,信息校验',
     312, 287, 'published', timestamp '2026-04-15 09:00:00', 8,
     timestamp '2026-04-15 08:40:00', timestamp '2026-04-15 09:00:00', 0);

merge into biz_notice_read (
    id, notice_id, user_id, read_at
) key(id) values
    (1, 3, 1, timestamp '2026-04-15 09:20:00'),
    (2, 3, 2, timestamp '2026-04-15 09:30:00');

merge into sys_audit_log (
    id, actor_user_id, actor_name, module_name, action_text, result_text, created_at
) key(id) values
    (1, 8, '李老师', '学生画像', '查看学生敏感字段：联系方式', '成功', timestamp '2026-04-19 09:12:00'),
    (2, 8, '李老师', '通知', '发布定向通知：奖学金材料提交', '成功', timestamp '2026-04-18 17:43:00');

merge into biz_honor (
    id, title, owner_name, owner_user_id, honor_scope, honor_year, category_label, story, created_at, updated_at, is_deleted
) key(id) values
    (1, '国家奖学金获得者', '赵晨曦', 1, 'personal', '2025', '国家奖学金', '综合成绩排名专业前 3%，参与创新训练项目和志愿服务。',
     timestamp '2025-12-20 10:00:00', timestamp '2025-12-20 10:00:00', 0),
    (2, '优秀共青团干部', '陈一诺', 2, 'personal', '2026', '党团荣誉', '长期协助支部活动组织和材料收集，推动团员青年理论学习。',
     timestamp '2026-04-02 10:00:00', timestamp '2026-04-02 10:00:00', 0),
    (3, '先进班集体', '软件工程2班', null, 'collective', '2025', '先进集体', '班级学风建设成效明显，竞赛参与率和志愿服务时长居年级前列。',
     timestamp '2025-11-15 10:00:00', timestamp '2025-11-15 10:00:00', 0),
    (4, '学院党建工作示范支部', '本科生第一党支部', null, 'party', '2025', '党团荣誉', '支部主题党日与理论学习组织规范，材料归档完整。',
     timestamp '2025-10-30 10:00:00', timestamp '2025-10-30 10:00:00', 0),
    (5, '“挑战杯”省赛二等奖', '赵晨曦', 1, 'personal', '2026', '科研竞赛', '围绕校园服务数字化主题完成项目路演与答辩，获得省赛二等奖。',
     timestamp '2026-05-06 09:20:00', timestamp '2026-05-06 09:20:00', 0),
    (6, '优秀志愿者（校庆专项）', '赵晨曦', 1, 'personal', '2026', '志愿服务', '累计服务 48 小时，负责观众引导和活动现场秩序保障。',
     timestamp '2026-05-12 14:15:00', timestamp '2026-05-12 14:15:00', 0),
    (7, '创新创业训练项目校级立项', '赵晨曦', 1, 'personal', '2026', '科研项目', '项目方向为“院系数据治理与流程协同”，进入校级重点培育名单。',
     timestamp '2026-04-26 16:40:00', timestamp '2026-04-26 16:40:00', 0),
    (8, '十佳团支部', '软件工程2班团支部', null, 'collective', '2026', '党团荣誉', '年度主题团日活动完成度高，青年理论学习覆盖率达到 100%。',
     timestamp '2026-03-30 11:05:00', timestamp '2026-03-30 11:05:00', 0),
    (9, '学院就业先锋宿舍', '梅园 3 栋 412', null, 'collective', '2026', '就业与成长', '宿舍成员就业去向落实率与求职互助氛围在年级中表现突出。',
     timestamp '2026-05-02 10:30:00', timestamp '2026-05-02 10:30:00', 0),
    (10, '社会实践先进个人', '赵晨曦', 1, 'personal', '2024', '社会实践', '连续两年参加乡村振兴社会实践，形成调研报告并在学院交流。',
     timestamp '2024-12-12 09:10:00', timestamp '2024-12-12 09:10:00', 0);
