# 数据库设计说明

本文档配合 `database/schema.sql` 使用，说明核心表职责、关键关系和开发约束。数据库目标环境为人大金仓 Kingbase，SQL 按 PostgreSQL 兼容语法编写。

## 1. 通用约定

- 主键统一使用 `bigserial` / `bigint`。
- 核心业务表保留 `created_at`、`updated_at`、`is_deleted`。
- 状态字段统一使用 `varchar`，枚举值见 `docs/dictionary.md`。
- 文件不直接存数据库，统一写入 `file_resource`，业务表只保存 `file_id`。
- 敏感字段可以应用层加密后入库，查询接口必须按角色脱敏。
- 管理端新增、修改、删除、导入、导出、审批、敏感字段查看必须写入 `audit_log`。
- 系统异常、接口 5xx、定时任务失败、消息发送失败等必须写入 `system_event_log`。

## 2. 表分组

### 2.1 用户与权限

| 表名 | 说明 ****|
| --- | --- |
| `sys_user` | 登录账户。学生账户通过 `student_id` 绑定 `stu_student`。 |
| `sys_role` | 角色定义，含默认数据范围 `data_scope`。 |
| `sys_permission` | 菜单、按钮、接口权限点。 |
| `sys_user_role` | 用户与角色多对多关系。 |
| `sys_role_permission` | 角色与权限多对多关系。 |
| `sys_user_scope` | 用户实际授权范围，如班级、支部、年级、学院。 |

关键约束：

- `sys_user.username` 唯一，可使用学号或工号。
- 学生用户必须绑定 `stu_student.id`。
- 后端鉴权必须同时校验权限码和 `sys_user_scope`。

### 2.2 文件资源

| 表名 | 说明 |
| --- | --- |
| `file_resource` | 上传文件统一索引，保存文件名、URL、大小、哈希和业务归属。 |

常见 `biz_type`：

- `kb_policy`
- `kb_template`
- `notice_attachment`
- `party_material`
- `certificate_template`
- `generated_certificate`
- `student_import`
- `honor_cover`

约束：

- 政策文件和模板支持 PDF、Word、Excel。
- 政策文件建议单文件最大 30MB。
- 下载接口必须校验业务权限。

### 2.3 学生画像

| 表名 | 说明 |
| --- | --- |
| `stu_student` | 学生基础档案，包含学号、姓名、年级、专业、班级、政治面貌等。 |
| `stu_student_ext` | 学生扩展档案，包含生源地、户籍地、导师、经济困难等级、修学/延毕记录等敏感信息。 |
| `stu_tag` | 标签定义，如就业意向、奖学金关注、党员发展对象。 |
| `stu_student_tag` | 学生与标签关联。 |
| `stu_growth_record` | 成长记录，覆盖竞赛、实践、志愿服务、干部任职、奖惩。 |
| `stu_import_task` | 批量导入任务，记录成功、失败数量和错误文件。 |

关键关系：

- `stu_student_ext.student_id` 与 `stu_student.id` 一对一。
- `stu_student_tag.student_id` 与 `stu_tag.id` 组成学生标签关系。
- `stu_growth_record.proof_file_id` 指向证明附件。

敏感字段：

- `id_card_no`
- `phone`
- `native_place`
- `household_address`
- `tutor_name`
- `family_economic_level`
- `study_change_record`
- `sensitive_remark`

### 2.4 奖励荣誉

| 表名 | 说明 |
| --- | --- |
| `honor_record` | 个人或集体荣誉，包含年度、类别、公开状态、排序、展示时效和事迹。 |

约束：

- `owner_type=student` 时建议填写 `owner_student_id`。
- 学生端或公开页只展示 `public_status=published` 且在展示时间范围内的数据。

### 2.5 知识库与智能问答

| 表名 | 说明 |
| --- | --- |
| `kb_category` | 知识库分类，支持父子级。 |
| `kb_article` | 当前版本知识条目。 |
| `kb_article_version` | 知识条目历史版本。 |
| `kb_template` | 常用 Word/Excel/PDF 模板下载。 |
| `kb_question_log` | 智能问答记录，含问题、回答、来源、置信度和反馈。 |

关键约束：

- 已发布知识条目更新时必须生成 `kb_article_version`。
- 学生端只能查询 `published` 且未过期条目。
- 智能问答结果必须能追溯到知识条目或附件来源。
- 没有可靠来源时，接口应返回未检索到依据，不得编造政策。

### 2.6 党团事务流程

