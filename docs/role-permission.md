# 角色权限说明

本文档定义系统角色、数据范围、菜单权限和关键操作权限。前端用于控制菜单和按钮展示，后端用于最终鉴权和数据范围过滤。

## 1. 角色定义

| 角色代码 | 角色名称 | 默认数据范围 | 说明 |
| --- | --- | --- | --- |
| `student` | 普通学生 | `self` | 学生本人使用，只能访问个人相关数据。 |
| `class_cadre` | 班团骨干 | `class` / `branch` | 班长、团支书等，访问授权班级或支部范围内的进展摘要。 |
| `teacher_admin` | 管理老师 | `grade` / `major` / `class` / `department` | 辅导员、班主任、教学秘书等，负责日常管理和审批。 |
| `college_leader` | 学院领导 | `department` | 查看学院汇总、统计看板和重点问题。 |
| `system_admin` | 系统管理员 | `global` | 维护账号、角色、权限和基础配置。可选角色，谨慎分配。 |

## 2. 数据范围

| 范围代码 | 名称 | 示例 |
| --- | --- | --- |
| `self` | 本人 | 当前登录学生本人 |
| `class` | 班级 | 软件工程2班 |
| `branch` | 支部 | 本科生第一党支部 |
| `major` | 专业 | 软件工程 |
| `grade` | 年级 | 2022级 |
| `department` | 学院 | 信息科学与工程学院 |
| `global` | 全局 | 系统管理员维护范围 |

实现要求：

- `sys_role.data_scope` 只表示默认范围。
- 用户实际范围明细写入 `sys_user_scope`。
- 同一用户可以有多个范围，例如管理老师同时管理 2022 级和 2023 级。
- 前端展示范围信息，后端负责过滤数据。

## 3. 菜单权限

### 3.1 学生端菜单

| 菜单 | 权限码 | student | class_cadre | teacher_admin | college_leader |
| --- | --- | --- | --- | --- | --- |
| 学生首页 | `student:dashboard:view` | 是 | 是 | 否 | 否 |
| 智能问答 | `student:kb:view` | 是 | 是 | 否 | 否 |
| 党团流程 | `student:party:view` | 是 | 是 | 否 | 否 |
| 通知中心 | `student:notice:view` | 是 | 是 | 否 | 否 |
| 院内申请 | `student:application:view` | 是 | 是 | 否 | 否 |
| 个人画像 | `student:profile:view` | 是 | 是 | 否 | 否 |
| 奖励荣誉 | `student:honor:view` | 是 | 是 | 是 | 是 |
| 班团协同 | `cadre:party:todo:view` | 否 | 是 | 是 | 否 |

### 3.2 管理端菜单

| 菜单 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 管理首页 | `admin:dashboard:view` | 否 | 否 | 是 | 是 | 是 |
| 学生画像管理 | `admin:student:view` | 否 | 否 | 是 | 汇总只读 | 是 |
| 知识库管理 | `admin:kb:view` | 否 | 否 | 是 | 只读 | 是 |
| 党团流程管理 | `admin:party:view` | 否 | 否 | 是 | 汇总只读 | 是 |
| 精准通知 | `admin:notice:view` | 否 | 否 | 是 | 只读 | 是 |
| 审批处理 | `admin:application:view` | 否 | 否 | 是 | 否 | 是 |
| 荣誉管理 | `admin:honor:view` | 否 | 否 | 是 | 只读 | 是 |
| 审计日志 | `admin:audit:view` | 否 | 否 | 是 | 是 | 是 |
| 系统日志 | `admin:system-log:view` | 否 | 否 | 是 | 是 | 是 |
| 角色权限 | `admin:permission:view` | 否 | 否 | 否 | 否 | 是 |

## 4. 操作权限矩阵

### 4.1 学生档案

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看本人档案 | `student:profile:self` | 是 | 是 | 否 | 否 | 否 |
| 查看学生列表 | `student:list:view` | 否 | 授权范围摘要 | 是 | 汇总只读 | 是 |
| 查看学生详情 | `student:detail:view` | 本人 | 授权范围摘要 | 是 | 否 | 是 |
| 查看敏感字段 | `student:sensitive:view` | 本人脱敏 | 否 | 是 | 否 | 是 |
| 修改学生信息 | `student:update` | 否 | 否 | 是 | 否 | 是 |
| 导入学生信息 | `student:import` | 否 | 否 | 是 | 否 | 是 |
| 导出学生信息 | `student:export` | 否 | 否 | 是 | 汇总导出 | 是 |
| 维护学生标签 | `student:tag:update` | 否 | 否 | 是 | 否 | 是 |

敏感字段访问要求：

- 调用接口时需传 `includeSensitive=true`。
- 后端校验 `student:sensitive:view`。
- 成功后写入 `audit_log`。
- 前端展示“本次敏感信息访问已记录”。

### 4.2 知识库

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查询已发布知识 | `kb:article:public:view` | 是 | 是 | 是 | 是 | 是 |
| 智能问答 | `kb:qa:ask` | 是 | 是 | 是 | 是 | 是 |
| 下载模板 | `kb:template:download` | 是 | 是 | 是 | 是 | 是 |
| 新增知识条目 | `kb:article:create` | 否 | 否 | 是 | 否 | 是 |
| 修改知识条目 | `kb:article:update` | 否 | 否 | 是 | 否 | 是 |
| 发布知识条目 | `kb:article:publish` | 否 | 否 | 是 | 否 | 是 |
| 停用知识条目 | `kb:article:disable` | 否 | 否 | 是 | 否 | 是 |
| 查看版本历史 | `kb:article:version:view` | 否 | 否 | 是 | 只读 | 是 |

