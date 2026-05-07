const { profile, quickEntries, notices } = require('../../utils/mock');

Page({
  data: {
    profile,
    quickEntries,
    recentNotices: notices.slice(0, 2)
  },

  onTapEntry(event) {
    const key = event.currentTarget.dataset.key;
    const pathMap = {
      kb: '/pages/kb/index',
      progress: '/pages/progress/index',
      notice: '/pages/notice/index',
      certificate: '/pages/certificate/index'
    };

    const url = pathMap[key];
    if (url) {
      wx.switchTab({ url });
    }
  }
});
