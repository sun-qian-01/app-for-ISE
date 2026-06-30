# 学院学生综合服务与党团管理平台接口文档

## 1. 基础约定

基础路径：`/api/v1`

请求头：

- `Content-Type: application/json`
- `Authorization: Bearer <token>`

统一响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "202605111230001234"
}
```

分页响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "records": [],
    "pageNo": 1,
    "pageSize": 10,
    "total": 120
  },
  "requestId": "202605111230001234"
}
```

通用规则：

- 时间格式：`yyyy-MM-dd HH:mm:ss`
- 日期格式：`yyyy-MM-dd`
- 分页参数：`pageNo` 从 1 开始，`pageSize` 默认 10，最大 100
- 文件上传：先调用 `/files/upload` 获取 `fileId`，业务接口只传 `fileId`
- 敏感字段：后端统一脱敏，前端不能依赖隐藏字段实现权限
- 角色权限：接口必须校验 RBAC 和数据范围，前端只负责按权限码展示

## 2. 错误码

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 40001 | 参数校验失败 |
| 40100 | 未登录或 token 无效 |
| 40300 | 无权限访问 |
| 40400 | 资源不存在 |
| 40900 | 状态冲突，例如已审批申请再次审批 |
| 41300 | 文件过大 |
| 50000 | 系统内部错误 |

## 3. 认证与当前用户

### 3.1 登录

`POST /auth/login`

请求体：

```json
{
  "username": "20220001",
  "password": "123456"
}
```

响应 `data`：

```json
{
  "token": "jwt-token-demo",
  "user": {
    "id": 1,
    "username": "20220001",
    "realName": "赵晨曦",
    "userType": "student",
    "roles": ["student"],
    "permissions": ["student:profile:view"],
    "dataScopes": [{ "scopeType": "self", "scopeValue": "1" }]
  }
}
```

### 3.2 当前用户

`GET /auth/me`

返回当前用户、角色、权限码、菜单、数据范围、绑定学生信息摘要。

### 3.3 退出登录

`POST /auth/logout`

## 4. 文件接口

### 4.1 上传文件

`POST /files/upload`

请求类型：`multipart/form-data`

字段：

- `file`：文件
- `bizType`：业务类型，示例 `kb_policy`、`notice_attachment`、`party_material`、`template`

约束：

- 政策文件和模板支持 PDF、Word、Excel
- 政策文件单文件建议最大 30MB
- 返回 `fileId`、`fileName`、`fileUrl`、`fileSize`

### 4.2 文件下载

`GET /files/{fileId}/download`

后端按业务归属和用户权限校验下载资格。

## 5. 学生档案与画像

### 5.1 我的学生画像

`GET /students/me/profile`

响应 `data`：

```json
{
  "student": {
    "id": 1,
    "studentNo": "20220001",
    "name": "赵晨曦",
    "grade": "2022",
    "major": "软件工程",
    "className": "软件工程2班",
    "politicalStatus": "预备党员",
    "phoneMasked": "138****1234"
  },
  "tags": [{ "id": 1, "tagName": "就业意向" }],
  "growthSummary": {
    "competitionCount": 2,
    "volunteerHours": 36,
    "honorCount": 1
  },
  "todoCount": 3,
  "unreadNoticeCount": 2,
  "currentPartyStage": "预备党员"
}
```

### 5.2 学生列表

`GET /students`

查询参数：

- `pageNo`
- `pageSize`
- `name`
- `studentNo`
- `grade`
- `major`
- `className`
- `politicalStatus`
- `status`
- `tagId`
- `isGraduating`

权限：

- 学生不可调用
- 班团骨干只能查授权范围摘要
- 管理老师按数据范围查询

### 5.3 学生详情

`GET /students/{studentId}`

查询参数：

- `includeSensitive`：是否请求敏感字段，默认 `false`

约束：

- `includeSensitive=true` 仅管理老师可用，并写入审计日志
- 敏感字段包括身份证号、户籍地、生源地、导师、修学/延毕记录、联系方式等

