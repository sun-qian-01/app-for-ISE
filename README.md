# 学院学生综合服务与党团管理平台

## 项目概览

本项目面向学院内部学生事务与党团管理场景，目标是建设一个前后端分离的 Web 平台，覆盖学生服务、党团流程、精准通知、院内申请、学生画像、知识库与审计日志等核心业务。

当前仓库已经不再只是需求文档或静态原型，而是进入了“文档齐备 + 正式前端工程推进中 + 后端 MVP 工程已启动”的阶段。

## 仓库架构

### 根目录

| 路径 | 说明 |
| --- | --- |
| `README.md` | 项目总览、目录说明、开发进度与后续建议 |
| `index.html` | 早期静态原型入口 |
| `login.html` | 早期静态登录原型 |
| `student.html` | 早期学生端静态原型 |
| `admin.html` | 早期管理端静态原型 |
| `styles.css` | 早期静态原型样式 |
| `add.md` | 早期补充需求与待办记录 |
| `pom.xml` | 后端 Spring Boot Maven 工程配置 |

### `docs/`

这是当前最重要的文档目录，已经形成了比较完整的项目约束边界。

| 路径 | 说明 |
| --- | --- |
| `docs/architecture.md` | 总体架构与业务边界 |
| `docs/api.md` | 接口统一命名与 REST 契约 |
| `docs/api-mvp-alignment.md` | 当前后端 MVP 与目标接口的对齐说明 |
| `docs/database.md` | 数据库设计说明 |
| `docs/frontend-spec.md` | 前端页面结构与交互说明 |
| `docs/role-permission.md` | 角色与权限矩阵 |
| `docs/dictionary.md` | 状态、类型、字典统一命名 |
| `docs/developer-diary.md` | 开发者日记，记录阶段工作与决策 |
| `docs/backend/*` | 后端环境、运行、设计和变更记录 |

### `database/`

| 路径 | 说明 |
| --- | --- |
| `database/schema.sql` | Kingbase/PostgreSQL 兼容建表脚本 |

### `web/`

这是当前正式前端工程目录，后续前端开发应以这里为主，不再继续在根目录静态原型上叠加业务代码。

主要结构：

| 路径 | 说明 |
| --- | --- |
| `web/src/layouts/` | 学生端、管理端布局 |
| `web/src/router/` | 路由与权限守卫 |
| `web/src/stores/` | Pinia 状态管理 |
| `web/src/api/` | API client 与模块化接口封装 |
| `web/src/mocks/` | mock 数据与 mock 服务 |
| `web/src/components/common/` | 通用组件 |
| `web/src/views/student/` | 学生端页面 |
| `web/src/views/admin/` | 管理端页面 |
| `web/src/views/` | 登录、403、404、根路由等公共页 |
| `web/src/composables/` | 字典与权限组合式工具 |
| `web/src/constants/` | 统一字典常量 |
| `web/src/styles/` | 正式前端样式 |

### `src/`

这是当前后端 Java 工程目录，已经有可运行的 Spring Boot MVP 骨架。

主要结构：

| 路径 | 说明 |
| --- | --- |
| `src/main/java/com/ise/platform/common/` | 通用响应、异常、鉴权、请求上下文等基础设施 |
| `src/main/java/com/ise/platform/modules/auth/` | 认证模块 |
| `src/main/java/com/ise/platform/modules/dashboard/` | 首页聚合模块 |
| `src/main/java/com/ise/platform/modules/student/` | 学生画像模块 |
| `src/main/java/com/ise/platform/modules/kb/` | 知识库模块 |
| `src/main/java/com/ise/platform/modules/party/` | 党团流程模块 |
| `src/main/java/com/ise/platform/modules/notice/` | 通知模块 |
| `src/main/java/com/ise/platform/modules/application/` | 院内申请与审批模块 |
| `src/main/java/com/ise/platform/modules/file/` | 文件上传下载模块 |
| `src/main/java/com/ise/platform/modules/dict/` | 字典模块 |
| `src/main/resources/` | 配置、H2 初始化脚本、静态测试页 |
| `src/test/java/` | 模块测试与集成测试 |

### `mini_app/`

这是早期小程序演示版目录，当前仍保留作为移动端结构参考，但不是当前主开发线。

### `scripts/`

这里保留了早期静态原型的模拟逻辑脚本，仅作为参考，不应继续作为正式业务代码扩展。

## 当前开发进度

### 1. 文档体系

目前文档侧已经比较完整，以下几份文档已经形成开发约束基础：

- 总体边界：`docs/architecture.md`
- 接口契约：`docs/api.md`
- 数据库设计：`database/schema.sql` 与 `docs/database.md`
- 前端开发说明：`docs/frontend-spec.md`
- 角色权限说明：`docs/role-permission.md`
- 字典命名统一：`docs/dictionary.md`

这意味着前后端后续开发已经不需要再从零讨论命名和边界，应该直接按现有文档推进。

### 2. 前端进度

正式前端工程已经启动，并且完成了第一轮和第二轮基础建设。

已完成内容：

