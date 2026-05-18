# 学院学生综合服务与党团管理平台

## 1. 项目定位

本项目面向学院内部学生事务与党团事务管理场景，目标是建设一个前后端分离的 Web 网站平台。平台服务对象包括学院全体在校本科生、研究生、班团骨干、管理老师和学院领导，覆盖政策咨询、党团流程、学生画像、通知触达、奖励荣誉、院内申请审批、日志审计等核心业务。

当前仓库包含静态 Web 原型、需求与设计文档、接口文档、权限文档、枚举字典和 Kingbase 数据库设计基线，可作为前端、后端、数据库和测试人员的开发交接资料。

## 2. 需求来源

需求依据：

- `学院学生综合服务与党团管理平台-需求描述完善版.docx`
- `docs/architecture.md`
- `docs/api.md`
- `docs/database.md`
- `docs/frontend-spec.md`
- `docs/role-permission.md`
- `docs/dictionary.md`
- `database/schema.sql`

甲方需求中明确：一期平台面向学院内部使用，不直接对接校级“微人大”等外部系统；产品形态采用前后端分离 Web 网站，同时兼顾手机端与电脑端访问；数据库采用人大金仓 Kingbase；学生、政策、模板、通知等数据通过后台维护、文件上传、Excel/Word/PDF 导入导出等方式进入系统。

## 3. 目标用户

| 用户类型 | 说明 | 核心诉求 |
| --- | --- | --- |
| 普通学生 | 学院全体在校本科生及研究生 | 查询政策、查看通知、提交党团材料、发起院内申请、查看个人档案 |
| 班团骨干 | 班长、团支书等 | 在授权班级或支部范围内查看进展、协助催办和信息收集 |
| 管理老师 | 辅导员、班主任、教学秘书、团委老师等 | 维护知识库、管理学生画像、发布通知、审核材料和申请 |
| 学院领导 | 学院管理人员 | 查看学院维度运行情况、统计结果和重点问题 |
| 系统管理员 | 平台维护人员 | 管理账号、角色、权限、字典、日志和系统配置 |

## 4. 核心建设目标

- 建设“一站式”学院学生服务窗口。
- 实现学生事务办理线上化。
- 实现政策咨询和模板查询智能化。
- 实现党团流程可视化和过程留痕。
- 实现通知精准推送和阅读追踪。
- 实现学生数据统一管理和标签画像。
- 通过角色权限、数据范围、敏感字段脱敏、审计日志和系统日志保障安全与可追溯。

## 5. 功能需求范围

### 5.1 P0：一期核心功能

| 模块 | 主要需求 |
| --- | --- |
| 登录与权限 | 支持学号/工号登录，登录后根据角色展示不同菜单；后端按权限码和数据范围做强校验。 |
| 智能问答与知识库 | 管理员维护政策文件、标准答案、知识条目、模板文件；学生可检索政策并查看问答来源。 |
| 党团事务流程 | 展示入党、入团等固定线性流程；学生提交阶段材料；老师审核、退回、补充说明；过程全量留痕。 |
| 通知与消息触达 | 管理老师创建通知，按年级、班级、标签、政治面貌等精准投放；记录已读、未读、发送失败。 |
| 审计与系统日志 | 管理端关键操作写审计日志；系统异常、接口错误、任务失败、前端报错写系统事件日志。 |

### 5.2 P1：一期重要功能

| 模块 | 主要需求 |
| --- | --- |
| 学生画像与信息管理 | 维护学生基础信息、敏感扩展信息、标签、成长记录；支持 Excel 导入导出和多维检索。 |
| 奖励荣誉展示 | 管理个人或集体荣誉，支持年份、类别、公开状态、展示顺序和展示时效。 |
| 院内申请与审批 | 支持证明、请假、盖章等院内申请流转；证明可由模板自动生成；审批记录可追溯。 |
| 文件上传与模板下载 | 支持 PDF、Word、Excel 上传、下载和归档；政策文件建议单文件不超过 30MB。 |

### 5.3 P2：后续扩展功能

| 模块 | 主要需求 |
| --- | --- |
| 学业分析与预警 | 录入培养方案，解析成绩单，识别课程或学分缺口，生成预警和选课建议。 |
| 理论自测 | 导入党建/团建题库，供学生学习和自测。 |

P2 功能依赖数据完整性和业务规则稳定性，不作为一期主线交付重点。

## 6. 非功能需求

| 类别 | 要求 |
| --- | --- |
| 安全性 | 密码不可明文存储；身份证号、联系方式、户籍地、导师、修学/延毕记录等敏感字段需加密或脱敏展示。 |
| 权限控制 | 平台按普通学生、班团骨干、管理老师、学院领导、系统管理员进行角色划分；后端必须做权限和数据范围校验。 |
| 日志追踪 | 管理操作写 `audit_log`；系统事件和异常写 `system_event_log`，用于定位问题发生时间、操作人员和请求链路。 |
| 并发与规模 | 支持学院约 1200 人使用，在通知发布、材料集中提交等高峰时段保持稳定。 |
| 可维护性 | 模块化设计，避免补丁式开发；状态、权限、字典和接口需统一维护。 |
| 可靠性 | 关键业务状态变更必须具备事务控制、明确反馈和失败日志。 |
| 易用性 | 学生端适配手机端高频操作；管理端适配 PC 端批量维护、筛选、审批和统计。 |

