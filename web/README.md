# Web 前端工程

本目录是学院学生综合服务与党团管理平台的正式 Web 前端工程。

## 技术栈

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios

## 当前进度

当前已完成：

- 路由结构初始化
- 登录页与登录态管理骨架
- 路由守卫
- 学生端与管理端布局
- 首批学生端页面迁移
- 首批管理端页面迁移
- mock 数据层

当前仍未完成：

- 真实后端 API 接入
- 通用组件体系抽取
- 字典统一管理与权限工具完善
- 文件上传、分页、筛选、空状态、错误状态等正式能力

## 启动方式

```bash
npm install
npm run dev
```

## 构建方式

```bash
npm run build
```

## 目录说明

- `src/router/`：路由定义与守卫
- `src/stores/`：Pinia 状态管理
- `src/api/`：API client 和模块接口封装
- `src/layouts/`：学生端与管理端布局
- `src/views/`：页面级视图
- `src/mocks/`：开发阶段模拟数据与模拟接口
- `src/styles/`：全局样式

## 迁移策略

当前正式工程是从仓库根目录下的静态原型逐步迁移而来：

- 根目录 `index.html`、`login.html`、`student.html`、`admin.html` 继续作为原型参考
- `web/` 目录承载正式前端开发
- 后续应优先在 `web/` 内迭代，不再向静态原型叠加业务复杂度