| 表名 | 说明 |
| --- | --- |
| `flow_definition` | 流程定义，如入党流程、入团流程。 |
| `flow_stage_definition` | 阶段定义，如入党申请人、积极分子、发展对象、预备党员、正式党员。 |
| `flow_instance` | 学生个人流程实例。 |
| `flow_stage_record` | 阶段记录，保存状态、截止时间、审核意见。 |
| `flow_material` | 阶段材料提交与审核记录。 |
| `flow_action_log` | 全量动作留痕，覆盖提交、通过、退回、撤回、重批。 |
| `flow_exam_question` | 理论自测题库，可选模块。 |

关键关系：

- 一个 `flow_definition` 有多个 `flow_stage_definition`。
- 一个学生可以有多条 `flow_instance`，但同一流程通常只保留一条进行中实例。
- 一个 `flow_stage_record` 可以有多条 `flow_material`。
- 状态变更必须追加 `flow_action_log`，不能覆盖历史。

### 2.7 通知与精准推送

| 表名 | 说明 |
| --- | --- |
| `msg_notice` | 通知主表，保存标题、正文、标签、有效期、状态。 |
| `msg_notice_attachment` | 通知附件。 |
| `msg_notice_scope` | 目标对象规则，如年级、班级、标签、政治面貌。 |
| `msg_notice_user` | 站内消息触达与已读状态。 |
| `msg_delivery_record` | 邮件、微信、短信模拟等渠道发送记录。 |

发布约束：

- 发布时后端根据 `msg_notice_scope` 计算接收人，并写入 `msg_notice_user`。
- 邮件、微信失败不影响站内消息生成，但要写 `msg_delivery_record.error_message`。
- 短信一期只记录 `mocked` 状态。

### 2.8 院内申请与证明

| 表名 | 说明 |
| --- | --- |
| `cert_template` | 证明模板、表单配置和审批规则。 |
| `biz_application` | 院内申请主表，覆盖证明、请假、盖章等类型。 |
| `biz_approval_record` | 审批动作记录。 |

关键约束：

- `application_type` 至少支持 `certificate`、`leave`、`seal`。
- 证明生成文件写入 `generated_file_id`。
- 状态变化只能通过审批接口完成。
- 申请撤回需校验申请人、状态和 `revoke_deadline_at`。

### 2.9 学业预警扩展

| 表名 | 说明 |
| --- | --- |
| `academic_program` | 培养方案要求。 |
| `academic_warning` | 学业预警结果。 |

一期说明：

- 不做成绩单自动解析主流程。
- 可以保留手工录入或演示数据。

### 2.10 审计日志与系统事件日志

| 表名 | 说明 |
| --- | --- |
| `audit_log` | 操作审计日志，记录操作人、模块、动作、对象、接口路径和结果。 |
| `system_event_log` | 系统事件和异常日志，记录发生时间、操作人员、请求链路、错误信息和堆栈摘要。 |

必须审计：

- 管理端新增、修改、删除。
- 批量导入、导出。
- 审批通过、驳回、撤回、重批。
- 查看学生敏感字段。
- 通知发布、归档。
- 知识库发布、停用、版本替换。

必须写系统事件日志：

- 未捕获异常。
- 接口 5xx。
- 登录失败次数异常、账号锁定。
- 文件上传、解析、下载失败。
- 批量导入任务失败。
- 定时任务执行失败。
- 邮件、微信、短信模拟发送失败。
- 前端运行时错误上报。

`audit_log` 与 `system_event_log` 区别：

- `audit_log` 解决“谁在什么时候做了什么业务操作，结果如何”。
- `system_event_log` 解决“系统什么时候发生了什么事件或错误，关联哪个用户、请求和链路”。
- 一个审批失败可能同时写两类日志：审批动作写 `audit_log`，程序异常写 `system_event_log`。

## 3. 索引建议

`schema.sql` 已包含基础索引。开发中如果出现列表慢查询，优先补充以下方向：

- 学生列表：`grade`、`major`、`class_name`、`political_status`。
- 通知列表：`status`、`publish_at`、`user_id`、`read_status`。
- 审批列表：`current_approver_id`、`status`。
- 审计日志：`user_id`、`created_at`、`target_type`、`target_id`。
- 系统事件日志：`event_level`、`event_type`、`occurred_at`、`user_id`、`request_id`。
- 知识库：`category_id`、`publish_status`、`keywords`。

## 4. 初始化数据建议

项目启动时建议初始化：

- 四类角色：`student`、`class_cadre`、`teacher_admin`、`college_leader`。
- 基础权限码：按 `docs/role-permission.md`。
- 知识库分类：奖助、学籍、党团、证明、就业、宿舍。
- 入党流程定义和五个阶段。
- 常用申请模板：在读证明、成绩证明、盖章申请、请假申请。