## 7. 正式 Web 前端建议

本项目目标是 Web 网站，不是小程序。正式开发建议新建前端工程，当前静态页面作为原型参考。

推荐路由：

```text
/login
/student/dashboard
/student/kb
/student/party
/student/notices
/student/applications
/student/profile
/student/honors

/admin/dashboard
/admin/students
/admin/kb
/admin/party
/admin/notices
/admin/applications
/admin/honors
/admin/audit-logs
/admin/system-logs

/leader/dashboard
```

前端详细要求见 `docs/frontend-spec.md`。

## 8. 后端与数据库建议

后端以 RESTful JSON API 提供能力，统一路径前缀为 `/api/v1`。认证采用登录获取 token、后续请求携带 `Authorization: Bearer <token>` 的方式。接口响应统一包含 `code`、`message`、`data`、`requestId`。

数据库采用 Kingbase，核心设计见：

- `database/schema.sql`
- `docs/database.md`
- `docs/dictionary.md`

后端必须重点保证：

- 登录鉴权。
- RBAC 权限校验。
- 数据范围过滤。
- 对象归属校验。
- 敏感字段脱敏。
- 审计日志。
- 系统事件与异常日志。
- 文件上传下载权限控制。
- 申请和流程状态机约束。

## 9. 仓库文件说明

| 路径 | 说明 |
| --- | --- |
| `index.html` | 静态 Web 原型入口页。 |
| `student.html` | 学生端静态原型。 |
| `admin.html` | 管理端静态原型。 |
| `styles.css` | 静态原型样式。 |
| `scripts/app.js` | 静态原型模拟数据和交互逻辑。正式开发时不应作为业务代码直接使用。 |
| `docs/architecture.md` | 总体架构、业务域、权限和技术边界。 |
| `docs/api.md` | REST API 接口契约。 |
| `docs/database.md` | 数据库设计说明。 |
| `docs/frontend-spec.md` | Web 前端开发说明。 |
| `docs/role-permission.md` | 角色权限矩阵。 |
| `docs/dictionary.md` | 枚举字典和错误码。 |
| `database/schema.sql` | Kingbase/PostgreSQL 兼容数据库建表脚本。 |

## 10. 静态原型运行方式

直接在浏览器中打开：

- `index.html`
- `student.html`
- `admin.html`

推荐从 `index.html` 进入。静态原型使用本地 mock 数据，不连接真实后端，不具备真实登录、持久化和权限校验能力。

## 11. 前后端交接说明

前后端开发请以 `docs/architecture.md` 作为总体架构边界，以 `docs/api.md` 作为接口联调契约，以 `database/schema.sql` 和 `docs/database.md` 作为数据库设计依据，并参考 `docs/frontend-spec.md`、`docs/role-permission.md`、`docs/dictionary.md` 完成页面路由、权限控制、枚举状态和交互细节实现。

开发过程中请重点保证登录鉴权、角色权限、数据范围隔离、敏感字段脱敏、审计日志、系统异常日志、文件上传下载、党团流程留痕、通知精准推送和院内申请审批状态流转的一致性。前端不得绕过接口直接假定权限或状态，后端需对所有接口做权限校验、归属校验和日志记录。

## 12. 后端开发启动说明（MVP）

仓库已新增后端工程骨架（Spring Boot, Java 17, Maven）：

- `pom.xml`
- `src/main/java/com/ise/platform/**`
- `src/test/java/com/ise/platform/**`

本地运行：

```bash
mvn spring-boot:run
```

测试运行：

```bash
mvn test
```

默认地址：

- `http://localhost:8080`
- API 前缀：`/api/v1`

当前已实现首批可联调接口（内存版实现，后续可替换数据库）：

- 认证：`POST /api/v1/auth/login`、`GET /api/v1/auth/me`、`POST /api/v1/auth/logout`
- 首页：`GET /api/v1/dashboard/student`、`GET /api/v1/dashboard/admin`、`GET /api/v1/dashboard/leader`
- 通知：`GET /api/v1/notices/my`、`POST /api/v1/notices/{noticeId}/read`、`POST /api/v1/notices/read-all`
- 党团：`GET /api/v1/party/instances/me`、`POST /api/v1/party/stage-records/{stageRecordId}/materials`
- 知识库：`GET /api/v1/kb/articles`、`POST /api/v1/kb/qa`
- 申请：`GET /api/v1/applications/my`、`POST /api/v1/applications`