### 5.4 更新学生基础信息

`PUT /students/{studentId}`

请求体示例：

```json
{
  "phone": "13800001234",
  "email": "student@example.com",
  "politicalStatus": "预备党员",
  "status": "active"
}
```

### 5.5 学生成长记录列表

`GET /students/{studentId}/growth-records`

查询参数：

- `recordType`：`competition`、`practice`、`volunteer`、`cadre`、`reward_punishment`

### 5.6 新增成长记录

`POST /students/{studentId}/growth-records`

```json
{
  "recordType": "volunteer",
  "title": "学院迎新志愿服务",
  "startDate": "2026-04-01",
  "endDate": "2026-04-02",
  "description": "累计服务 8 小时",
  "proofFileId": 10
}
```

### 5.7 更新学生标签

`PUT /students/{studentId}/tags`

```json
{
  "tagIds": [1, 2, 3]
}
```

### 5.8 学生信息导入

`POST /students/import-tasks`

```json
{
  "importType": "student_base",
  "fileId": 20
}
```

响应返回 `taskNo`，前端通过 `/students/import-tasks/{taskNo}` 查询结果。

### 5.9 导出学生信息

`GET /students/export`

查询参数同学生列表。后端必须按当前用户数据范围过滤。

## 6. 标签管理

### 6.1 标签列表

`GET /tags`

查询参数：

- `tagType`
- `keyword`

### 6.2 新建标签

`POST /tags`

```json
{
  "tagCode": "employment_target",
  "tagName": "就业意向",
  "tagType": "profile",
  "description": "用于推送实习与就业通知"
}
```

### 6.3 修改标签

`PUT /tags/{tagId}`

### 6.4 停用标签

`DELETE /tags/{tagId}`

## 7. 智能问答与知识库

### 7.1 知识库分类

`GET /kb/categories`

### 7.2 知识条目分页

`GET /kb/articles`

查询参数：

- `pageNo`
- `pageSize`
- `keyword`
- `categoryId`
- `publishStatus`
- `tag`

学生端只返回已发布且未过期条目。

### 7.3 知识条目详情

`GET /kb/articles/{articleId}`

响应需包含附件、来源、版本号、发布时间。

### 7.4 创建知识条目

`POST /kb/articles`

```json
{
  "categoryId": 1,
  "title": "国家奖学金评定流程说明",
  "summary": "包含申请资格、材料清单和公示流程",
  "standardAnswer": "请按学院通知提交成绩单、申请表和证明材料。",
  "content": "详细政策正文",
  "keywords": "奖学金,国家奖学金,评定",
  "sourceFileId": 12,
  "effectiveAt": "2026-04-01 00:00:00",
  "expireAt": "2026-12-31 23:59:59"
}
```

### 7.5 修改知识条目

`PUT /kb/articles/{articleId}`

约束：已发布条目修改时后端必须生成历史版本。

### 7.6 发布、停用、归档知识条目

- `POST /kb/articles/{articleId}/publish`
- `POST /kb/articles/{articleId}/disable`
- `POST /kb/articles/{articleId}/expire`

### 7.7 知识条目版本

`GET /kb/articles/{articleId}/versions`

### 7.8 智能问答

`POST /kb/qa`

```json
{
  "question": "国家奖学金需要提交哪些材料？",
  "categoryId": 1,
  "history": [
    {
      "role": "user",
      "content": "国家奖学金评定流程是什么？"
    },
    {
      "role": "assistant",
      "content": "国家奖学金评定流程以学院当年通知为准，通常包含申请、材料审核、公示等环节。"
    }
  ]
}
```

响应 `data`：

```json
{
  "answer": "国家奖学金通常需要提交申请表、成绩证明和相关获奖材料，具体以学院当年通知为准。",
  "sources": [
    {
      "articleId": 1,
      "title": "国家奖学金评定流程说明",
      "fileName": "国家奖学金评定办法.pdf",
      "sourceUrl": "/api/v1/files/12/download"
    }
  ],
  "confidence": 0.86
}
```

