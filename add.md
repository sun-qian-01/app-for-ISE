## 1. 学生画像管理

  当前管理端只有学生列表和简单搜索，需要补齐：

  - 学生详情抽屉或详情页。
  - 多维筛选：姓名、学号、年级、专业、班级、政治面貌、标签、毕业年级。
  - 标签维护：查看、添加、修改、停用标签，支持给学生更新标签。
  - 成长记录维护：查看、新增成长记录，支持证明附件。
  - 敏感字段查看：需要显式按钮，例如“查看完整联系方式”。
  - 查看敏感字段后展示提示：“本次敏感信息访问已记录”。
  - Excel 导入：上传文件、展示导入任务进度、成功数、失败数、错误文件。
  - 学生信息导出：按当前筛选条件导出，并提示导出受当前用户数据范围限制。
  - 列表需有分页、加载、空状态、错误状态。

  相关接口：
  - `GET /students`
  - `GET /students/{studentId}`
  - `PUT /students/{studentId}`
  - `GET /students/{studentId}/growth-records`
  - `POST /students/{studentId}/growth-records`
  - `PUT /students/{studentId}/tags`
  - `POST /students/import-tasks`
  - `GET /students/export`
  - `GET /tags`
  - `POST /tags`
  - `PUT /tags/{tagId}`
  - `DELETE /tags/{tagId}`

## 2. 院内申请与证明审批

  学生端需要补齐：

  - 证明模板选择。
  - 根据模板 `formSchemaJson` 渲染动态表单字段。
  - 申请详情页或详情弹窗。
  - 审批记录展示。
  - 生成文件预览/下载。
  - 撤回申请功能，需要填写撤回原因。
  - 我的申请列表支持按申请类型、状态筛选。

  管理端需要补齐：

  - 待审批列表筛选：申请类型、状态、模板。
  - 申请详情。
  - 申请人信息摘要。
  - 附件和生成文件查看/下载。
  - 审批记录。
  - 通过/驳回时填写审批意见。
  - 状态冲突时刷新详情并提示用户。
  - 证明模板管理：模板列表、新建模板、上传模板文件、配置动态表单和审批规则。

  相关接口：
  - `GET /certificates/templates`
  - `POST /certificates/templates`
  - `POST /applications`
  - `GET /applications/my`
  - `GET /applications/{applicationId}`
  - `POST /applications/{applicationId}/revoke`
  - `GET /applications/approvals/pending`
  - `POST /applications/{applicationId}/approve`
  - `POST /applications/{applicationId}/reject`

## 3. 荣誉展示与管理

  学生端需要补齐：

  - 按年份、荣誉类别、个人/集体筛选。
  - 荣誉图片展示。
  - 荣誉详情展示：获奖对象、年份、类别、先进事迹、图片等。

  管理端需要补齐：

  - 荣誉新增表单。
  - 荣誉编辑表单。
  - 发布、下线操作。
  - 展示排序配置。
  - 展示时效配置。
  - 图片上传。
  - 管理列表支持筛选和分页。

  相关接口：
  - `GET /honors/public`
  - `GET /honors`
  - `POST /honors`
  - `POST /honors/{honorId}/publish`
  - `POST /honors/{honorId}/unpublish`

## 4. 文件上传下载体系

  当前页面多处只是“模拟上传文件名”，需要统一接入真实文件能力：

  - 建立通用 `FileUploader` 组件。
  - 支持 PDF、Word、Excel 上传。
  - 支持上传进度展示。
  - 支持文件大小、类型校验。
  - 上传成功后返回并保存 `fileId`。
  - 业务接口只传 `fileId`，不要传文件名。
  - 支持文件下载。
  - 下载失败或无权限时展示明确提示。
  - 上传/下载异常需要保留后端返回的 `requestId`。
  - 涉及政策文件、通知附件、党团材料、证明模板、荣誉图片、成长记录证明附件等场景都应统一使用该组件或文件接口。

  相关接口：
  - `POST /files/upload`
  - `GET /files/{fileId}/download`

## 5. 班团骨干独立体验

  当前登录页有 `class_cadre`，但学生端没有独立功能入口，需要补齐：

  - 登录为班团骨干时，学生端额外展示“班团协同”入口。
  - 展示授权班级或支部范围。
  - 查看授权范围内成员党团流程进展摘要。
  - 查看待催办列表。
  - 支持催办操作。
  - 明确禁止审批操作，审批按钮不展示或置灰说明。
  - 页面和按钮需按权限码控制，而不是只按角色名判断。

  相关权限码：
  - `cadre:party:todo:view`
  - `party:instance:scope:view`
  - `party:todo:remind`

  相关接口：
  - `GET /party/todos`
  - `GET /party/instances/students/{studentId}`
