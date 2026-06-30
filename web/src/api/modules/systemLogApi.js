export async function getSystemLogs() {
  return [
    {
      levelLabel: "错误",
      module: "application",
      requestId: "202605120930001001",
      path: "审批处理",
      message: "审批状态冲突",
      detail: "同一申请被重复审批，后端返回 40900。",
    },
    {
      levelLabel: "警告",
      module: "frontend",
      requestId: "202605120914001112",
      path: "学生画像",
      message: "学生画像页面渲染异常",
      detail: "前端捕获页面渲染异常，并已记录到系统日志。",
    },
  ];
}
