# 本地数据库开发说明

## 1. 当前选择

当前阶段先不使用人大金仓 Kingbase。默认使用 H2 作为本地开发和测试数据库：

- 开发运行：H2 file 模式，数据落在 `./data/app-for-ise-dev.mv.db`
- 自动测试：H2 mem 模式，每次测试上下文重新初始化
- 兼容模式：`MODE=PostgreSQL`

这样做的目标是让 Linux 开发机、CI 和新加入开发者都能快速运行后端，不被 Windows 授权版本限制卡住。

## 2. 当前已持久化范围

目前只有“院内申请与审批”模块已接入数据库：

- `biz_application`
- `biz_approval_record`
- `cert_template`
- `sys_user`
- `stu_student`

其他模块仍是内存演示实现，后续应逐步迁移。

## 3. 运行与重置

启动后端：

```bash
. "$HOME/.local/bin/java-maven-env.sh"
mvn -Dmaven.repo.local=/tmp/.m2/repository spring-boot:run
```

运行测试：

```bash
. "$HOME/.local/bin/java-maven-env.sh"
mvn -Dmaven.repo.local=/tmp/.m2/repository test -q
```

重置本地开发数据：

```bash
rm -rf data/
```

`data/` 已加入 `.gitignore`，不要提交本地数据库文件。

## 4. 后续迁移到 Kingbase 的注意点

- 保持 Controller 和 DTO 契约不变，前端不应感知数据库切换。
- 将 `schema-h2.sql` 中已落地表与 `database/schema.sql` 对齐，差异必须记录在变更文档。
- 引入 Flyway 或 Liquibase 后，停止依赖 Spring SQL init 管理正式环境结构。
- 将演示版 `max(id) + 1` 主键策略替换为数据库身份列、序列或统一 ID 组件。
- 对审批、撤回、导入、发布等状态流转保留事务测试，避免迁移后出现“状态变了但日志没写”的半成功问题。
