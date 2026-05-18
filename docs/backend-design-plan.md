# 学院学生综合服务与党团管理平台后端设计方案（开发落地版）

## 1. 目标与范围

本方案面向“新加入后端开发者可直接开工”，基于现有接口文档、页面原型、数据库基线给出可执行设计。

- 设计输入：
  - `docs/api.md`
  - `docs/architecture.md`
  - `docs/role-permission.md`
  - `docs/database.md`
  - `database/schema.sql`
  - `student.html`、`admin.html`、`login.html`
- 一期优先级：
  - P0：认证鉴权、知识库问答、党团流程、通知触达、日志体系
  - P1：学生画像管理、院内申请审批、荣誉展示
  - P2：学业预警（预留）

## 2. 总体架构建议

建议采用“模块化单体（Modular Monolith）”先交付一期，避免过早拆微服务。

- API 风格：REST，统一前缀 `/api/v1`
- 架构分层：
  - `controller`：协议转换、参数校验、统一响应
  - `application`：用例编排（事务边界）
  - `domain`：状态机、权限规则、业务约束
  - `infrastructure`：数据库、文件存储、消息发送、日志落库
- 公共能力模块：
  - `auth`（JWT、登录、当前用户）
  - `rbac`（权限码 + 数据范围）
  - `audit`（审计日志）
  - `eventlog`（系统事件日志）
  - `file`（上传下载与权限归属）

推荐目录（示意）：

```text
src/main/java/.../modules
  auth/
  student/
  tag/
  kb/
  party/
  notice/
  application/
  honor/
  dashboard/
  log/
common/
  security/
  rbac/
  web/
  exception/
  util/
```

## 3. 技术选型建议

可选方案很多，但结合 Kingbase、RBAC、事务和日志需求，推荐 Java 栈：

- JDK 17 + Spring Boot 3.x
- ORM：MyBatis-Plus（复杂筛选和范围过滤更可控）或 JPA（团队熟悉可用）
- DB：Kingbase（PostgreSQL 兼容）
- 缓存：Redis（会话、字典、热点统计）
- 任务调度：Spring Scheduling（定时发布通知、过期处理）
- 文件存储：本地/NAS/MinIO（数据库仅存 `file_resource` 元数据）
- 可观测：Micrometer + 日志聚合（按条件可先本地文件）

当前开发阶段的临时数据库策略：

- 先不使用人大金仓 Kingbase，避免 Windows 授权版本限制 Linux 开发与 CI。
- 本地和测试默认使用 H2，并开启 PostgreSQL 兼容模式。
- SQL 表名和字段命名尽量对齐 `database/schema.sql`，后续迁移到 Kingbase 时优先替换数据源和迁移脚本，不改变 Controller/API 契约。
- 当前已用 Spring JDBC 接入申请模块；后续如团队确定 MyBatis-Plus 或 JPA，再按模块统一重构数据访问层。

## 4. 业务模块与表映射

### 4.1 认证与权限

