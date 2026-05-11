# 枚举字典

本文档定义前后端共用枚举。接口字段必须使用 `value`，页面展示使用 `label`。前端不得在多个页面重复硬编码同一套中文文案，应优先通过 `GET /dicts` 或统一本地字典读取。

## 1. 用户与权限

### 1.1 用户类型 `user_type`

| value | label |
| --- | --- |
| `student` | 学生 |
| `teacher` | 教师 |
| `leader` | 学院领导 |
| `system_admin` | 系统管理员 |

### 1.2 用户状态 `user_status`

| value | label |
| --- | --- |
| `enabled` | 启用 |
| `disabled` | 停用 |
| `locked` | 锁定 |

### 1.3 角色代码 `role_code`

| value | label |
| --- | --- |
| `student` | 普通学生 |
| `class_cadre` | 班团骨干 |
| `teacher_admin` | 管理老师 |
| `college_leader` | 学院领导 |
| `system_admin` | 系统管理员 |

### 1.4 数据范围 `data_scope`

| value | label |
| --- | --- |
| `self` | 本人 |
| `class` | 班级 |
| `branch` | 支部 |
| `major` | 专业 |
| `grade` | 年级 |
| `department` | 学院 |
| `global` | 全局 |

### 1.5 权限类型 `permission_type`

| value | label |
| --- | --- |
| `menu` | 菜单 |
| `button` | 按钮 |
| `api` | 接口 |

## 2. 学生画像

### 2.1 学生状态 `student_status`

| value | label |
| --- | --- |
| `active` | 在读 |
| `graduated` | 毕业 |
| `suspended` | 休学 |
| `withdrawn` | 退学 |
| `transferred` | 转专业 |
| `delayed` | 延毕 |

### 2.2 学历层次 `degree_level`

| value | label |
| --- | --- |
| `undergraduate` | 本科 |
| `master` | 硕士研究生 |
| `doctor` | 博士研究生 |

### 2.3 政治面貌 `political_status`

| value | label |
| --- | --- |
| `masses` | 群众 |
| `league_member` | 共青团员 |
| `party_applicant` | 入党申请人 |
| `activist` | 入党积极分子 |
| `development_candidate` | 发展对象 |
| `probationary_party_member` | 预备党员 |
| `party_member` | 中共党员 |

### 2.4 标签类型 `tag_type`

| value | label |
| --- | --- |
| `profile` | 画像标签 |
| `notice` | 通知标签 |
| `party` | 党团标签 |
| `risk` | 风险关注 |
| `honor` | 荣誉标签 |

### 2.5 标签来源 `tag_source_type`

| value | label |
| --- | --- |
| `manual` | 人工维护 |
| `import` | 批量导入 |
| `rule` | 规则生成 |

### 2.6 成长记录类型 `growth_record_type`

| value | label |
| --- | --- |
| `competition` | 科研竞赛 |
| `practice` | 社会实践 |
| `volunteer` | 志愿服务 |
| `cadre` | 干部任职 |
| `reward_punishment` | 奖惩情况 |

### 2.7 导入任务状态 `import_task_status`

| value | label |
| --- | --- |
| `pending` | 待处理 |
| `processing` | 处理中 |
| `success` | 全部成功 |
| `partial_success` | 部分成功 |
| `failed` | 失败 |

## 3. 文件与模板

### 3.1 文件业务类型 `file_biz_type`

| value | label |
| --- | --- |
| `kb_policy` | 政策文件 |
| `kb_template` | 知识库模板 |
| `notice_attachment` | 通知附件 |
| `party_material` | 党团材料 |
| `certificate_template` | 证明模板 |
| `generated_certificate` | 生成证明 |
| `student_import` | 学生导入文件 |
| `honor_cover` | 荣誉图片 |

### 3.2 模板类型 `template_type`

| value | label |
| --- | --- |
| `certificate` | 证明模板 |
| `leave` | 请假模板 |
| `seal` | 盖章模板 |
| `activity` | 活动模板 |
| `budget` | 预算模板 |
| `briefing` | 简报模板 |

## 4. 知识库

### 4.1 知识库分类建议 `kb_category`

| value | label |
| --- | --- |
| `scholarship` | 奖助 |
| `student_status` | 学籍 |
| `party_league` | 党团 |
| `certificate` | 证明 |
| `employment` | 就业 |
| `dormitory` | 宿舍 |

### 4.2 知识来源类型 `kb_source_type`

