# 后端环境信息

本文档记录当前仓库后端开发的实际环境和运行前提，便于多人协作时快速对齐。

## 1. 当前开发机环境（采样时间：2026-05-18）

- OS：Ubuntu Linux 22.04 系（内核 `6.8.0-90-generic`）
- 用户级 JDK：`Temurin OpenJDK 17.0.19`
- 用户级 Maven：`Apache Maven 3.9.9`

## 2. 当前后端工程技术基线

- 框架：Spring Boot `3.3.5`
- 语言目标：Java `17`
- 构建工具：Maven（`pom.xml`）
- 当前开发数据库：H2 `file` 模式，开启 PostgreSQL 兼容模式
- 当前测试数据库：H2 `mem` 模式，每次测试上下文按 SQL 脚本初始化

## 3. 用户级安装位置

- `JAVA_HOME=$HOME/.local/toolchains/jdk-17`
- `MAVEN_HOME=$HOME/.local/toolchains/maven`
- 环境脚本：`$HOME/.local/bin/java-maven-env.sh`
- 已写入：`~/.bashrc`、`~/.profile`

## 4. 注意事项

在本会话沙箱中，`~/.m2` 不可写，运行测试时请使用：

```bash
mvn -Dmaven.repo.local=/tmp/.m2/repository test
```

测试框架层面已增加 `mock-maker-subclass` 配置（`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`），用于规避部分 Linux/JDK 环境下 Mockito inline attach 失败问题。

当前沙箱环境不允许监听本地端口（`java.net.SocketException: 不允许的操作`），因此这里无法直接打开 `http://localhost:8080` 做浏览器验证；请在你的本机终端执行第 8 节命令进行联调。

## 5. 本地数据库说明

当前阶段先不使用人大金仓 Kingbase。为了让 Linux 开发机和普通本地环境都能快速跑通，默认使用 H2：

- 开发库 URL：`jdbc:h2:file:./data/app-for-ise-dev;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`
- 数据文件位置：`./data/app-for-ise-dev.mv.db`
- 建表脚本：`src/main/resources/db/schema-h2.sql`
- 演示数据脚本：`src/main/resources/db/data-h2.sql`
- 测试库 URL：`jdbc:h2:mem:app_for_ise_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1`

H2 脚本只覆盖当前已接入持久化的核心表：`sys_user`、`stu_student`、`cert_template`、`biz_application`、`biz_approval_record`。表名和字段尽量对齐 `database/schema.sql`，便于后续切换 Kingbase/PostgreSQL 方言。

重置本地开发数据：

```bash
rm -rf data/
```

`data/` 已加入 `.gitignore`，不要提交本地数据库文件。

当前限制：

- 只有“院内申请与审批”模块已改为 JDBC + H2 持久化。
- 其他模块仍是内存演示实现，重启后恢复初始数据。
- 申请编号和主键生成仍是演示版策略，后续接入正式数据库时应改为数据库序列、身份列或统一 ID 组件。
- 尚未引入 Flyway/Liquibase，后续多人开发时建议尽快接入正式迁移工具。

## 6. 建议的最小可运行环境

- JDK 17+
- Maven 3.9+
- 可选：Docker（后续接入数据库/依赖服务时使用）

## 7. 验证命令

```bash
. "$HOME/.local/bin/java-maven-env.sh"
java -version
mvn -version
mvn -Dmaven.repo.local=/tmp/.m2/repository test -q
```

## 8. 本地联调启动命令

```bash
. "$HOME/.local/bin/java-maven-env.sh"
mvn -Dmaven.repo.local=/tmp/.m2/repository spring-boot:run
```

启动后可访问：

- API：`http://localhost:8080/api/v1/...`
- 交互页：`http://localhost:8080/interaction-test.html`

如当前 shell 配置了 `http_proxy`/`https_proxy`，命令行请求本地服务时建议显式绕过代理：

```bash
curl --noproxy "*" http://127.0.0.1:8080/interaction-test.html
```

Windows、Linux 的完整运行、预览、调试步骤见：

- `docs/run-preview-debug.md`
