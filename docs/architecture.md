# 总体设计草案

## 1. 目标

本文档用于在当前静态原型基础上，为后续前后端联调提供统一的数据建模和接口边界。首期设计聚焦以下能力：

- 账号与 RBAC 权限控制
- 学生档案与标签画像
- 政策知识库
- 通知与精准推送
- 党团流程跟踪
- 电子证明申请与审批
- 审计日志

## 2. 业务域划分

首期系统按业务域拆分为以下模块：

1. 用户与权限域
2. 学生档案域
3. 内容知识库域
4. 消息通知域
5. 流程审批域
6. 审计与运维域

## 3. 核心实体

### 3.1 用户与权限

- `sys_user`：系统登录用户
- `sys_role`：角色定义
- `sys_permission`：菜单/按钮/接口权限点
- `sys_user_role`：用户与角色关联
- `sys_role_permission`：角色与权限关联

### 3.2 学生与画像

- `stu_student`：学生基础档案
- `stu_student_ext`：学生扩展信息
- `stu_tag`：标签定义
- `stu_student_tag`：学生标签关联
- `stu_change_log`：学籍异动记录

### 3.3 知识库

- `kb_category`：知识库分类
- `kb_article`：知识条目/政策文章
- `kb_attachment`：条目附件

### 3.4 通知推送

- `msg_notice`：通知主表
- `msg_notice_scope`：通知范围
- `msg_notice_user`：用户接收与已读状态

### 3.5 党团流程与证明审批

- `flow_party_progress`：党团发展进度
- `flow_party_record`：党团节点记录
- `cert_template`：证明模板
- `cert_application`：证明申请单
- `cert_approval_record`：审批流记录

### 3.6 审计

- `audit_log`：操作审计日志

## 4. 核心关系说明

### 4.1 用户与学生关系

- 学生端登录用户通常与一个 `stu_student` 一一关联。
- 老师和管理员用户不关联学生档案，但会通过角色和数据范围访问学生数据。

### 4.2 标签与通知关系

- 通知发布时可指定年级、专业、班级、标签等条件。
- 为保证查询效率，通知触达结果写入 `msg_notice_user`，不依赖每次实时动态匹配。

### 4.3 审批与流程关系

- 党团流程与证明审批分开建模，不混用一张通用流程表。
- 原因是两者状态机相似，但业务字段明显不同；首期分开更稳，后期可抽象统一工作流引擎。

## 5. 状态设计

### 5.1 学生状态

- `active`：在读
- `graduated`：毕业
- `suspended`：休学
- `withdrawn`：退学
- `transferred`：转专业
- `other_changed`：其他异动

### 5.2 通知状态

- `draft`：草稿
- `published`：已发布
- `archived`：已归档

### 5.3 证明申请状态

- `draft`：草稿
- `submitted`：已提交
- `reviewing`：审核中
- `approved`：已通过
- `rejected`：已驳回
- `revoked`：已撤回
- `archived`：已归档

### 5.4 党团节点状态

- `pending`：未开始
- `processing`：进行中
- `completed`：已完成
- `returned`：已退回

## 6. 数据范围控制建议

首期后端建议在用户会话中缓存以下信息：

- 用户 ID
- 角色集合
- 数据范围类型
- 可访问的班级、专业、年级、支部范围

数据范围建议分为：

- `self`：仅本人
- `class`：本班
- `major`：本专业
- `grade`：本年级
- `department`：学院范围
- `global`：全局

## 7. 接口风格约定

- 风格：RESTful JSON API
- 鉴权：登录后返回 token，后续通过 `Authorization: Bearer <token>` 传递
- 时间字段：统一使用 `yyyy-MM-dd HH:mm:ss`
- 主键：数据库使用 `bigint`
- 删除策略：核心业务表优先逻辑删除，使用 `is_deleted`

## 8. 首期不做

以下内容不进入首期数据库和接口基线：

- 正式请假系统
- 短信通道
- 微信实名登录接入
- 通用 BPM 引擎
- 成绩单自动解析引擎