| value | label |
| --- | --- |
| `manual` | 手工录入 |
| `file` | 文件解析 |
| `url` | 外部链接 |

### 4.3 知识条目状态 `kb_publish_status`

| value | label |
| --- | --- |
| `draft` | 草稿 |
| `published` | 已发布 |
| `disabled` | 已停用 |
| `expired` | 已过期 |

### 4.4 问答反馈 `qa_feedback`

| value | label |
| --- | --- |
| `helpful` | 有帮助 |
| `unhelpful` | 无帮助 |
| `need_manual` | 需人工处理 |

## 5. 党团流程

### 5.1 流程类型 `flow_type`

| value | label |
| --- | --- |
| `party_join` | 入党流程 |
| `league_join` | 入团流程 |

### 5.2 入党阶段代码 `party_stage_code`

| value | label |
| --- | --- |
| `applicant` | 入党申请人 |
| `activist` | 入党积极分子 |
| `development_candidate` | 发展对象 |
| `probationary_party_member` | 预备党员 |
| `party_member` | 正式党员 |

### 5.3 流程实例状态 `flow_instance_status`

| value | label |
| --- | --- |
| `processing` | 办理中 |
| `completed` | 已完成 |
| `suspended` | 暂停 |
| `cancelled` | 已取消 |

### 5.4 阶段状态 `flow_stage_status`

| value | label |
| --- | --- |
| `pending` | 未开始 |
| `submitted` | 已提交 |
| `reviewing` | 审核中 |
| `approved` | 已通过 |
| `returned` | 已退回 |
| `revoked` | 已撤回 |

### 5.5 材料提交状态 `material_submit_status`

| value | label |
| --- | --- |
| `submitted` | 已提交 |
| `supplemented` | 已补充 |
| `revoked` | 已撤回 |

### 5.6 材料审核状态 `material_review_status`

| value | label |
| --- | --- |
| `pending` | 待审核 |
| `approved` | 已通过 |
| `returned` | 已退回 |
| `supplement_required` | 需补充 |

### 5.7 流程动作 `flow_action_type`

| value | label |
| --- | --- |
| `submit` | 提交 |
| `approve` | 通过 |
| `return` | 退回 |
| `request_supplement` | 要求补充 |
| `revoke` | 撤回 |
| `reopen` | 重批 |
| `comment` | 补充说明 |

### 5.8 理论题型 `exam_question_type`

| value | label |
| --- | --- |
| `single_choice` | 单选题 |
| `multiple_choice` | 多选题 |
| `judge` | 判断题 |
| `short_answer` | 简答题 |

## 6. 通知与推送

### 6.1 通知类型 `notice_type`

| value | label |
| --- | --- |
| `normal` | 普通通知 |
| `targeted` | 定向通知 |
| `urgent` | 紧急通知 |

### 6.2 通知状态 `notice_status`

| value | label |
| --- | --- |
| `draft` | 草稿 |
| `scheduled` | 定时发布 |
| `published` | 已发布 |
| `archived` | 已归档 |
| `expired` | 已过期 |

### 6.3 通知目标范围 `notice_scope_type`

| value | label |
| --- | --- |
| `all` | 全体学生 |
| `grade` | 年级 |
| `major` | 专业 |
| `class` | 班级 |
| `branch` | 支部 |
| `tag` | 标签 |
| `political_status` | 政治面貌 |
| `student` | 指定学生 |

### 6.4 阅读状态 `read_status`

| value | label |
| --- | --- |
| `unread` | 未读 |
| `read` | 已读 |

### 6.5 触达状态 `delivery_status`

| value | label |
| --- | --- |
| `pending` | 待发送 |
| `success` | 发送成功 |
| `failed` | 发送失败 |
| `mocked` | 模拟发送 |

### 6.6 发送渠道 `delivery_channel`

| value | label |
| --- | --- |
| `site` | 站内消息 |
| `email` | 邮件 |
| `wechat` | 微信提醒 |
| `sms` | 短信模拟 |

## 7. 院内申请与审批

### 7.1 申请类型 `application_type`

| value | label |
| --- | --- |
| `certificate` | 证明申请 |
| `leave` | 请假申请 |
| `seal` | 盖章申请 |

### 7.2 申请状态 `application_status`

| value | label |
| --- | --- |
| `draft` | 草稿 |
| `submitted` | 已提交 |
| `reviewing` | 审核中 |
| `approved` | 已通过 |
| `rejected` | 已驳回 |
| `revoked` | 已撤回 |
| `archived` | 已归档 |

### 7.3 审批动作 `approval_action_type`