约束：没有来源时不得编造具体学院政策。RAG 开启时，后端仍会调用 AI 模型回答一般解释性问题，但 `sources` 为空且置信度较低；如果用户询问具体政策、流程、时间、材料且检索不到依据，应回答“未检索到可靠依据”或明确提示缺少可靠来源。

说明：`history` 为可选字段，用于连续追问场景。后端会截取最近对话作为检索上下文，但回答仍必须基于知识库来源或明确提示无可靠依据。

### 7.9 模板下载列表

`GET /kb/templates`

查询参数：

- `templateType`
- `categoryId`
- `keyword`

## 8. 党团事务流程

### 8.1 流程定义列表

`GET /party/flows`

返回入党、入团等流程及阶段定义。

### 8.2 我的党团进度

`GET /party/instances/me`

响应必须包含完整阶段数组，方便前端渲染线性流程。

```json
{
  "flowName": "入党流程",
  "instanceStatus": "processing",
  "currentStageCode": "probationary_party_member",
  "stages": [
    {
      "stageCode": "applicant",
      "stageName": "入党申请人",
      "stageOrder": 1,
      "stageStatus": "approved",
      "dueAt": "2026-04-25 23:59:59",
      "materials": []
    }
  ]
}
```

### 8.3 指定学生党团进度

`GET /party/instances/students/{studentId}`

权限：本人、授权班团骨干、管理老师可访问。

### 8.4 创建或调整流程实例

`POST /party/instances`

```json
{
  "flowId": 1,
  "studentId": 1,
  "branchName": "软件工程本科生第一党支部",
  "ownerUserId": 8
}
```

### 8.5 提交阶段材料

`POST /party/stage-records/{stageRecordId}/materials`

```json
{
  "materialName": "季度思想汇报",
  "fileId": 31
}
```

### 8.6 审核阶段材料

`POST /party/materials/{materialId}/review`

```json
{
  "action": "approve",
  "comment": "材料完整，同意通过"
}
```

`action` 可选：

- `approve`
- `return`

### 8.7 阶段审核

`POST /party/stage-records/{stageRecordId}/review`

```json
{
  "action": "approve",
  "comment": "支部审核通过"
}
```

`action` 可选：

- `approve`
- `return`
- `request_supplement`

### 8.8 撤回或重批

`POST /party/stage-records/{stageRecordId}/reopen`

```json
{
  "reason": "审批意见填写错误，需重新审核"
}
```

约束：仅管理老师可在限定时间内操作，后端必须保留原审批日志。

### 8.9 班团骨干催办列表

`GET /party/todos`

查询参数：

- `grade`
- `className`
- `branchName`
- `stageCode`
- `status`

## 9. 理论自测，可选

### 9.1 题目列表

`GET /party/exam/questions`

### 9.2 提交自测

`POST /party/exam/submissions`

```json
{
  "answers": [
    { "questionId": 1, "answer": "A" }
  ]
}
```

## 10. 通知与精准推送

### 10.1 我的通知

`GET /notices/my`

查询参数：

- `pageNo`
- `pageSize`
- `readStatus`
- `tag`

通知列表和详情返回 `deliveredCount`、`readCount`、`unreadCount`：

- `deliveredCount`：通知总人数，保留兼容字段。
- `readCount`：已读人数。
- `unreadCount`：未读人数，满足 `unreadCount + readCount = deliveredCount`。

### 10.2 标记已读

`POST /notices/{noticeId}/read`

### 10.3 全部标记已读

`POST /notices/read-all`

### 10.4 创建通知

`POST /notices`

```json
{
  "title": "2026 年春季学期奖学金材料提交通知",
  "content": "请于 4 月 24 日前完成材料提交，逾期系统将关闭入口。",
  "noticeType": "targeted",
  "tags": ["奖助", "2022级"],
  "publishAt": "2026-04-19 12:00:00",
  "expireAt": "2026-04-24 23:59:59",
  "attachmentFileIds": [41],
  "scopes": [
    { "scopeType": "grade", "scopeValue": "2022" },
    { "scopeType": "tag", "scopeValue": "scholarship_target" }
  ],
  "channels": ["site", "email", "wechat"]
}
```

