/** 主框架菜单与左侧大类配置。 */

/**
 * 左侧大类分片：对话、图片、视频、音乐。
 * maestro（自动成片）本质是视频工具，归入视频组。
 */
export function createRailGroups(tools, chatApps) {
  return [
    {
      key: 'chat',
      label: '对话',
      title: '对话应用',
      description: '与主流大模型对话，写作、分析、推理都在这里。',
      icon: 'ChatDotRound',
      items: chatApps
    },
    {
      key: 'image',
      label: '图片',
      title: '图片生成',
      description: '文生图、改图、海报与商业视觉设计。',
      icon: 'Picture',
      items: tools.filter(item => item.category === 'image')
    },
    {
      key: 'video',
      label: '视频',
      title: '视频创作',
      description: '文生视频、图生视频、成片与数字人口播。',
      icon: 'VideoCamera',
      items: tools.filter(item => item.category === 'video' || item.category === 'maestro')
    },
    {
      key: 'music',
      label: '音乐',
      title: '音乐生成',
      description: '从风格、歌词到完整歌曲与人声。',
      icon: 'Headset',
      items: tools.filter(item => item.category === 'music')
    }
  ]
}

export const consoleMenus = [
  { path: '/console/overview', title: '概览', icon: 'Odometer' },
  { path: '/console/usages', title: '使用历史', icon: 'DataLine' },
  { path: '/console/wallet', title: '钱包与充值', icon: 'Wallet' },
  { path: '/console/subscription', title: '订阅套餐', icon: 'Medal' },
  { path: '/console/orders', title: '订单列表', icon: 'Tickets' },
  { path: '/console/coupons', title: '兑换与优惠券', icon: 'Present' },
  { path: '/console/connectors', title: '连接器', icon: 'Connection' },
  { path: '/console/skills', title: '技能', icon: 'MagicStick' },
  { path: '/console/browser-devices', title: '浏览器设备', icon: 'Monitor' },
  { path: '/console/earnings', title: '推荐收益', icon: 'Coin' }
]
