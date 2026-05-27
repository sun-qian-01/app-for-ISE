export async function getAuditLogs() {
  return [
    {
      actor: "辅导员 李老师",
      module: "学生画像",
      action: "查看学生敏感字段：联系方式",
      time: "2026-04-19 09:12:00",
      result: "成功",
    },
    {
      actor: "辅导员 李老师",
      module: "通知",
      action: "发布定向通知：奖学金材料提交",
      time: "2026-04-18 17:43:00",
      result: "成功",
    },
  ];
}
