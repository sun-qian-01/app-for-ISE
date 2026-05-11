# 前端开发说明

本文档面向 Web 前端开发人员，说明页面结构、路由、组件、交互和接口对接要求。项目目标是开发网站，不是小程序。

## 1. 技术建议

推荐技术栈：

- Vue 3 + Vite + Vue Router + Pinia
- 或 React + Vite + React Router + Zustand / Redux Toolkit

无论使用哪种框架，都必须具备：

- 路由守卫。
- 登录 token 管理。
- `/auth/me` 当前用户状态恢复。
- API client 统一封装。
- 权限码控制菜单和按钮。
- 列表页统一分页、筛选、加载、空状态、错误状态。

## 2. 页面风格

当前静态原型文件可作为视觉参考：

- `index.html`
- `student.html`
- `admin.html`
- `styles.css`

视觉要求：

- 保持米色背景、酒红主色、低饱和卡片、侧边栏导航的现有风格。
- 学生端优先适配手机和窄屏。
- 管理端优先适配 PC，保留较高信息密度。
- 表格、筛选区、表单、审批动作区需要清晰紧凑。
- 不要将静态 `scripts/app.js` 作为正式业务代码；正式代码应改为 API 调用。

## 3. 路由结构

### 3.1 公共路由

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/login` | 登录页 | 学号/工号 + 密码登录。 |
| `/` | 入口页 | 可根据登录状态跳转，未登录跳 `/login`。 |
| `/403` | 无权限页 | 用户无权限访问时展示。 |
| `/404` | 未找到页 | 路由不存在时展示。 |

### 3.2 学生端路由

| 路由 | 页面 | 主要接口 |
| --- | --- | --- |
| `/student/dashboard` | 学生首页 | `GET /dashboard/student` |
| `/student/kb` | 智能问答与知识库 | `POST /kb/qa`、`GET /kb/articles`、`GET /kb/templates` |
| `/student/party` | 党团流程 | `GET /party/instances/me`、`POST /party/stage-records/{id}/materials` |
| `/student/notices` | 通知中心 | `GET /notices/my`、`POST /notices/{id}/read` |
| `/student/applications` | 院内申请 | `GET /applications/my`、`POST /applications` |
| `/student/profile` | 个人画像 | `GET /students/me/profile`、`GET /students/{id}/growth-records` |
| `/student/honors` | 奖励荣誉 | `GET /honors/public` |

### 3.3 管理端路由

| 路由 | 页面 | 主要接口 |
| --- | --- | --- |
| `/admin/dashboard` | 管理首页 | `GET /dashboard/admin` |
| `/admin/students` | 学生画像管理 | `GET /students`、`GET /students/{id}`、`POST /students/import-tasks`、`GET /students/export` |
| `/admin/kb` | 知识库管理 | `GET /kb/articles`、`POST /kb/articles`、`PUT /kb/articles/{id}` |
| `/admin/party` | 党团流程管理 | `GET /party/todos`、`POST /party/stage-records/{id}/review` |
| `/admin/notices` | 精准通知 | `GET /notices`、`POST /notices`、`GET /notices/{id}/stats` |
| `/admin/applications` | 审批处理 | `GET /applications/approvals/pending`、`POST /applications/{id}/approve`、`POST /applications/{id}/reject` |
| `/admin/honors` | 荣誉管理 | `GET /honors`、`POST /honors`、`POST /honors/{id}/publish` |
| `/admin/audit-logs` | 审计日志 | `GET /audit-logs` |
| `/admin/system-logs` | 系统日志 | `GET /system-logs`、`GET /system-logs/{id}` |
| `/admin/dicts` | 字典维护，可选 | `GET /dicts` |

### 3.4 领导端路由

| 路由 | 页面 | 主要接口 |
| --- | --- | --- |
| `/leader/dashboard` | 领导看板 | `GET /dashboard/leader` |

领导端只展示学院汇总数据，不默认展示学生敏感明细。

## 4. 登录与权限

登录流程：

1. 用户访问受保护路由。
2. 如果本地无 token，跳转 `/login`。
3. 登录调用 `POST /auth/login`。
4. 保存 token。
5. 调用 `GET /auth/me` 获取用户、角色、权限码、数据范围。
6. 根据角色跳转默认首页。

默认跳转：

- `student`：`/student/dashboard`
- `class_cadre`：优先进入 `/student/dashboard`，额外展示协同入口。
- `teacher_admin`：`/admin/dashboard`
- `college_leader`：`/leader/dashboard` 或 `/admin/dashboard`

前端权限要求：

- 菜单由角色和权限码控制。
- 按钮由权限码控制。
- 前端隐藏不是安全措施；后端仍必须校验。
- 遇到接口 `40300` 跳转或展示无权限提示。

## 5. 页面详细说明

### 5.1 登录页

字段：

- 用户名：学号或工号。
- 密码。
- 登录按钮。

状态：

- 登录中。
- 账号或密码错误。
- token 失效后重新登录。

### 5.2 学生首页

展示：

- 待办数。
- 未读通知数。
- 当前党团阶段。
- 成长记录摘要。
- 近期通知。
- 待提交材料。
- 申请处理进度。
- 标签画像摘要。

交互：

- 点击通知进入通知详情或通知中心。
- 点击材料待办进入党团流程页。
- 点击申请进度进入院内申请详情。

### 5.3 智能问答与知识库

展示：

- 问题输入框。
- 回答结果。
- 来源列表。
- 知识条目列表。
- 分类、关键词筛选。
- 模板下载列表。

交互：

- 提问调用 `POST /kb/qa`。
- 回答必须展示来源。
- 没有可靠来源时展示“未检索到可靠依据”。
- 模板下载调用文件下载接口。

### 5.4 党团流程

展示：

- 完整线性阶段。
- 当前阶段。
- 每个阶段状态、截止时间、审核意见。
- 当前阶段材料列表。

交互：

- 学生只能给本人当前阶段提交材料。
- 上传文件先调用 `/files/upload`。
- 提交材料再调用党团材料接口。
- 已审核通过材料不可重复覆盖，只能追加补充材料。

### 5.5 通知中心

展示：

- 通知列表。
- 标题、发布时间、标签、附件、阅读状态。
- 渠道触达信息，可选。

交互：

- 标记单条已读。
- 全部标记已读。
- 按已读、未读、标签筛选。

### 5.6 院内申请

展示：

- 申请类型。
- 动态表单字段。
- 我的申请列表。
- 申请详情。
- 审批记录。
- 生成文件下载。

交互：

- 发起申请调用 `POST /applications`。
- 撤回申请调用 `POST /applications/{id}/revoke`。
- 不允许前端直接修改申请状态。

### 5.7 个人画像

展示：

- 基础档案。
- 标签。
- 成长记录。
- 脱敏联系方式。
- 党团状态。

交互：

- 普通学生只查看本人。
- 成长记录新增是否开放由权限控制。

### 5.8 学生画像管理

展示：

- 学生列表。
- 筛选条件：姓名、学号、年级、专业、班级、政治面貌、标签、毕业年级。
- 学生详情抽屉或详情页。
- 标签维护。
- 成长记录。

交互：

- 查看敏感字段需要显式按钮，例如“查看完整联系方式”。
- 查看敏感字段成功后后端写审计，前端展示“本次访问已记录”。
- 导入 Excel 后展示任务进度、成功数、失败数和错误文件。

### 5.9 知识库管理

展示：

- 分类树。
- 条目列表。
- 版本号。
- 发布状态。
- 来源文件。
- 模板文件。

交互：

- 新增、编辑、发布、停用、过期。
- 已发布条目修改后应提示会生成历史版本。
- 上传政策文件先调用 `/files/upload`。

### 5.10 党团流程管理

展示：

- 待审核材料。
- 待审核阶段。
- 班团骨干催办列表。
- 学生流程详情。

交互：

- 通过。
- 退回。
- 要求补充。
- 限时撤回或重批。
- 班团骨干只能查看和催办，不能审批。

### 5.11 精准通知管理

展示：

- 通知列表。
- 创建通知表单。
- 目标范围规则。
- 渠道选择。
- 阅读与发送统计。

交互：

- 保存草稿。
- 定时发布。
- 立即发布。
- 归档。
- 查看已读、未读、失败明细。

### 5.12 审批处理

展示：

- 待审批列表。
- 申请详情。
- 申请人信息摘要。
- 附件和生成文件。
- 审批记录。

交互：

- 通过。
- 驳回。
- 查看生成文件。
- 状态冲突时刷新详情。

### 5.13 荣誉展示与管理

学生端展示：

- 年份。
- 荣誉类别。
- 个人或集体名称。
- 先进事迹。
- 图片。

管理端交互：

- 新增。
- 编辑。
- 发布。
- 下线。
- 排序。
- 设置展示时效。

### 5.14 审计日志

展示：

- 操作人。
- 模块。
- 动作。
- 对象。
- 时间。
- IP。
- 结果。

交互：

- 按模块、动作、时间、操作人筛选。
- 导出日志需按权限控制。

### 5.15 系统日志

用途：

- 平台出问题时，定位发生时间、操作人员、请求编号、接口路径和错误原因。
- 和审计日志区分：审计日志看业务操作，系统日志看事件和报错。

展示：

- 发生时间。
- 日志级别。
- 事件类型。
- 模块。
- 事件编码。
- 事件摘要。
- 操作人员。
- `requestId`。
- `traceId`。
- 请求路径。
- IP。
- 错误类型。
- 错误信息。

交互：

- 按时间、级别、类型、模块、用户、`requestId` 筛选。
- 点击查看详情。
- 详情页展示堆栈摘要、额外上下文、关联对象。
- 支持复制 `requestId`，方便沟通问题。
- 普通管理人员默认不展示完整堆栈；系统管理员或排障权限人员可查看。

前端错误上报：

- 前端应捕获运行时错误、路由渲染错误、接口异常，并调用 `POST /system-logs/client-errors`。
- 上报内容必须脱敏，不得上传 token、密码、身份证号、完整手机号。
- 全局错误提示中应展示后端返回的 `requestId`。

## 6. 通用组件建议

- `AppLayout`：整体布局。
- `SideNav`：侧边导航。
- `PageHeader`：页面标题和操作区。
- `MetricCard`：指标卡。
- `DataTable`：列表表格。
- `SearchBar`：筛选区。
- `StatusTag`：状态标签。
- `FileUploader`：文件上传。
- `AuditNotice`：敏感字段访问提示。
- `ApprovalActions`：审批按钮区。
- `EmptyState`：空状态。
- `ErrorState`：错误状态。

## 7. 接口封装建议

建议按模块拆 API 文件：

- `authApi`
- `fileApi`
- `studentApi`
- `tagApi`
- `kbApi`
- `partyApi`
- `noticeApi`
- `applicationApi`
- `honorApi`
- `dashboardApi`
- `auditApi`
- `systemLogApi`
- `dictApi`

统一处理：

- token 注入。
- 401 跳登录。
- 403 无权限提示。
- `code !== 0` 统一错误提示。
- 错误提示保留 `requestId`，便于排查。
- 分页参数。
- 文件上传进度。

## 8. 验收关注点

- 未登录不能访问学生端和管理端。
- 学生不能通过改 URL 查看他人数据。
- 班团骨干不能审批。
- 查看敏感字段会触发审计。
- 通知发布后能看到接收人、已读、未读和发送失败统计。
- 知识库问答结果必须展示来源。
- 党团流程必须展示完整阶段，不只展示当前阶段。
- 申请状态只能通过审批操作变化。
- 后端接口异常或前端运行时错误能在系统日志中查到发生时间、用户和 `requestId`。
