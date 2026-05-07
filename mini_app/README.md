# mini_app 演示版说明

这是根据仓库 `README.md` 与 `docs` 文档实现的微信小程序学生端基础版，覆盖以下最小功能：

- 首页聚合：个人信息、待办/未读统计、快捷入口、近期通知
- 知识库：关键词检索 + 分类筛选
- 党团进度：当前阶段与时间线记录
- 通知中心：查看通知、单条已读、全部已读
- 证明申请：选择模板、填写用途、提交申请、撤回申请

## 目录结构

```text
mini_app/
  app.js
  app.json
  app.wxss
  project.config.json
  sitemap.json
  utils/mock.js
  pages/
    home/
    kb/
    progress/
    notice/
    certificate/
```

## 运行方式

1. 打开微信开发者工具
2. 选择“导入项目”
3. 项目目录选择 `mini_app`
4. `AppID` 可先使用测试号或游客模式（当前配置为 `touristappid`）
5. 点击编译即可查看页面

## 说明

- 当前为前端演示版，数据来自 `utils/mock.js`。
- 后续可按 `docs/api.md` 把页面逻辑改为调用真实后端接口。
