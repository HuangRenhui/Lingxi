/** 钱包页静态配置。 */

export const payMethods = ['支付宝', '微信支付']

export const amounts = [
  { value: 50, bonus: '' },
  { value: 100, bonus: '' },
  { value: 300, bonus: '送 20 积分' },
  { value: 500, bonus: '送 50 积分' },
  { value: 1000, bonus: '送 150 积分' },
  { value: 2000, bonus: '送 400 积分' }
]

export const rules = [
  '对话按 token 计费，图片按张、视频按秒、音乐按首计费。',
  '任务开始前预扣积分，失败或取消自动退回。',
  '充值余额用于全部应用，不支持提现。',
  '如需对账，可在本页导出积分流水。'
]