- 基于 `Vue 3 + Vite + Vue Router + Pinia + Axios` 初始化正式前端工程
- 完成登录页、路由、受保护路由、角色路由分发
- 完成学生端与管理端基础布局
- 完成 mock 数据层与模块化 API 封装
- 完成字典工具、权限工具、公共组件体系
- 完成加载态、错误态、空状态、分页栏等基础组件
- 管理端菜单已支持基于权限码过滤

当前已具备正式页面骨架的页面包括：

- 学生端：
  - 仪表盘
  - 知识库 / 智能问答
  - 党团流程
  - 通知中心
  - 院内申请
  - 个人画像
  - 荣誉展示
- 管理端：
  - 仪表盘
  - 学生画像管理
  - 知识库管理
  - 精准通知
  - 审批处理
  - 荣誉管理
  - 审计日志
  - 系统日志

本轮前端刚刚完成的重点是：

- 补强知识库、通知、申请三组页面的数据结构
- 把学生端知识库、通知、申请页迁成统一业务页结构
- 把管理端知识库、通知、审批三页从占位页升级为正式页面骨架

当前前端状态可以概括为：

- 已脱离单纯静态原型
- 已具备持续开发的组件化基础
- 已具备 mock 驱动的联调前置结构
- 距离“可接真实后端”的状态已经不远

### 3. 后端进度

后端 Spring Boot 工程已经启动，具备 MVP 骨架和部分可运行接口。

已具备：

- 统一响应结构
- 全局异常处理
- 请求 ID 上下文
- 鉴权上下文与认证过滤器
- 认证模块骨架
- 首页聚合模块骨架
- 学生模块骨架
- 知识库模块骨架
- 党团模块骨架
- 通知模块骨架
- 院内申请模块骨架
- 文件模块骨架
- 字典模块骨架
- H2 本地开发数据库初始化脚本
- 基础测试类与模块测试

当前后端更接近“可运行的 MVP 后端骨架”，还没有进入完整业务实现阶段。

### 4. 静态原型状态

根目录下的：

- `index.html`
- `login.html`
- `student.html`
- `admin.html`
- `styles.css`
- `scripts/app.js`

仍然保留，主要用途是：

- 回看最初视觉与交互方向
- 为正式前端迁移提供参考

不建议后续继续在这些文件上叠加开发。

## 当前推荐开发主线

建议当前仓库按以下主线理解：

1. 文档边界已经基本稳定，以 `docs/` 和 `database/` 为准。
2. 正式前端以 `web/` 为唯一主战场。
3. 正式后端以 `src/` 为唯一主战场。
4. 根目录静态原型只保留参考价值，不再继续扩展。
5. `mini_app/` 暂不作为当前主开发线。

## 本地运行

### 前端

```bash
cd web
npm install
npm run dev
```

构建验证：

```bash
cd web
npm run build
```

### 后端

```bash
mvn spring-boot:run
```

测试：

```bash
mvn test
```

说明：

- 仓库已内置 `.mvn/maven.config`，默认将本地 Maven 仓库写入 `/tmp/m2repo`，避免部分环境下 `~/.m2` 不可写导致启动失败。
- 当前后端以 `JDK 17` 为运行与编译目标。

如需更具体的运行、联调、调试说明，优先查看：

- `docs/backend/run-preview-debug.md`
- `docs/backend/backend-environment.md`

## 后续建议

### 前端建议

前端下一步建议按这个顺序推进：

1. 为管理端通知页补“创建 / 编辑通知”表单区。
2. 为管理端知识库页补“新建 / 编辑文章”和“模板上传替换”交互。
3. 为管理端审批页补“审批意见抽屉 / 详情侧栏”。
4. 将当前列表页逐步统一接入真实分页，而不是只做前端筛选。
5. 开始让 `web/src/api/modules/*` 分批替换 mock server，接真实后端接口。

### 后端建议

后端下一步建议按这个顺序推进：

1. 先把认证、字典、知识库、通知、申请这几条高频链路补成可联调接口。
2. 优先保证权限码校验、数据范围校验、对象归属校验。
3. 尽快把申请模块之外的核心模块从内存实现迁到数据库实现。
4. 补请求日志、审计日志、系统异常日志的完整落库或可追踪机制。
5. 对齐 `docs/api.md` 与当前 MVP 差异，持续维护 `docs/api-mvp-alignment.md`。

### 协作建议

为了避免后面仓库再次出现“代码走到哪里、文档还停在旧阶段”的问题，建议保持这三个习惯：

1. 每做完一轮前端或后端功能，就同步更新 `docs/developer-diary.md`。
2. 接口字段、状态枚举、权限命名只以 `docs/api.md`、`docs/dictionary.md`、`docs/role-permission.md` 为准。
3. 如果目录职责发生变化，优先回写 `README.md`，不要只在对话里说明。

## 当前结论

当前仓库已经具备继续正式开发的条件，最重要的是开发重心已经明确：

- 文档边界在 `docs/`
- 数据结构边界在 `database/`
- 正式前端在 `web/`
- 正式后端在 `src/`

接下来最值得继续投入的是“把正式前端从页面骨架推进到真实表单和联调态”，以及“把后端 MVP 从骨架推进到可供前端接入的稳定接口层”。
