# 接口文档草案

## 1. 基础约定

### 1.1 基础路径

- 本地开发建议前缀：`/api/v1`

### 1.2 请求头

- `Content-Type: application/json`
- `Authorization: Bearer <token>`

### 1.3 统一响应格式

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "202604191210001234"
}
```

### 1.4 分页格式

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "records": [],
    "pageNo": 1,
    "pageSize": 10,
    "total": 120
  }
}
```

## 2. 认证与用户

### 2.1 登录

- `POST /api/v1/auth/login`

请求体：

```json
{
  "username": "20220001",
  "password": "123456"
}
```

响应体：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt-token-demo",
    "user": {
      "id": 1,
      "username": "20220001",
      "realName": "赵晨曦",
      "userType": "student",
      "roles": ["student"]
    }
  }
}
```

### 2.2 获取当前登录用户

- `GET /api/v1/auth/me`

### 2.3 退出登录

- `POST /api/v1/auth/logout`

## 3. 学生档案

### 3.1 获取当前学生画像

- `GET /api/v1/students/me/profile`

响应字段：

- 基本信息
- 标签列表
- 当前党团阶段
- 未读通知数
- 待办数

### 3.2 学生列表

- `GET /api/v1/students`

查询参数：

- `pageNo`
- `pageSize`
- `name`
- `studentNo`
- `grade`
- `major`
- `className`
- `status`
- `tagId`

### 3.3 学生详情

- `GET /api/v1/students/{studentId}`

### 3.4 更新学生标签

- `PUT /api/v1/students/{studentId}/tags`

请求体：

```json
{
  "tagIds": [1, 2, 3]
}
```

## 4. 知识库

### 4.1 知识库分类列表

- `GET /api/v1/kb/categories`

### 4.2 知识库文章分页查询

- `GET /api/v1/kb/articles`

查询参数：

- `pageNo`
- `pageSize`
- `keyword`
- `categoryId`
- `publishStatus`

### 4.3 知识库文章详情

- `GET /api/v1/kb/articles/{articleId}`

### 4.4 创建知识库文章

- `POST /api/v1/kb/articles`

请求体：

```json
{
  "categoryId": 1,
  "title": "休学与复学办理指南",
  "summary": "说明休学申请条件、复学材料和学院审核路径",
  "content": "详细内容",
  "keywords": "休学,复学,学籍"
}
```

### 4.5 发布知识库文章

- `POST /api/v1/kb/articles/{articleId}/publish`

## 5. 通知中心

### 5.1 当前用户通知分页

- `GET /api/v1/notices/my`

查询参数：

- `pageNo`
- `pageSize`
- `readStatus`

### 5.2 标记通知已读

- `POST /api/v1/notices/{noticeId}/read`

### 5.3 全部标记已读

- `POST /api/v1/notices/read-all`

### 5.4 创建通知

- `POST /api/v1/notices`

请求体：

```json
{
  "title": "2026 年春季学期奖学金材料提交通知",
  "content": "请于 4 月 24 日前完成材料提交",
  "noticeType": "targeted",
  "publishAt": "2026-04-19 12:00:00",
  "scopeList": [
    { "scopeType": "grade", "scopeValue": "2022" },
    { "scopeType": "tag", "scopeValue": "scholarship_target" }
  ]
}
```

### 5.5 通知管理分页

- `GET /api/v1/notices`

## 6. 党团流程

### 6.1 获取当前学生党团进度

- `GET /api/v1/party-progress/me`

### 6.2 获取指定学生党团进度

- `GET /api/v1/party-progress/{studentId}`

### 6.3 更新党团当前阶段

- `PUT /api/v1/party-progress/{studentId}`

请求体：

```json
{
  "currentStage": "probationary_party_member",
  "currentStatus": "processing"
}
```

### 6.4 新增节点记录

- `POST /api/v1/party-progress/{studentId}/records`

请求体：

```json
{
  "stageName": "预备党员",
  "stageOrder": 4,
  "status": "completed",
  "reviewComment": "支部大会通过"
}
```

## 7. 证明申请与审批

### 7.1 证明模板列表

- `GET /api/v1/certificates/templates`

### 7.2 发起证明申请

- `POST /api/v1/certificates/applications`

请求体：

```json
{
  "templateId": 1,
  "purpose": "实习单位提交材料",
  "extraData": {
    "receiveOrg": "某科技公司"
  }
}
```

### 7.3 我的证明申请列表

- `GET /api/v1/certificates/applications/my`

### 7.4 证明申请详情

- `GET /api/v1/certificates/applications/{applicationId}`

### 7.5 撤回证明申请

- `POST /api/v1/certificates/applications/{applicationId}/revoke`

请求体：

```json
{
  "reason": "用途变更"
}
```

### 7.6 待审批列表

- `GET /api/v1/certificates/approvals/pending`

查询参数：

- `pageNo`
- `pageSize`
- `status`
- `templateId`

### 7.7 审批通过

- `POST /api/v1/certificates/applications/{applicationId}/approve`

请求体：

```json
{
  "comment": "材料齐全，同意通过"
}
```

### 7.8 审批驳回

- `POST /api/v1/certificates/applications/{applicationId}/reject`

请求体：

```json
{
  "comment": "请补充身份证明材料"
}
```

## 8. 标签管理

### 8.1 标签列表

- `GET /api/v1/tags`

### 8.2 新建标签

- `POST /api/v1/tags`

请求体：

```json
{
  "tagCode": "employment_target",
  "tagName": "就业意向",
  "tagType": "profile",
  "description": "用于推送实习与就业通知"
}
```

## 9. 审计日志

### 9.1 审计日志分页

- `GET /api/v1/audit-logs`

查询参数：

- `pageNo`
- `pageSize`
- `userId`
- `moduleCode`
- `actionCode`
- `startTime`
- `endTime`

## 10. 首页聚合接口

### 10.1 学生端首页

- `GET /api/v1/dashboard/student`

返回内容建议：

- 待办数
- 未读通知数
- 快捷入口
- 近期通知
- 当前党团阶段
- 标签画像

### 10.2 管理端首页

- `GET /api/v1/dashboard/admin`

返回内容建议：

- 学生总数
- 待审批数
- 今日推送数
- 风险预警数
- 待处理任务
- 热门知识库条目

## 11. 错误码建议

| 错误码 | 含义 |
| --- | --- |
| 0 | 成功 |
| 40001 | 参数校验失败 |
| 40100 | 未登录或 token 无效 |
| 40300 | 无权限访问 |
| 40400 | 资源不存在 |
| 40900 | 状态冲突 |
| 50000 | 系统内部错误 |