### 4.3 党团流程

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看本人流程 | `party:instance:self:view` | 是 | 是 | 否 | 否 | 否 |
| 查看授权范围流程 | `party:instance:scope:view` | 否 | 是 | 是 | 汇总只读 | 是 |
| 提交本人材料 | `party:material:self:submit` | 是 | 是 | 否 | 否 | 否 |
| 审核材料 | `party:material:review` | 否 | 否 | 是 | 否 | 是 |
| 审核阶段 | `party:stage:review` | 否 | 否 | 是 | 否 | 是 |
| 退回材料 | `party:material:return` | 否 | 否 | 是 | 否 | 是 |
| 撤回或重批 | `party:stage:reopen` | 否 | 否 | 是 | 否 | 是 |
| 班团催办 | `party:todo:remind` | 否 | 是 | 是 | 否 | 是 |

约束：

- 班团骨干不能执行正式审批。
- 学生只能提交本人当前阶段材料。
- 审批撤回或重批必须在限定时间内执行，并保留历史日志。

### 4.4 通知与推送

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看我的通知 | `notice:my:view` | 是 | 是 | 是 | 是 | 是 |
| 标记已读 | `notice:my:read` | 是 | 是 | 是 | 是 | 是 |
| 通知管理列表 | `notice:manage:view` | 否 | 否 | 是 | 只读 | 是 |
| 创建通知 | `notice:create` | 否 | 否 | 是 | 否 | 是 |
| 发布通知 | `notice:publish` | 否 | 否 | 是 | 否 | 是 |
| 归档通知 | `notice:archive` | 否 | 否 | 是 | 否 | 是 |
| 查看通知统计 | `notice:stats:view` | 否 | 否 | 是 | 是 | 是 |

### 4.5 院内申请与审批

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 发起申请 | `application:create` | 是 | 是 | 是 | 否 | 是 |
| 查看我的申请 | `application:self:view` | 是 | 是 | 是 | 否 | 是 |
| 撤回我的申请 | `application:self:revoke` | 是 | 是 | 是 | 否 | 是 |
| 查看待审批 | `application:approval:view` | 否 | 否 | 是 | 否 | 是 |
| 审批通过 | `application:approve` | 否 | 否 | 是 | 否 | 是 |
| 审批驳回 | `application:reject` | 否 | 否 | 是 | 否 | 是 |
| 维护证明模板 | `certificate:template:manage` | 否 | 否 | 是 | 否 | 是 |

### 4.6 荣誉展示

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看公开荣誉 | `honor:public:view` | 是 | 是 | 是 | 是 | 是 |
| 查看荣誉管理列表 | `honor:manage:view` | 否 | 否 | 是 | 只读 | 是 |
| 新增荣誉 | `honor:create` | 否 | 否 | 是 | 否 | 是 |
| 修改荣誉 | `honor:update` | 否 | 否 | 是 | 否 | 是 |
| 发布荣誉 | `honor:publish` | 否 | 否 | 是 | 否 | 是 |
| 下线荣誉 | `honor:unpublish` | 否 | 否 | 是 | 否 | 是 |

### 4.7 审计日志

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看审计日志 | `audit:list:view` | 否 | 否 | 是 | 是 | 是 |
| 导出审计日志 | `audit:export` | 否 | 否 | 是 | 否 | 是 |

### 4.8 系统日志

| 操作 | 权限码 | student | class_cadre | teacher_admin | college_leader | system_admin |
| --- | --- | --- | --- | --- | --- | --- |
| 查看系统日志列表 | `system-log:list:view` | 否 | 否 | 是 | 是 | 是 |
| 查看系统日志详情 | `system-log:detail:view` | 否 | 否 | 是 | 是 | 是 |
| 查看错误堆栈 | `system-log:stack:view` | 否 | 否 | 否 | 否 | 是 |
| 导出系统日志 | `system-log:export` | 否 | 否 | 是 | 否 | 是 |

约束：

- 系统日志用于排查平台问题，普通学生和班团骨干不可访问。
- 学院领导可查看日志摘要和统计，不默认查看完整堆栈。
- 完整堆栈、请求摘要、额外上下文只开放给系统管理员或具备排障权限的人员。

## 5. 前端权限实现

前端登录后必须保存：

- `token`
- `user`
- `roles`
- `permissions`
- `dataScopes`

推荐判断函数：

```js
function hasPermission(code) {
  return currentUser.permissions.includes(code);
}
```

菜单展示：

- 学生端菜单只展示学生拥有的权限。
- 管理端菜单只展示管理权限。
- 领导端隐藏审批按钮和敏感明细。

按钮展示：

- 按钮必须绑定具体权限码。
- 不要只按角色名称判断。
- 权限不足时不展示按钮；如需要展示，应置灰并说明原因。

## 6. 后端权限实现

后端必须实现：

- 登录鉴权。
- 接口权限码校验。
- 数据范围过滤。
- 对象归属校验。
- 敏感字段脱敏。
- 审计日志。

典型风险：

- 学生通过修改 URL 查看他人申请。
- 班团骨干调用审批接口。
- 管理老师导出超出授权范围的数据。
- 前端隐藏了字段但接口仍返回敏感原文。

以上风险必须在后端解决。