| value | label |
| --- | --- |
| `submit` | 提交 |
| `approve` | 通过 |
| `reject` | 驳回 |
| `revoke` | 撤回 |
| `transfer` | 转交 |
| `comment` | 备注 |

## 8. 荣誉展示

### 8.1 荣誉类型 `honor_type`

| value | label |
| --- | --- |
| `personal` | 个人荣誉 |
| `collective` | 集体荣誉 |

### 8.2 荣誉类别 `honor_category`

| value | label |
| --- | --- |
| `national_scholarship` | 国家奖学金 |
| `school_excellent` | 校级优秀 |
| `advanced_collective` | 先进集体 |
| `competition_award` | 竞赛获奖 |
| `party_league_honor` | 党团荣誉 |

### 8.3 荣誉归属类型 `honor_owner_type`

| value | label |
| --- | --- |
| `student` | 学生个人 |
| `class` | 班级 |
| `branch` | 支部 |
| `team` | 团队 |
| `other` | 其他 |

### 8.4 公开状态 `public_status`

| value | label |
| --- | --- |
| `hidden` | 隐藏 |
| `published` | 已公开 |
| `unpublished` | 已下线 |

## 9. 学业预警扩展

### 9.1 预警类型 `academic_warning_type`

| value | label |
| --- | --- |
| `credit_gap` | 学分缺口 |
| `course_missing` | 课程缺失 |
| `graduation_risk` | 毕业风险 |
| `manual` | 人工预警 |

### 9.2 预警等级 `warning_level`

| value | label |
| --- | --- |
| `low` | 低 |
| `medium` | 中 |
| `high` | 高 |

### 9.3 预警状态 `warning_status`

| value | label |
| --- | --- |
| `open` | 待处理 |
| `processing` | 跟进中 |
| `closed` | 已关闭 |

## 10. 审计

### 10.1 审计模块 `audit_module_code`

| value | label |
| --- | --- |
| `auth` | 认证 |
| `student` | 学生画像 |
| `kb` | 知识库 |
| `party` | 党团流程 |
| `notice` | 通知 |
| `application` | 院内申请 |
| `honor` | 荣誉展示 |
| `file` | 文件 |
| `permission` | 权限 |
| `import_export` | 导入导出 |

### 10.2 审计动作 `audit_action_code`

| value | label |
| --- | --- |
| `create` | 新增 |
| `update` | 修改 |
| `delete` | 删除 |
| `view` | 查看 |
| `view_sensitive` | 查看敏感字段 |
| `import` | 导入 |
| `export` | 导出 |
| `approve` | 审批通过 |
| `reject` | 审批驳回 |
| `publish` | 发布 |
| `archive` | 归档 |
| `login` | 登录 |
| `logout` | 退出 |

### 10.3 操作结果 `result_code`

| value | label |
| --- | --- |
| `success` | 成功 |
| `failed` | 失败 |
| `partial_success` | 部分成功 |

## 11. 接口错误码

| code | label |
| --- | --- |
| `0` | 成功 |
| `40001` | 参数校验失败 |
| `40100` | 未登录或 token 无效 |
| `40300` | 无权限访问 |
| `40400` | 资源不存在 |
| `40900` | 状态冲突 |
| `41300` | 文件过大 |
| `50000` | 系统内部错误 |

## 12. 系统事件日志

### 12.1 事件类型 `system_event_type`

| value | label |
| --- | --- |
| `api_error` | 接口异常 |
| `job_error` | 定时任务异常 |
| `file_error` | 文件处理异常 |
| `message_error` | 消息发送异常 |
| `auth_event` | 认证事件 |
| `permission_event` | 权限事件 |
| `client_error` | 前端错误 |
| `system_event` | 系统事件 |

### 12.2 事件级别 `system_event_level`

| value | label |
| --- | --- |
| `debug` | 调试 |
| `info` | 信息 |
| `warn` | 警告 |
| `error` | 错误 |
| `fatal` | 严重错误 |

### 12.3 系统日志模块 `system_log_module_code`

| value | label |
| --- | --- |
| `frontend` | 前端 |
| `auth` | 认证 |
| `student` | 学生画像 |
| `kb` | 知识库 |
| `party` | 党团流程 |
| `notice` | 通知 |
| `application` | 院内申请 |
| `honor` | 荣誉展示 |
| `file` | 文件 |
| `message` | 消息通道 |
| `job` | 定时任务 |
| `database` | 数据库 |
| `system` | 系统 |
