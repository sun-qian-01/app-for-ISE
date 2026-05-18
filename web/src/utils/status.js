export function statusClass(status) {
  if (["approved", "已通过", "成功"].includes(status)) return "is-done";
  if (["reviewing", "审核中", "待审核"].includes(status)) return "is-current";
  if (["supplement_required", "需补充", "退回", "部分失败", "已退回"].includes(status)) return "is-warn";
  return "";
}

export function stageLabel(status) {
  const map = {
    approved: "已完成",
    reviewing: "审核中",
    pending: "未开始",
  };
  return map[status] || status;
}