后端职责：

- 保存草稿或定时发布
- 发布时计算接收人，写入总人数、已读人数和未读人数统计
- 记录每个渠道发送结果

### 10.5 通知管理分页

`GET /notices`

查询参数：

- `pageNo`
- `pageSize`
- `keyword`
- `status`
- `tag`
- `createdBy`
- `startTime`
- `endTime`

### 10.6 发布通知

`POST /notices/{noticeId}/publish`

### 10.7 归档通知

`POST /notices/{noticeId}/archive`

### 10.8 通知阅读与发送统计

`GET /notices/{noticeId}/stats`

响应包含总人数、已读、未读、发送成功、发送失败、各渠道统计。

## 11. 荣誉展示

### 11.1 公开荣誉列表

`GET /honors/public`

查询参数：

- `awardYear`
- `honorCategory`
- `ownerType`

### 11.2 管理端荣誉列表

`GET /honors`

### 11.3 创建荣誉

`POST /honors`

```json
{
  "honorType": "personal",
  "honorCategory": "国家奖学金",
  "title": "2025 年国家奖学金获得者",
  "awardYear": "2025",
  "ownerType": "student",
  "ownerStudentId": 1,
  "ownerName": "赵晨曦",
  "story": "积极参与科研竞赛和志愿服务。",
  "coverFileId": 51,
  "publicStatus": "hidden",
  "displayOrder": 10
}
```

### 11.4 发布或下线荣誉

- `POST /honors/{honorId}/publish`
- `POST /honors/{honorId}/unpublish`

## 12. 证明与院内审批

### 12.1 证明模板列表

`GET /certificates/templates`

查询参数：

- `templateType`
- `status`

### 12.2 新建证明模板

`POST /certificates/templates`

```json
{
  "templateCode": "student_status",
  "templateName": "在读证明",
  "templateType": "certificate",
  "fileId": 61,
  "formSchemaJson": "{\"fields\":[{\"name\":\"purpose\",\"label\":\"用途\"}]}",
  "flowRuleJson": "{\"approvers\":[\"counselor\"]}"
}
```

### 12.3 发起申请

`POST /applications`

```json
{
  "applicationType": "certificate",
  "templateId": 1,
  "title": "在读证明申请",
  "purpose": "实习单位提交材料",
  "formData": {
    "receiveOrg": "某科技公司"
  }
}
```

响应返回申请单号、当前状态、当前审批人和预览文件 ID。

### 12.4 我的申请列表

`GET /applications/my`

查询参数：

- `pageNo`
- `pageSize`
- `applicationType`
- `status`

### 12.5 申请详情

`GET /applications/{applicationId}`

响应包含申请信息、生成文件、审批记录。

### 12.6 撤回申请

`POST /applications/{applicationId}/revoke`

```json
{
  "reason": "用途变更"
}
```

约束：仅提交人可在允许撤回状态下撤回。

### 12.7 待审批列表

`GET /applications/approvals/pending`

查询参数：

- `pageNo`
- `pageSize`
- `applicationType`
- `status`
- `templateId`

### 12.8 审批通过

`POST /applications/{applicationId}/approve`

```json
{
  "comment": "材料齐全，同意通过"
}
```

### 12.9 审批驳回

`POST /applications/{applicationId}/reject`

```json
{
  "comment": "请补充身份证明材料"
}
```

## 13. 学业预警扩展

### 13.1 培养方案列表

`GET /academic/programs`

### 13.2 学业预警列表

`GET /academic/warnings`

查询参数：

- `studentId`
- `warningLevel`
- `status`

说明：该模块为 P2 扩展，一期可以只返回手工录入或演示数据。

## 14. 首页聚合

### 14.1 学生端首页