- 主要表：`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`sys_user_scope`
- 关键接口：`/auth/login`、`/auth/me`、`/auth/logout`
- 关键实现：
  - JWT 内含 `userId`、`roleCodes`、`tokenVersion`
  - `/auth/me` 返回权限码和数据范围，前端据此渲染菜单/按钮
  - 所有业务接口后端再次校验权限，前端隐藏不算安全

### 4.2 学生画像

- 主要表：`stu_student`、`stu_student_ext`、`stu_tag`、`stu_student_tag`、`stu_growth_record`、`stu_import_task`
- 关键接口：`/students`、`/students/{id}`、`/students/{id}/growth-records`、`/students/import-tasks`、`/students/export`
- 关键实现：
  - `includeSensitive=true` 时必须验 `student:sensitive:view` 并写审计日志
  - 班团骨干仅可看授权范围“摘要字段”
  - 导入任务异步化：创建任务后立即返回 `taskNo`

### 4.3 知识库与问答

- 主要表：`kb_category`、`kb_article`、`kb_article_version`、`kb_template`、`kb_question_log`
- 关键接口：`/kb/articles`、`/kb/articles/{id}/publish`、`/kb/qa`、`/kb/templates`
- 关键实现：
  - 已发布条目修改时自动写 `kb_article_version`
  - 学生查询自动附带 `publish_status=published` + 未过期过滤
  - `POST /kb/qa` 必须返回 `sources`；无依据时固定提示“不可靠来源”

### 4.4 党团流程

- 主要表：`flow_definition`、`flow_stage_definition`、`flow_instance`、`flow_stage_record`、`flow_material`、`flow_action_log`
- 关键接口：`/party/instances/me`、`/party/stage-records/{id}/materials`、`/party/stage-records/{id}/review`
- 关键实现：
  - 严格线性状态机：学生仅可提交“本人当前阶段”
  - 审核动作：`approve`、`return`、`request_supplement`
  - 撤回重批保留历史，不覆盖旧记录

### 4.5 通知与精准推送

- 主要表：`msg_notice`、`msg_notice_scope`、`msg_notice_user`、`msg_delivery_record`、`msg_notice_attachment`
- 关键接口：`/notices`、`/notices/{id}/publish`、`/notices/my`、`/notices/{id}/stats`
- 关键实现：
  - 发布时后端计算接收人并批量写 `msg_notice_user`
  - 渠道失败不影响站内通知，但写 `msg_delivery_record`
  - 支持草稿、定时发布、过期归档

### 4.6 院内申请与审批

- 主要表：`cert_template`、`biz_application`、`biz_approval_record`
- 关键接口：`/applications`、`/applications/{id}/approve`、`/applications/{id}/reject`、`/applications/{id}/revoke`
- 关键实现：
  - 状态只能由后端状态机驱动，不允许前端直改
  - 撤回需校验提交人 + 状态 + `revoke_deadline_at`
  - 审批动作写审计日志与审批记录

### 4.7 荣誉展示

- 主要表：`honor_record`
- 关键接口：`/honors/public`、`/honors`、`/honors/{id}/publish`
- 关键实现：
  - 学生端仅看 `public_status=published` 且在展示时间窗口内

### 4.8 日志体系

- 审计日志：`audit_log`（业务责任追踪）
- 系统事件：`system_event_log`（异常与排障）
- 落地方式：
  - 业务操作用 AOP 或统一审计组件写 `audit_log`
  - 全局异常处理器 + 关键失败分支写 `system_event_log`
  - 前端报错接收口：`POST /system-logs/client-errors`

## 5. 鉴权与数据范围实现方案

## 5.1 权限校验（RBAC）

- 每个接口映射一个权限码（如 `notice:publish`）
- 注解式校验：`@RequirePermission("notice:publish")`
- 登录后用户权限可短期缓存（Redis，5-10 分钟）

## 5.2 数据范围校验（Scope）

统一“范围过滤器”构建 SQL 条件：

- `self`：`student_id = currentStudentId`
- `class`：`class_name in userScopes`
- `branch`：按支部/流程 owner 过滤
- `major`、`grade`、`department`：按授权值拼接

对外要求：即使前端传了任意 `studentId`，也必须在服务端二次校验归属。

## 6. 关键状态机（必须先实现）

### 6.1 申请状态机（biz_application.status）

- `submitted -> reviewing -> approved/rejected`
- `submitted/reviewing -> revoked`（仅申请人、仅时限内）
- 任何非法跳转返回 `40900`

### 6.2 党团阶段状态机（flow_stage_record.stage_status）

- `pending -> submitted -> reviewing -> approved`
- `reviewing -> returned` 或 `request_supplement`
- `approved` 原则上不可重复审批；重批走 `reopen` 专用动作

## 7. 接口实现优先级（按前端联调价值）

第一批（最小可联调闭环）：

1. `POST /auth/login`
2. `GET /auth/me`
3. `GET /dashboard/student`
4. `GET /dashboard/admin`
5. `GET /notices/my` + `POST /notices/{id}/read`
6. `GET /party/instances/me`
7. `POST /party/stage-records/{id}/materials`
8. `GET /kb/articles` + `POST /kb/qa`
9. `GET /applications/my` + `POST /applications`

第二批（管理核心）：

1. `GET /students` / `GET /students/{id}`
2. `POST /students/import-tasks`
3. `POST /notices` / `POST /notices/{id}/publish`
4. `GET /applications/approvals/pending` / `approve` / `reject`
5. `GET /audit-logs` / `GET /system-logs`

## 8. 事务与一致性建议

- 单请求内强一致（数据库事务）
- 申请审批这类“状态更新 + 操作记录”必须放在公开服务方法事务内，避免同类内部调用导致事务代理失效。
- 跨系统动作（如通知多渠道发送）采用“主流程成功 + 异步发送 + 失败落日志”
- 批量导入采用任务表驱动，避免长事务
- 关键并发点（重复审批、重复发布）使用“状态条件更新 + 行锁/乐观锁”

## 9. 安全与合规基线

- 密码只存 `password_hash`（BCrypt/Argon2）
- 敏感字段（身份证、电话、地址）加密存储或至少严格脱敏输出
- 文件下载必须校验“业务归属 + 当前用户权限”
- 日志脱敏：禁止记录密码、token、身份证原文、完整手机号

## 10. 开发排期建议（两周节奏示例）

- 第 1 周：
  - 鉴权/RBAC/数据范围中间件
  - 登录、`/auth/me`、学生/管理首页聚合
  - 通知“我的通知 + 已读”
  - 党团“我的进度 + 材料提交”
- 第 2 周：
  - 知识库查询与问答
  - 申请发起与我的申请
  - 管理端审批接口
  - 审计日志与系统日志闭环

## 11. 新开发者开工清单

1. 先跑通数据库初始化：`database/schema.sql`
2. 建立统一响应与错误码（`code/message/data/requestId`）
3. 完成 `auth + rbac + scope` 三件套后再写业务模块
4. 先实现第一批联调接口，确保 `student.html/admin.html` 主要路径可打通
5. 每个状态变更接口先写状态机测试，再写实现
6. 审计日志和系统日志不要后补，第一版就接入

---

如果你希望，我可以下一步直接给出“Spring Boot 工程骨架 + 模块包结构 + 前 10 个接口的 DTO/Controller 空实现”，让你按这个方案当天就能进入联调。

## 12. 当前实现进展（2026-05-18）

已按本文档先完成“可联调 MVP”：

- 已落地：认证、首页聚合、通知、知识库问答、院内申请、学生画像核心接口、字典接口
- 已落地：党团流程定义、我的流程实例、当前阶段材料提交
- 已落地：申请撤回、待审批列表、审批通过/驳回（含状态机约束）
- 已落地：学生写接口（基础信息更新、成长记录新增、标签更新、导入任务创建与查询）
- 已落地：统一响应、统一错误码、`requestId` 透传、Bearer 鉴权过滤器
- 已落地：复杂逻辑单测与集成测试（阶段约束、访问权限、问答兜底、申请审批流转）
- 已落地：后端内置交互页 `interaction-test.html`（支持登录、上传、流程、申请、学生写操作）联调
- 已落地：H2 本地数据库接入，院内申请与审批模块已迁移为 JDBC 持久化。
- 已落地：申请审批事务回滚测试，确保状态更新和审批记录写入原子一致。

- 当前仍有多数模块为内存演示实现；下一步应继续迁移学生画像、通知、党团和知识库模块，并补齐权限表校验与审计日志落库。
