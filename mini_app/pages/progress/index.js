const { progress } = require('../../utils/mock');

function mapStatus(status) {
  if (status === 'completed') return '已完成';
  if (status === 'processing') return '进行中';
  if (status === 'returned') return '已退回';
  return '未开始';
}

Page({
  data: {
    currentStage: progress.currentStage,
    records: progress.records.map((item) => ({
      ...item,
      statusText: mapStatus(item.status)
    }))
  }
});
