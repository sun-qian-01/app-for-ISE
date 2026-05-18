# API 联调对齐说明（MVP）

本文档用于前端快速联调，说明“当前已实现且可用”的 API，以及与 `docs/api.md` 的差异。

## 1. 已实现接口清单

### 1.1 认证

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

说明：

- `GET /auth/me` 已返回 `menus`、`roles`、`permissions`、`dataScopes`、`studentSummary`。

### 1.2 首页

- `GET /api/v1/dashboard/student`
- `GET /api/v1/dashboard/admin`
- `GET /api/v1/dashboard/leader`

### 1.3 文件

- `POST /api/v1/files/upload`（`multipart/form-data`）
  - 字段：`file`、`bizType`
- `GET /api/v1/files/{fileId}/download`

### 1.4 学生画像

- `GET /api/v1/students/me/profile`
- `GET /api/v1/students`
- `GET /api/v1/students/{studentId}`
- `PUT /api/v1/students/{studentId}`
- `GET /api/v1/students/{studentId}/growth-records`
- `POST /api/v1/students/{studentId}/growth-records`
- `PUT /api/v1/students/{studentId}/tags`
- `POST /api/v1/students/import-tasks`
- `GET /api/v1/students/import-tasks/{taskNo}`

说明：

- `includeSensitive=true` 仅 `teacher_admin` 可用（符合主文档约束）。
- 导入任务当前为演示实现，创建后会返回已完成状态。

### 1.5 知识库

- `GET /api/v1/kb/articles`
- `POST /api/v1/kb/qa`

说明：

- 无可靠来源时返回固定文案：`未检索到可靠依据`。

### 1.6 党团

- `GET /api/v1/party/flows`
- `GET /api/v1/party/instances/me`
- `POST /api/v1/party/stage-records/{stageRecordId}/materials`

说明：

- 学生仅可提交当前阶段材料；否则返回 `40900`。

### 1.7 通知

- `GET /api/v1/notices/my`
- `POST /api/v1/notices/{noticeId}/read`
- `POST /api/v1/notices/read-all`

### 1.8 申请

- `GET /api/v1/applications/my`
- `POST /api/v1/applications`
- `GET /api/v1/applications/{applicationId}`
- `POST /api/v1/applications/{applicationId}/revoke`
- `GET /api/v1/applications/approvals/pending`
- `POST /api/v1/applications/{applicationId}/approve`
- `POST /api/v1/applications/{applicationId}/reject`

说明：

- 该模块当前已接入 H2 本地数据库，数据会写入 `biz_application` 与 `biz_approval_record`。
- 已实现基本状态机约束：
  - 可撤回：`submitted`、`reviewing`
  - 可审批：`submitted`、`reviewing`
  - 非法状态流转返回 `40900`
- `create/revoke/approve/reject` 已纳入事务控制；状态变更和审批记录必须同时成功。

### 1.9 字典

- `GET /api/v1/dicts`

## 2. 响应约定

所有 JSON 接口统一：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "..."
}
```

分页字段统一：

- `records`
- `pageNo`
- `pageSize`
- `total`

## 3. 当前差异与说明

前端联调请注意以下差异：

1. 当前只有“申请”模块使用 H2 持久化；其他模块仍为内存版实现，演示数据会在服务重启后恢复初始状态。
2. 文件下载为二进制响应（非 `ApiResponse`），与主文档一致。
3. 以下接口的返回体为联调友好扩展：
   - `PUT /students/{studentId}` 当前返回更新后的学生详情。
   - `POST /applications/{id}/revoke|approve|reject` 当前返回 `applicationNo/status/currentApprover`。
4. 仍未覆盖 `docs/api.md` 的全部接口（例如 `students/export`、通知管理全量、知识库版本管理、日志中心等）。
5. 当前开发阶段先不使用人大金仓；H2 只作为本地开发与测试数据库，正式部署前需要补充 Kingbase 连接配置和迁移脚本。

## 4. 快速交互测试页面

后端已内置联调页面：

- `http://localhost:8080/interaction-test.html`

页面已支持：

- 登录与 token 管理
- 核心查询接口按钮调用
- 文件上传与党团材料提交串联测试
- 学生成长记录新增、标签更新、导入任务创建与查询
- 申请创建、详情、撤回、待审批查询、审批通过/驳回
