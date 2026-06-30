export function statusClass(status) {
  if (["approved", "已通过", "成功"].includes(status)) return "is-done";
  if (["submitted", "reviewing", "审核中", "待审核"].includes(status)) return "is-current";
  if (["supplement_required", "returned", "return", "需补充", "退回", "部分失败", "已退回"].includes(status)) return "is-warn";
  return "";
}

export function stageLabel(status) {
  const map = {
    approved: "已完成",
    submitted: "已提交",
    reviewing: "审核中",
    pending: "未开始",
    returned: "已退回",
    return: "已退回",
  };
  return map[status] || status;
}
