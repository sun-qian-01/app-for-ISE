# Runtime Deployment Guide

1) 将整个目录上传到部署服务器（例如 `/tmp/app-for-ise-runtime-*`）。
2) 检查 `config/app.env`，确认 RAG_LLM_API_KEY 已设置。进入目录后执行：

```bash
./scripts/runtime-install.sh --public-ip <部署机IP>
```

3) 后续启停：

```bash
./scripts/runtime-start.sh
./scripts/runtime-stop.sh
./scripts/runtime-status.sh
```
