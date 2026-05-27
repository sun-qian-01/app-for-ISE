# 部署机（非开发机）发布方案

本文档对应“开发机构建、部署机运行”的交付方式。

## 1. 目标

- 部署机只负责运行，不安装 Maven/Node 开发链。
- 通过一个发布包完成安装与启动。
- 默认访问方式：`http://<部署机IP>`

## 2. 交付材料

在开发机执行发布脚本后，会得到：

- `release/app-for-ise-runtime-<version>.tar.gz`

压缩包内包含：

- `backend/app-for-ise-backend.jar`
- `frontend/`（已构建静态资源）
- `scripts/runtime-install.sh`（首次安装）
- `scripts/runtime-start.sh`（启动/重启）
- `scripts/runtime-stop.sh`（停止）
- `scripts/runtime-status.sh`（状态+健康检查）

## 3. 开发机构建发布包

```bash
cd /home/tianjiaming/learn/SWE/app-for-ISE
./scripts/make-runtime-release.sh
```

可选参数：

```bash
./scripts/make-runtime-release.sh --version v20260527
./scripts/make-runtime-release.sh --output-dir /tmp/releases
./scripts/make-runtime-release.sh --skip-build
```

## 4. 传输到部署机

示例：

```bash
scp release/app-for-ise-runtime-<version>.tar.gz <user>@<deploy-host>:/tmp/
```

## 5. 部署机安装与启动（首次）

```bash
ssh <user>@<deploy-host>
cd /tmp
tar -xzf app-for-ise-runtime-<version>.tar.gz
cd app-for-ise-runtime-<version>
./scripts/runtime-install.sh --public-ip <部署机IP>
```

该脚本会自动：

1. 安装运行依赖（java17/nginx/rsync/curl）
2. 部署后端到 `/opt/app-for-ise`
3. 部署前端到 `/var/www/app-for-ise`
4. 生成 systemd 服务与 Nginx 配置
5. 启动并健康检查

## 6. 日常运维

```bash
./scripts/runtime-start.sh
./scripts/runtime-stop.sh
./scripts/runtime-status.sh
```

或系统命令：

```bash
sudo systemctl status app-for-ise.service
sudo systemctl restart app-for-ise.service
sudo systemctl status nginx
sudo systemctl restart nginx
```

## 7. 升级发布

1. 开发机重新打包。
2. 传新包到部署机。
3. 解压后再次执行安装脚本：

```bash
./scripts/runtime-install.sh --public-ip <部署机IP>
```

安装脚本是幂等的，会覆盖并重启服务。
