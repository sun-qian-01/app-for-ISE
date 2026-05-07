const { notices } = require('../../utils/mock');

Page({
  data: {
    noticeList: notices
  },

  onMarkRead(event) {
    const id = Number(event.currentTarget.dataset.id);
    const noticeList = this.data.noticeList.map((item) => {
      if (item.id === id) {
        return { ...item, read: true };
      }
      return item;
    });

    this.setData({ noticeList });
    wx.showToast({ title: '已标记已读', icon: 'success' });
  },

  onMarkAllRead() {
    const noticeList = this.data.noticeList.map((item) => ({ ...item, read: true }));
    this.setData({ noticeList });
    wx.showToast({ title: '全部已读', icon: 'success' });
  }
});
