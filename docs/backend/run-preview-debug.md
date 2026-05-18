# 运行、预览与调试指南（Windows / Linux）

本文档面向新加入开发者，说明如何在 Windows 与 Linux 上运行后端、预览页面、调试接口和排查常见环境问题。

## 1. 前置要求

通用要求：

- JDK 17+
- Maven 3.9+
- Git
- 浏览器：Chrome、Edge 或 Firefox

当前后端技术栈：

- Spring Boot `3.3.5`
- Java `17`
- Maven
- 本地开发数据库：H2 file 模式
- 测试数据库：H2 mem 模式

当前阶段先不使用人大金仓 Kingbase。H2 只用于本地开发与自动测试，正式数据库迁移说明见 `docs/database-local-dev.md`。

## 2. Linux 运行

如果使用本仓库当前用户级 Java/Maven 环境：

```bash
. "$HOME/.local/bin/java-maven-env.sh"
java -version
mvn -version
```

启动后端：

```bash
mvn -Dmaven.repo.local=/tmp/.m2/repository spring-boot:run
```

运行测试：

```bash
mvn -Dmaven.repo.local=/tmp/.m2/repository test -q
```

打包构建：

```bash
mvn -Dmaven.repo.local=/tmp/.m2/repository -DskipTests package -q
```

如果你的 Linux 用户目录下 `~/.m2` 可正常写入，也可以省略 `-Dmaven.repo.local=/tmp/.m2/repository`。

## 3. Windows 运行

### 3.1 PowerShell

确认环境：

```powershell
java -version
mvn -version
```

启动后端：

```powershell
mvn spring-boot:run
```

运行测试：

```powershell
mvn test
```

打包构建：

```powershell
mvn -DskipTests package
```

### 3.2 CMD

```cmd
java -version
mvn -version
mvn spring-boot:run
```

如果 Windows 上 Maven 不能识别，请检查：

- `JAVA_HOME` 是否指向 JDK 17 目录。
- Maven 的 `bin` 目录是否加入 `Path`。
- 新开终端后再执行 `java -version` 与 `mvn -version`。

## 4. 页面预览

### 4.1 后端交互测试页

启动后端后访问：

```text
http://localhost:8080/interaction-test.html
```

该页面用于快速联调后端 API，支持：

- 登录与 token 管理
- 党团流程定义、实例、材料提交流程
- 申请创建、详情、撤回、待审批、审批通过/驳回
- 学生写接口：成长记录新增、标签更新、导入任务查询

演示账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 学生 | `20220001` | `123456` |
| 班团骨干 | `20220018` | `123456` |
| 管理老师 | `teacher001` | `123456` |
| 学院领导 | `leader001` | `123456` |

### 4.2 静态原型页面

不启动后端也可以直接打开：

- `index.html`
- `student.html`
- `admin.html`
- `login.html`

这些页面使用本地 mock 数据，只用于产品和前端原型预览，不代表真实后端权限和持久化行为。

## 5. API 调试

### 5.1 curl 登录

Linux / macOS / Git Bash：

```bash
curl --noproxy "*" -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"20220001","password":"123456"}'
```

PowerShell：

```powershell
curl.exe -X POST http://127.0.0.1:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"20220001\",\"password\":\"123456\"}"
```

如果命令行配置了代理，本地请求建议绕过代理：

- Linux / Git Bash：`curl --noproxy "*"`
- PowerShell：通常可直接访问 `127.0.0.1`，若仍失败请检查系统代理或终端代理变量。

### 5.2 Postman / Apifox

基础地址：

```text
http://localhost:8080/api/v1
```

登录后复制返回的 `token`，后续接口添加请求头：

```text
Authorization: Bearer <token>
```

统一响应格式：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "..."
}
```

## 6. 数据库文件与重置

当前本地 H2 数据文件：

```text
data/app-for-ise-dev.mv.db
```

该目录已加入 `.gitignore`，不要提交本地数据库文件。

重置本地开发数据库：

Linux / Git Bash：

```bash
rm -rf data/
```

PowerShell：

```powershell
Remove-Item -Recurse -Force .\data
```

CMD：

```cmd
rmdir /s /q data
```

重置后重新运行 `mvn spring-boot:run`，Spring 会按 `src/main/resources/db/schema-h2.sql` 和 `src/main/resources/db/data-h2.sql` 初始化演示数据。

## 7. IDE 调试

### 7.1 IntelliJ IDEA

推荐方式：

1. 使用 IDEA 打开仓库根目录。
2. 等待 Maven 项目导入完成。
3. 确认 Project SDK 为 JDK 17。
4. 打开 `src/main/java/com/ise/platform/PlatformBackendApplication.java`。
5. 点击 `main` 方法旁边运行或调试按钮。
6. 在 Controller 或 Service 中打断点。
7. 访问 `http://localhost:8080/interaction-test.html` 或用 Postman 请求接口触发断点。

### 7.2 VS Code

建议安装：

- Extension Pack for Java
- Spring Boot Extension Pack

操作：

1. 打开仓库根目录。
2. 等待 Java 插件识别 Maven 项目。
3. 在 `PlatformBackendApplication.java` 中点击 `Run` 或 `Debug`。
4. 在 `src/main/java/com/ise/platform/modules/**` 下的 Controller/Service 中打断点。

## 8. Maven 远程调试

如果希望用 Maven 启动，同时让 IDE attach 到调试端口：

Linux / Git Bash：

```bash
MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
mvn spring-boot:run
```

PowerShell：

```powershell
$env:MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
mvn spring-boot:run
```

然后在 IDE 中创建 Remote JVM Debug 配置，连接：

```text
localhost:5005
```

如需应用启动前停住等待调试器，把 `suspend=n` 改为 `suspend=y`。

## 9. 常见问题

### 9.1 端口 8080 被占用

Linux：

```bash
lsof -i :8080
```

Windows PowerShell：

```powershell
netstat -ano | findstr :8080
```

可以临时换端口：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

访问地址同步改为：

```text
http://localhost:8081/interaction-test.html
```

### 9.2 Maven 下载依赖失败

建议检查：

- 网络代理设置。
- Maven `settings.xml` 镜像配置。
- 是否能访问 Maven Central 或团队内部 Maven 镜像。

Linux 沙箱或只读用户目录中，可使用：

```bash
mvn -Dmaven.repo.local=/tmp/.m2/repository test -q
```

### 9.3 页面能打开但接口返回 401

先点击交互页的“登录”按钮，确认页面显示 token。接口请求必须携带：

```text
Authorization: Bearer <token>
```

### 9.4 本地数据和预期不一致

删除 `data/` 后重启后端，恢复演示数据。

### 9.5 当前 Codex 沙箱无法预览网页

当前 Codex 沙箱可能禁止监听本地端口，表现为：

```text
java.net.SocketException: 不允许的操作
```

这不是代码错误。请在你的 Windows 或 Linux 本机终端执行启动命令后，用浏览器访问交互页。
