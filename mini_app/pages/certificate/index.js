const { certificateTemplates, certificateApplications } = require('../../utils/mock');

function mapStatus(status) {
  const map = {
    draft: '草稿',
    submitted: '已提交',
    reviewing: '审核中',
    approved: '已通过',
    rejected: '已驳回',
    revoked: '已撤回'
  };
  return map[status] || status;
}

Page({
  data: {
    templateOptions: certificateTemplates,
    applications: certificateApplications,
    selectedTemplateId: certificateTemplates[0].id,
    selectedTemplateName: certificateTemplates[0].name,
    purpose: ''
  },

  onTemplateChange(event) {
    const index = Number(event.detail.value);
    const selectedTemplate = this.data.templateOptions[index];
    this.setData({
      selectedTemplateId: selectedTemplate.id,
      selectedTemplateName: selectedTemplate.name
    });
  },

  onPurposeInput(event) {
    this.setData({ purpose: event.detail.value });
  },

  onSubmitApplication() {
    const { selectedTemplateId, templateOptions, purpose, applications } = this.data;
    const purePurpose = purpose.trim();

    if (!purePurpose) {
      wx.showToast({ title: '请填写用途', icon: 'none' });
      return;
    }

    const selectedTemplate = templateOptions.find((item) => item.id === selectedTemplateId);
    const newRecord = {
      id: Date.now(),
      templateId: selectedTemplateId,
      templateName: selectedTemplate ? selectedTemplate.name : '未知模板',
      purpose: purePurpose,
      status: 'submitted',
      statusText: this.formatStatus('submitted'),
      createdAt: this.formatNow()
    };

    this.setData({
      applications: [newRecord, ...applications],
      purpose: ''
    });

    wx.showToast({ title: '申请已提交', icon: 'success' });
  },

  onRevoke(event) {
    const id = Number(event.currentTarget.dataset.id);
    const applications = this.data.applications.map((item) => {
      if (item.id === id) {
        return { ...item, status: 'revoked', statusText: this.formatStatus('revoked') };
      }
      return item;
    });

    this.setData({ applications });
    wx.showToast({ title: '已撤回', icon: 'success' });
  },

  formatStatus(status) {
    return mapStatus(status);
  },

  formatNow() {
    const now = new Date();
    const pad = (n) => `${n}`.padStart(2, '0');
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
  },

  onLoad() {
    const applications = this.data.applications.map((item) => ({
      ...item,
      statusText: this.formatStatus(item.status)
    }));
    this.setData({ applications });
  }
});
