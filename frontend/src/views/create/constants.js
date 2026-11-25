/** 创作页静态配置：与业务状态无关，集中放置便于查找与复用。 */

export const tabs = [
  { key: 'current', label: '当前任务' },
  { key: 'gallery', label: '画廊' }
]

// 前端演示使用本地示例图，避免依赖在线渲染服务
export const demoCovers = {
  image: [
    '/images/showcase-sc-nano-lodge.jpg',
    '/images/showcase-sc-gpt-lamp.jpg',
    '/images/showcase-sc-nano-rain.jpg',
    '/images/showcase-sc-gpt-poster.jpg',
    '/images/category-image.jpg'
  ],
  video: [
    '/images/showcase-sc-seedance-ink.jpg',
    '/images/showcase-sc-seedream-desert.jpg',
    '/images/showcase-sc-maestro-china.jpg',
    '/images/category-video.jpg'
  ]
}

export const demoFiles = {
  video: ['/media/demo-video-ink.mp4', '/media/demo-video-desert.mp4'],
  music: ['/media/demo-music.wav']
}

export const SERVICE_LABEL = {
  chat: '对话',
  image: '图像生成',
  video: '视频生成',
  music: '音乐生成',
  maestro: '视频生成'
}
