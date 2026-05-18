# 后端改动记录

## 2026-05-18

### 0. 开发环境升级（当前用户级）

- 已安装用户级 JDK：Temurin 17.0.19
- 已安装用户级 Maven：3.9.9
- 已配置环境脚本：`~/.local/bin/java-maven-env.sh`
- 已追加到：`~/.bashrc`、`~/.profile`

### 1. 新增后端工程骨架

- 新增 `Spring Boot + Maven` 工程结构
- 新增统一配置：
  - 统一响应 `ApiResponse`
  - 统一错误码 `ErrorCode`
  - 全局异常处理 `GlobalExceptionHandler`
  - `requestId` 生成与透传过滤器
  - Bearer Token 鉴权过滤器（内存 token）

### 2. 已实现接口（MVP 可联调版）

- 认证：
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
  - `POST /api/v1/auth/logout`
  - `GET /auth/me` 已补 `menus` 字段
- 首页：
  - `GET /api/v1/dashboard/student`
  - `GET /api/v1/dashboard/admin`
  - `GET /api/v1/dashboard/leader`
- 通知：
  - `GET /api/v1/notices/my`
  - `POST /api/v1/notices/{noticeId}/read`
  - `POST /api/v1/notices/read-all`
- 文件：
  - `POST /api/v1/files/upload`
  - `GET /api/v1/files/{fileId}/download`
- 党团：
  - `GET /api/v1/party/flows`
  - `GET /api/v1/party/instances/me`
  - `POST /api/v1/party/stage-records/{stageRecordId}/materials`
- 知识库：
  - `GET /api/v1/kb/articles`
  - `POST /api/v1/kb/qa`
- 申请：
  - `GET /api/v1/applications/my`
  - `POST /api/v1/applications`
  - `GET /api/v1/applications/{applicationId}`
  - `POST /api/v1/applications/{applicationId}/revoke`
  - `GET /api/v1/applications/approvals/pending`
  - `POST /api/v1/applications/{applicationId}/approve`
  - `POST /api/v1/applications/{applicationId}/reject`
- 学生画像：
  - `GET /api/v1/students/me/profile`
  - `GET /api/v1/students`
  - `GET /api/v1/students/{studentId}`
  - `PUT /api/v1/students/{studentId}`
  - `GET /api/v1/students/{studentId}/growth-records`
  - `POST /api/v1/students/{studentId}/growth-records`
  - `PUT /api/v1/students/{studentId}/tags`
  - `POST /api/v1/students/import-tasks`
  - `GET /api/v1/students/import-tasks/{taskNo}`
- 字典：
  - `GET /api/v1/dicts`

### 2.1 数据库接入（当前先不使用人大金仓）

- 新增本地开发数据库：H2 file 模式，配置在 `src/main/resources/application.yml`
- 新增测试数据库：H2 mem 模式，配置在 `src/test/resources/application.yml`
- 新增 SQL 初始化脚本：
  - `src/main/resources/db/schema-h2.sql`
  - `src/main/resources/db/data-h2.sql`
- 新增 Maven 依赖：
  - `spring-boot-starter-jdbc`
  - `h2`
- 已将“院内申请与审批”模块从内存实现迁移为 JDBC 持久化：
  - `biz_application`
  - `biz_approval_record`
  - `cert_template`
  - 依赖演示用户 `sys_user` 与学生 `stu_student`
- 已把 `data/` 加入 `.gitignore`，避免提交本地 H2 数据文件。
- 注意：其余模块目前仍是内存演示实现，后续按模块逐步迁移。

### 3. 新增状态机与权限约束（本轮重点）

- 申请状态机（演示版）：
  - `submitted/reviewing -> approved/rejected`
  - `submitted/reviewing -> revoked`（仅申请人）
  - 非法跳转返回 `40900`
  - `create/revoke/approve/reject` 已使用事务边界；审批状态更新和审批记录写入必须同时成功或同时回滚。
- 学生写接口权限：
  - 成长记录新增：本人、班团骨干、管理角色可操作
  - 标签更新与导入任务：班团骨干或管理角色可操作（导入任务仅管理角色）
- 敏感字段读取：`includeSensitive=true` 仅 `teacher_admin` 可用

### 4. 测试现状

- 已添加 `MockMvc` 集成测试（`ApiIntegrationTest`）
- 已添加复杂逻辑 UnitTest：
  - `PartyServiceTest`（流程定义与当前阶段提交约束）
  - `StudentServiceTest`（成长记录新增、标签更新、导入任务权限）
  - `ApplicationServiceTest`（H2 持久化读回、撤回、待审批、审批通过、越权拦截、审批事务回滚）
  - `KbServiceTest`（问答无来源兜底）
  - `NoticeServiceTest`（已读/未读状态变化）
  - `FileServiceTest`（上传者权限与下载权限）
- 解决测试环境门禁：
  - 新增 `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
  - 配置 `mock-maker-subclass`，避免 JDK attach 限制导致 Mockito 初始化失败
- 验证命令：
  - `. "$HOME/.local/bin/java-maven-env.sh"`
  - `mvn -Dmaven.repo.local=/tmp/.m2/repository test -q`
  - 结果：通过

### 5. 前端联调支持

- 后端内置交互页：`/interaction-test.html`
- 本轮扩展交互项：
  - 党团流程定义查询
  - 学生成长记录新增、标签更新、导入任务查询
  - 申请撤回、待审批查询、审批通过/驳回
- 联调对齐文档：`docs/api-mvp-alignment.md`
- 沙箱验证说明：应用可完成启动流程但当前沙箱禁止端口监听，需在开发机本地执行 `spring-boot:run` 后访问交互页

### 6. 后续计划

- 补齐 `students/export` 与通知管理全量接口
- 将剩余内存实现逐步替换为数据库实现（对齐 `database/schema.sql`）
- 增加 RBAC 权限点校验与数据范围过滤中间层
- 引入 Flyway/Liquibase 管理数据库迁移，替换当前演示用 SQL 初始化策略