`GET /dashboard/student`

返回：

- 待办数
- 未读通知数
- 近期通知
- 当前党团阶段
- 待提交材料
- 申请处理进度
- 标签画像摘要

### 14.2 管理端首页

`GET /dashboard/admin`

返回：

- 学生总数
- 待审批数
- 今日推送数
- 风险预警数
- 待处理任务
- 热门知识库条目
- 通知阅读统计

### 14.3 领导端首页

`GET /dashboard/leader`

返回学院维度汇总数据，不返回学生敏感明细。

## 15. 审计日志

### 15.1 审计日志分页

`GET /audit-logs`

查询参数：

- `pageNo`
- `pageSize`
- `userId`
- `moduleCode`
- `actionCode`
- `targetType`
- `targetId`
- `startTime`
- `endTime`

必须写审计的动作：

- 管理端新增、修改、删除
- 批量导入和导出
- 审批通过、驳回、撤回、重批
- 查看学生敏感字段
- 通知发布和归档
- 知识库发布、停用和版本替换

## 16. 系统事件与异常日志

系统事件日志用于排查平台问题，和审计日志分开。审计日志关注业务操作责任，系统事件日志关注错误、异常、任务失败和请求链路。

### 16.1 系统事件日志分页

`GET /system-logs`

查询参数：

- `pageNo`
- `pageSize`
- `eventType`
- `eventLevel`
- `moduleCode`
- `eventCode`
- `userId`
- `requestId`
- `traceId`
- `startTime`
- `endTime`

响应字段建议：

- `id`
- `eventType`
- `eventLevel`
- `moduleCode`
- `eventCode`
- `eventMessage`
- `userId`
- `usernameSnapshot`
- `realNameSnapshot`
- `requestId`
- `traceId`
- `requestMethod`
- `requestPath`
- `requestIp`
- `targetType`
- `targetId`
- `errorClass`
- `errorMessage`
- `occurredAt`

权限：仅管理老师、学院领导和系统管理员可查看；普通学生和班团骨干不可查看。

### 16.2 系统事件日志详情

`GET /system-logs/{logId}`

响应在列表字段基础上增加：

- `stackTrace`
- `requestBodyDigest`
- `extraJson`

约束：

- `stackTrace` 仅系统管理员和具备排障权限的管理人员可查看。
- 日志详情不得返回密码、token、身份证号等敏感原文。

### 16.3 前端错误上报

`POST /system-logs/client-errors`

请求体：

```json
{
  "eventLevel": "error",
  "moduleCode": "frontend",
  "eventCode": "route_render_failed",
  "eventMessage": "学生画像页面渲染失败",
  "requestId": "202605111230001234",
  "requestPath": "/student/profile",
  "errorClass": "TypeError",
  "errorMessage": "Cannot read properties of undefined",
  "stackTrace": "sanitized stack trace",
  "extra": {
    "browser": "Chrome",
    "route": "/student/profile"
  }
}
```

约束：

- 前端不得上传密码、token、身份证号、完整手机号等敏感原文。
- 后端应补充当前登录用户、IP、发生时间。
- 未登录状态也允许上报，但只能记录匿名用户信息。

### 16.4 需要自动写系统日志的场景

- 未捕获异常
- 接口 5xx
- 登录异常和账号锁定
- 权限异常
- 文件上传、解析、下载失败
- 批量导入任务失败
- 定时任务失败
- 邮件、微信、短信模拟发送失败
- 前端运行时错误上报

## 17. 枚举字典接口

### 17.1 字典批量获取

`GET /dicts`

查询参数：

- `types`：逗号分隔，例如 `student_status,notice_status,application_status`

响应：

```json
{
  "student_status": [
    { "value": "active", "label": "在读" },
    { "value": "graduated", "label": "毕业" }
  ],
  "application_status": [
    { "value": "submitted", "label": "已提交" },
    { "value": "approved", "label": "已通过" }
  ]
}
```

前端不得在多个页面重复硬编码同一套枚举中文。
