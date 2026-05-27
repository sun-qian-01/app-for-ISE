#!/usr/bin/env bash
#
# make-runtime-release.sh
# -----------------------
# 用途：
#   在“开发机”上构建并打包发布物，生成可直接传给部署机的压缩包。
#
# 输出内容：
#   release/<release-name>.tar.gz
#
# 发布包包含：
#   - backend/app-for-ise-backend.jar
#   - frontend/* (Vite build 后静态文件)
#   - scripts/runtime-install.sh
#   - scripts/runtime-start.sh / runtime-stop.sh / runtime-status.sh
#   - DEPLOY.md （部署说明）
#
# 示例：
#   ./scripts/make-runtime-release.sh
#   ./scripts/make-runtime-release.sh --version v20260527 --output-dir /tmp/releases

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
WEB_DIR="${REPO_ROOT}/web"

OUTPUT_DIR="${REPO_ROOT}/release"
VERSION=""
SKIP_BUILD="0"

usage() {
  cat <<'EOF'
用法:
  ./scripts/make-runtime-release.sh [选项]

选项:
  --version <VER>       发布版本号（默认: 时间戳+git短hash）
  --output-dir <DIR>    发布包输出目录（默认: ./release）
  --skip-build          跳过构建，直接打包现有产物
  -h, --help            显示帮助
EOF
}

need_cmd() {
  local cmd="$1"
  command -v "${cmd}" >/dev/null 2>&1 || {
    echo "[ERROR] 缺少命令: ${cmd}" >&2
    exit 1
  }
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --version)
        VERSION="${2:-}"
        shift 2
        ;;
      --output-dir)
        OUTPUT_DIR="${2:-}"
        shift 2
        ;;
      --skip-build)
        SKIP_BUILD="1"
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "[ERROR] 未知参数: $1" >&2
        usage
        exit 1
        ;;
    esac
  done
}

resolve_version() {
  if [[ -n "${VERSION}" ]]; then
    return
  fi
  local stamp hash
  stamp="$(date +%Y%m%d%H%M%S)"
  hash="$(git -C "${REPO_ROOT}" rev-parse --short HEAD 2>/dev/null || echo nogit)"
  VERSION="${stamp}-${hash}"
}

build_artifacts() {
  echo "[INFO] 构建后端..."
  (cd "${REPO_ROOT}" && mvn -DskipTests package)

  echo "[INFO] 构建前端..."
  if [[ -f "${WEB_DIR}/package-lock.json" ]]; then
    (cd "${WEB_DIR}" && npm ci)
  else
    (cd "${WEB_DIR}" && npm install)
  fi
  (cd "${WEB_DIR}" && npm run build)
}

locate_backend_jar() {
  BACKEND_JAR="$(cd "${REPO_ROOT}" && ls -1t target/*.jar 2>/dev/null | grep -v 'original-' | head -n 1 || true)"
  if [[ -z "${BACKEND_JAR}" ]]; then
    echo "[ERROR] 未找到后端 jar（target/*.jar）。" >&2
    exit 1
  fi
}

validate_sources() {
  [[ -f "${REPO_ROOT}/${BACKEND_JAR}" ]] || {
    echo "[ERROR] 后端 jar 不存在: ${REPO_ROOT}/${BACKEND_JAR}" >&2
    exit 1
  }
  [[ -f "${WEB_DIR}/dist/index.html" ]] || {
    echo "[ERROR] 前端 dist 不存在: ${WEB_DIR}/dist/index.html" >&2
    exit 1
  }

  for f in runtime-install.sh runtime-start.sh runtime-stop.sh runtime-status.sh; do
    [[ -f "${SCRIPT_DIR}/${f}" ]] || {
      echo "[ERROR] 缺少脚本: ${SCRIPT_DIR}/${f}" >&2
      exit 1
    }
  done
}

make_bundle() {
  local bundle_name="app-for-ise-runtime-${VERSION}"
  local bundle_dir="${OUTPUT_DIR}/${bundle_name}"
  local archive="${OUTPUT_DIR}/${bundle_name}.tar.gz"

  echo "[INFO] 组装发布目录: ${bundle_dir}"
  rm -rf "${bundle_dir}"
  mkdir -p "${bundle_dir}/backend" "${bundle_dir}/frontend" "${bundle_dir}/scripts"

  cp -f "${REPO_ROOT}/${BACKEND_JAR}" "${bundle_dir}/backend/app-for-ise-backend.jar"
  rsync -a --delete "${WEB_DIR}/dist/" "${bundle_dir}/frontend/"

  cp -f "${SCRIPT_DIR}/runtime-install.sh" "${bundle_dir}/scripts/runtime-install.sh"
  cp -f "${SCRIPT_DIR}/runtime-start.sh" "${bundle_dir}/scripts/runtime-start.sh"
  cp -f "${SCRIPT_DIR}/runtime-stop.sh" "${bundle_dir}/scripts/runtime-stop.sh"
  cp -f "${SCRIPT_DIR}/runtime-status.sh" "${bundle_dir}/scripts/runtime-status.sh"
  chmod +x "${bundle_dir}/scripts/"*.sh

  cat > "${bundle_dir}/DEPLOY.md" <<'EOF'
# Runtime Deployment Guide

1) 将整个目录上传到部署服务器（例如 `/tmp/app-for-ise-runtime-*`）。
2) 进入目录后执行：

```bash
./scripts/runtime-install.sh --public-ip <部署机IP>
```

3) 后续启停：

```bash
./scripts/runtime-start.sh
./scripts/runtime-stop.sh
./scripts/runtime-status.sh
```
EOF

  echo "[INFO] 打包压缩: ${archive}"
  mkdir -p "${OUTPUT_DIR}"
  tar -C "${OUTPUT_DIR}" -czf "${archive}" "${bundle_name}"

  echo "[INFO] 发布包完成:"
  echo "  ${archive}"
}

main() {
  parse_args "$@"
  resolve_version

  need_cmd tar
  need_cmd rsync
  need_cmd mvn
  need_cmd npm

  if [[ "${SKIP_BUILD}" != "1" ]]; then
    build_artifacts
  else
    echo "[INFO] 跳过构建（--skip-build）"
  fi

  locate_backend_jar
  validate_sources
  make_bundle
}

main "$@"
