export const tools = [
  {
    key: 'maestro',
    path: '/maestro',
    title: '自动成片',
    category: 'maestro',
    icon: 'Film',
    description: '把主题交给 AI，直接得到成片。脚本、画面、配音、字幕和音乐自动完成。',
    model: 'Maestro 2.0',
    price: '21.00',
    resultType: 'video',
    promptLabel: '提示词',
    promptPlaceholder: '描述你想拍的成片主题，例如：一席中国味，从川味到江南',
    fields: [
      { type: 'upload', key: 'reference', label: '参考素材', accept: 'image/*,video/*,audio/*', multiple: true, text: '上传文件' },
      { type: 'select', key: 'language', label: '语言', options: ['简体中文', 'English'], value: '简体中文' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '9:16', w: 16, h: 28 },
        { value: '16:9', w: 28, h: 16 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '9:16' },
      { type: 'select', key: 'duration', label: '时长', options: ['自定义', '15s', '30s', '60s'], value: '自定义' },
      { type: 'select', key: 'videoType', label: '视频类型', options: ['图文旁白', '成片', '短剧', '数字人 · 口播'], value: '图文旁白' },
      { type: 'switch', key: 'subtitle', label: '字幕 · 花字', value: true },
      { type: 'select', key: 'style', label: '自定义视觉风格', options: ['请选择', '电影感', '国风', '纪实', '商业广告'], value: '请选择' },
      { type: 'select', key: 'voice', label: '自定义配音音色', options: ['请选择', '磁性男声', '温柔女声', '活力少女', '沉稳大叔'], value: '请选择' }
    ]
  },
  {
    key: 'openai-image',
    path: '/openai-image',
    title: 'OpenAI Image',
    category: 'image',
    icon: 'Picture',
    description: '文字渲染准确，适合海报、图表与带字设计稿。',
    model: 'gpt-image-2',
    price: '0.19',
    resultType: 'image',
    promptLabel: '图像提示',
    promptPlaceholder: '输入提示词，例如：一张极简风格的产品发布会海报',
    promptIndex: 3,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['gpt-image-2', 'gpt-image-1.5', 'gpt-image-1', 'gpt-image-2-mini'], value: 'gpt-image-2' },
      { type: 'select', key: 'size', label: '尺寸', options: ['自动', '1024x1024', '1536x1024', '1024x1536', '自定义尺寸'], value: '自动' },
      { type: 'select', key: 'quality', label: '画质', options: ['自动', '低', '中', '高'], value: '自动' },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传图片' }
    ]
  },
  {
    key: 'nanobanana',
    path: '/nanobanana',
    title: 'Nano Banana',
    category: 'image',
    icon: 'Cherry',
    description: '生成与编辑一体，对指令理解精准，适合快速改图。',
    model: 'nano-banana',
    price: '0.14',
    resultType: 'image',
    promptLabel: '图像提示',
    promptPlaceholder: '输入提示词，例如：蓝调雪后山地旅舍，覆雪木石与窗内暖光',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['nano-banana-pro', 'nano-banana', 'nano-banana-edit'], value: 'nano-banana' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['1K', '2K', '4K'], value: '1K' },
      { type: 'ratio', key: 'ratio', label: '宽高比', options: imageRatios, value: '1:1' },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传图片' }
    ]
  },
  {
    key: 'seedream',
    path: '/seedream',
    title: 'SeeDream',
    category: 'image',
    icon: 'Sunny',
    description: '中文语义理解出色，支持一次生成多张风格统一的组图。',
    model: 'doubao-seedream-5.0-lite',
    price: '0.37',
    resultType: 'image',
    promptLabel: '图像提示',
    promptPlaceholder: '输入提示词，例如：星河沙海旅人营地，银河横贯夜空',
    promptIndex: 6,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['doubao-seedream-5.0', 'doubao-seedream-5.0-lite', 'doubao-seedream-4.0', 'doubao-seedream-3.5'], value: 'doubao-seedream-5.0-lite' },
      { type: 'select', key: 'size', label: '尺寸', options: ['1K', '2K', '4K'], value: '2K' },
      { type: 'select', key: 'count', label: '图片张数', options: ['1', '2', '4'], value: '1' },
      { type: 'select', key: 'format', label: '输出格式', options: ['PNG', 'JPEG'], value: 'PNG' },
      { type: 'switch', key: 'enhance', label: '提示词优化', value: true },
      { type: 'switch', key: 'webSearch', label: '联网搜索', value: false },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传图片' }
    ]
  },
  {
    key: 'flux',
    path: '/flux',
    title: 'Flux',
    category: 'image',
    icon: 'Camera',
    description: '写实细节与文字排版兼优，适合摄影感画面与商业视觉。',
    model: 'Flux 2 Pro',
    price: '0.22',
    resultType: 'image',
    promptLabel: '图像提示',
    promptPlaceholder: '输入提示词，例如：清晨薄雾中的玻璃温室，露水与丁达尔光',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Flux 2 Pro', 'Flux 2 Dev', 'Flux 1.1 Pro Ultra', 'Flux Kontext'], value: 'Flux 2 Pro' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['1K', '2K', '4K'], value: '1K' },
      { type: 'ratio', key: 'ratio', label: '宽高比', options: imageRatios, value: '1:1' },
      { type: 'switch', key: 'raw', label: 'Raw 模式', value: false },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传图片' }
    ]
  },
  {
    key: 'ideogram',
    path: '/ideogram',
    title: 'Ideogram',
    category: 'image',
    icon: 'EditPen',
    description: '排版与文字生成见长，适合 Logo、海报与带字视觉。',
    model: 'Ideogram 3.0',
    price: '0.20',
    resultType: 'image',
    promptLabel: '图像提示',
    promptPlaceholder: '输入提示词，例如：一张瑞士风格爵士音乐节海报，标题排版醒目',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Ideogram 3.0', 'Ideogram 3.0 Turbo', 'Ideogram 2.0'], value: 'Ideogram 3.0' },
      { type: 'ratio', key: 'ratio', label: '宽高比', options: imageRatios, value: '1:1' },
      { type: 'select', key: 'style', label: '风格', options: ['自动', '写实', '设计', '动漫', '3D'], value: '自动' },
      { type: 'switch', key: 'magicPrompt', label: '魔法提示词', value: true },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传图片' }
    ]
  },
  {
    key: 'midjourney',
    path: '/midjourney',
    title: 'Midjourney',
    category: 'image',
    icon: 'Brush',
    description: '图像与视频一体，风格覆盖广，支持高级风格化参数。',
    model: '通用',
    price: '0.25',
    resultType: 'image',
    promptLabel: '图像描述',
    promptPlaceholder: '描述画面，例如：雾中城堡，电影感光影，广角镜头',
    promptIndex: 2,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['图像生成', '视频生成'], value: '图像生成' },
      { type: 'select', key: 'model', label: '模型', options: ['通用', 'Niji'], value: '通用' },
      { type: 'upload', key: 'reference', label: '参考图片', accept: 'image/*', text: '上传' },
      { type: 'ratio', key: 'ratio', label: '比例', options: imageRatios, value: '1:1' },
      { type: 'select', key: 'quality', label: '画质', options: ['标准', '超高'], value: '标准' },
      { type: 'select', key: 'version', label: '版本', options: ['8.2', '8.1', '7.0'], value: '8.2' },
      { type: 'switch', key: 'hd', label: 'HD 高清', value: false },
      { type: 'select', key: 'style', label: '风格选择', options: ['默认', '写实', '插画', '动漫', '摄影'], value: '默认' },
      { type: 'number', key: 'stylize', label: '高级风格化', min: 0, max: 1000, value: 100 },
      { type: 'number', key: 'weird', label: '怪异度', min: 0, max: 3000, value: 0 },
      { type: 'number', key: 'chaos', label: '混乱度', min: 0, max: 100, value: 0 },
      { type: 'upload', key: 'firstFrame', label: '来自首帧图片', accept: 'image/*', text: '上传图片', visibleWhen: { key: 'mode', equals: '视频生成' } },
      { type: 'upload', key: 'lastFrame', label: '尾帧图片', accept: 'image/*', text: '上传图片', visibleWhen: { key: 'mode', equals: '视频生成' } },
      { type: 'switch', key: 'loop', label: '视频循环播放', value: false, visibleWhen: { key: 'mode', equals: '视频生成' } },
      { type: 'select', key: 'videoQuality', label: '视频质量', options: ['480p', '720p'], value: '480p', visibleWhen: { key: 'mode', equals: '视频生成' } }
    ]
  },
  {
    key: 'qrart',
    path: '/qrart',
    title: 'QR Art',
    category: 'image',
    icon: 'Grid',
    description: '把二维码做成可扫描的艺术作品，内置多种预设风格。',
    model: 'QRArt 2.0',
    price: '0.30',
    resultType: 'image',
    promptLabel: '提示',
    promptPlaceholder: '输入二维码指向的内容，或描述希望生成的画面',
    promptIndex: 2,
    fields: [
      { type: 'modeTabs', key: 'inputType', options: ['文本内容输入', '上传提示'], value: '文本内容输入' },
      { type: 'upload', key: 'image', label: '二维码图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'inputType', equals: '上传提示' } },
      { type: 'select', key: 'model', label: '模型', options: ['QRArt 2.0', 'QRArt 1.5'], value: 'QRArt 2.0' },
      { type: 'ratio', key: 'ratio', label: '宽高比', options: imageRatios.slice(0, 5), value: '1:1' },
      { type: 'select', key: 'visibility', label: '可见度', options: ['公开', '私密'], value: '公开' },
      { type: 'tags', key: 'style', label: '预设风格', options: qrStyles, value: ['日落'] },
      { type: 'select', key: 'position', label: '位置', options: ['中心', '左上', '右上', '左下', '右下'], value: '中心' },
      { type: 'switch', key: 'advanced', label: '高级', value: false }
    ]
  },
  {
    key: 'seedance',
    path: '/seedance',
    title: 'SeeDance',
    category: 'video',
    icon: 'VideoCamera',
    description: '支持参考图、参考音频与参考视频，最高可输出 4K。',
    model: 'doubao-seedance-2.0',
    price: '8.40',
    resultType: 'video',
    generateLabel: '生成',
    promptLabel: '提示词',
    promptPlaceholder: '描述镜头内容，例如：墨色河流笔触向下流动，纸层山峦徐徐展开',
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['doubao-seedance-2.0', 'doubao-seedance-1.5', 'doubao-seedance-1.0-pro'], value: 'doubao-seedance-2.0' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 },
        { value: '4:3', w: 26, h: 20 },
        { value: '3:4', w: 20, h: 26 },
        { value: '1:1', w: 22, h: 22 },
        { value: '21:9', w: 30, h: 13 },
        { value: '自适应', w: 24, h: 18 }
      ], value: '16:9' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['480p', '720p', '1080p', '4k'], value: '720p' },
      { type: 'select', key: 'duration', label: '时长', options: ['5s', '10s'], value: '5s' },
      { type: 'switch', key: 'audio', label: '生成音频', value: false },
      { type: 'switch', key: 'lockCamera', label: '锁定镜头', value: false },
      { type: 'number', key: 'seed', label: '随机种子', min: 0, max: 999999, value: 0 },
      { type: 'modeTabs', key: 'refMode', options: ['首尾帧模式', '多模态参考模式'], value: '首尾帧模式' },
      { type: 'upload', key: 'firstFrame', label: '首帧图片（画面动效）', accept: 'image/*', text: '上传', visibleWhen: { key: 'refMode', equals: '首尾帧模式' } },
      { type: 'upload', key: 'lastFrame', label: '尾帧图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'refMode', equals: '首尾帧模式' } },
      { type: 'upload', key: 'refImage', label: '参考图片（让角色说话）', accept: 'image/*', text: '上传', visibleWhen: { key: 'refMode', equals: '多模态参考模式' } },
      { type: 'upload', key: 'refAudio', label: '参考音频（音色/配乐）', accept: 'audio/*', text: '上传', visibleWhen: { key: 'refMode', equals: '多模态参考模式' } },
      { type: 'upload', key: 'refVideo', label: '参考视频', accept: 'video/*', text: '上传', visibleWhen: { key: 'refMode', equals: '多模态参考模式' } }
    ]
  },
  {
    key: 'kling',
    path: '/kling',
    title: 'Kling',
    category: 'video',
    icon: 'MagicStick',
    description: '画面稳定、运镜丰富，另有运动控制与对口型照片玩法。',
    model: 'v2.5-Turbo',
    price: '2.10',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述画面与动作，例如：海边日落，镜头缓慢推进',
    promptIndex: 1,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['视频生成', '动作控制', '会说话的照片'], value: '视频生成' },
      { type: 'select', key: 'model', label: '模型版本', options: ['v2.5-Turbo', 'v2.5-Standard', 'v2.1-Master'], value: 'v2.5-Turbo' },
      { type: 'ratio', key: 'ratio', label: '视频比例', options: [
        { value: '1:1', w: 22, h: 22 },
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 }
      ], value: '16:9' },
      { type: 'upload', key: 'firstFrame', label: '首帧参考图片', accept: 'image/*', text: '上传' },
      { type: 'upload', key: 'lastFrame', label: '尾帧参考图片', accept: 'image/*', text: '上传' },
      { type: 'select', key: 'duration', label: '视频时长', options: ['5s', '10s'], value: '5s' },
      { type: 'select', key: 'videoMode', label: '视频模式', options: ['标准 (720p)', '专业 (1080p)'], value: '标准 (720p)' },
      { type: 'switch', key: 'sound', label: '同步生成音效', value: false },
      { type: 'select', key: 'camera', label: '运镜控制', options: ['不使用', '推镜', '拉镜', '摇镜', '环绕'], value: '不使用' },
      { type: 'number', key: 'freedom', label: '自由度', min: 0, max: 1, step: 0.1, value: 0.5 },
      { type: 'textarea', key: 'negativePrompt', label: '负向提示词', rows: 2, placeholder: '描述不希望出现的内容' }
    ]
  },
  {
    key: 'veo',
    path: '/veo',
    title: 'Veo',
    category: 'video',
    icon: 'Position',
    description: '光影自然、镜头感强，支持多素材融合生成。',
    model: 'Veo 3.1 Fast',
    price: '0.60',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述镜头内容，例如：清晨薄雾中的森林小径，镜头缓慢前移',
    promptIndex: 2,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['文生视频', '图生视频', '多图融合'], value: '文生视频' },
      { type: 'select', key: 'model', label: '模型版本', options: ['Veo 3.1', 'Veo 3.1 Fast', 'Veo 3.0', 'Veo 2'], value: 'Veo 3.1 Fast' },
      { type: 'ratio', key: 'ratio', label: '比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 }
      ], value: '16:9' },
      { type: 'upload', key: 'image', label: '首帧图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'mode', equals: '图生视频' } },
      { type: 'upload', key: 'images', label: '多图素材', accept: 'image/*', multiple: true, text: '上传', visibleWhen: { key: 'mode', equals: '多图融合' } },
      { type: 'switch', key: 'translate', label: '自动翻译', value: true }
    ]
  },
  {
    key: 'grok-video',
    path: '/grok-video',
    title: 'Grok 视频',
    category: 'video',
    icon: 'Promotion',
    description: '支持较长片段生成，适合完整场景演绎。',
    model: 'Grok Imagine Video 1.5 Fast',
    price: '0.63',
    resultType: 'video',
    promptLabel: '提示词',
    promptPlaceholder: '描述视频画面，例如：霓虹雨夜的东京街头，镜头跟拍行人',
    promptIndex: 1,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Grok Imagine Video 1.5 Fast', 'Grok Imagine Video 1.5', 'Grok Imagine Video 1.0'], value: 'Grok Imagine Video 1.5 Fast' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: videoRatios, value: '16:9' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['480p', '720p', '1080p'], value: '720p' },
      { type: 'select', key: 'duration', label: '时长', options: ['6s', '10s', '15s', '30s'], value: '6s' },
      { type: 'upload', key: 'image', label: '图片', accept: 'image/*', text: '上传' },
      { type: 'upload', key: 'reference', label: '参考图', accept: 'image/*', text: '上传' }
    ]
  },
  {
    key: 'pika',
    path: '/pika',
    title: 'Pika',
    category: 'video',
    icon: 'VideoPlay',
    description: '多图创建视频，支持丰富的视频效果。',
    model: '2.0',
    price: '5.34',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述视频内容，例如：云层之上的日落，镜头缓慢环绕',
    promptIndex: 1,
    fields: [
      { type: 'select', key: 'version', label: '模型版本', options: ['2.0', '1.5', '1.0'], value: '2.0' },
      { type: 'switch', key: 'multiImage', label: '多图创建视频', value: false },
      { type: 'upload', key: 'images', label: '参考图片', accept: 'image/*', multiple: true, text: '上传' },
      { type: 'select', key: 'effect', label: '视频效果', options: ['选择', '变形', '膨胀', '爆炸', '融化', '悬浮'], value: '选择' }
    ]
  },
  {
    key: 'minimax',
    path: '/minimax',
    title: 'MiniMax H3',
    category: 'video',
    icon: 'Monitor',
    description: '支持参考图与参考音频，画面稳定、细节丰富。',
    model: 'MiniMax H3',
    price: '3.84',
    resultType: 'video',
    generateLabel: '生成视频',
    promptLabel: '提示词',
    promptPlaceholder: '描述视频画面与镜头运动',
    promptIndex: 1,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['MiniMax H3', 'MiniMax H2', 'MiniMax Hailuo 02'], value: 'MiniMax H3' },
      { type: 'upload', key: 'image', label: '参考图片', accept: 'image/*', text: '上传' },
      { type: 'upload', key: 'audio', label: '参考音频', accept: 'audio/*', text: '上传' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['768P', '2K'], value: '768P' },
      { type: 'ratio', key: 'ratio', label: '宽高比', options: [
        { value: '自适应', w: 24, h: 18 },
        { value: '21:9', w: 30, h: 13 },
        { value: '16:9', w: 28, h: 16 },
        { value: '4:3', w: 26, h: 20 },
        { value: '1:1', w: 22, h: 22 },
        { value: '3:4', w: 20, h: 26 },
        { value: '9:16', w: 16, h: 28 }
      ], value: '16:9' },
      { type: 'select', key: 'duration', label: '时长（秒）', options: ['4s', '6s', '8s'], value: '4s' }
    ]
  },
  {
    key: 'sora',
    path: '/sora',
    title: 'Sora',
    category: 'video',
    icon: 'Film',
    description: '物理一致性出色，支持音画同步与多镜头分镜。',
    model: 'Sora 2',
    price: '4.20',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述场景、动作与镜头，例如：雨夜便利店门口，镜头缓缓推近',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Sora 2', 'Sora 2 Pro', 'Sora 1'], value: 'Sora 2' },
      { type: 'modeTabs', key: 'mode', options: ['文生视频', '图生视频'], value: '文生视频' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '16:9' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['480p', '720p', '1080p'], value: '720p' },
      { type: 'select', key: 'duration', label: '时长', options: ['5s', '10s', '15s', '20s'], value: '10s' },
      { type: 'switch', key: 'audio', label: '生成音频', value: true },
      { type: 'upload', key: 'image', label: '首帧图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'mode', equals: '图生视频' } },
      { type: 'upload', key: 'storyboard', label: '分镜参考图', accept: 'image/*', multiple: true, text: '上传' }
    ]
  },
  {
    key: 'luma',
    path: '/luma',
    title: 'Luma',
    category: 'video',
    icon: 'PartlyCloudy',
    description: '光影与材质表现自然，支持首尾帧与循环片段。',
    model: 'Ray 3',
    price: '3.60',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述镜头与氛围，例如：夕阳下的海岸公路，镜头缓慢横移',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Ray 3', 'Ray 2', 'Ray 2 Flash'], value: 'Ray 3' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '16:9' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['540p', '720p', '1080p', '4K'], value: '1080p' },
      { type: 'select', key: 'duration', label: '时长', options: ['5s', '9s', '10s'], value: '5s' },
      { type: 'switch', key: 'loop', label: '循环播放', value: false },
      { type: 'upload', key: 'firstFrame', label: '首帧图片', accept: 'image/*', text: '上传' },
      { type: 'upload', key: 'lastFrame', label: '尾帧图片', accept: 'image/*', text: '上传' },
      { type: 'switch', key: 'expand', label: '扩展画面', value: false }
    ]
  },
  {
    key: 'runway',
    path: '/runway',
    title: 'Runway',
    category: 'video',
    icon: 'Operation',
    description: '镜头控制精准，适合广告片、MV 与风格化短片。',
    model: 'Gen-5',
    price: '3.20',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述画面与运镜，例如：滑板穿过霓虹隧道，低角度跟拍',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Gen-5', 'Gen-4 Turbo', 'Gen-4'], value: 'Gen-5' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '16:9' },
      { type: 'select', key: 'duration', label: '时长', options: ['5s', '10s'], value: '5s' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['720p', '1080p'], value: '1080p' },
      { type: 'select', key: 'camera', label: '运镜', options: ['自动', '推镜', '拉镜', '环绕', '手持'], value: '自动' },
      { type: 'upload', key: 'image', label: '首帧图片', accept: 'image/*', text: '上传' }
    ]
  },
  {
    key: 'wan',
    path: '/wan',
    title: '通义万相',
    category: 'video',
    icon: 'Cloudy',
    description: '中文提示词理解好，支持文生视频与首尾帧控制。',
    model: 'Wan 2.5',
    price: '2.60',
    resultType: 'video',
    promptLabel: '视频提示',
    promptPlaceholder: '描述画面，例如：江南水乡清晨，乌篷船穿过石桥，薄雾弥漫',
    promptIndex: 2,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Wan 2.5', 'Wan 2.2', 'Wan 2.1'], value: 'Wan 2.5' },
      { type: 'modeTabs', key: 'mode', options: ['文生视频', '图生视频', '首尾帧'], value: '文生视频' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '16:9', w: 28, h: 16 },
        { value: '9:16', w: 16, h: 28 },
        { value: '4:3', w: 26, h: 20 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '16:9' },
      { type: 'select', key: 'resolution', label: '分辨率', options: ['480p', '720p', '1080p'], value: '720p' },
      { type: 'select', key: 'duration', label: '时长', options: ['5s', '10s'], value: '5s' },
      { type: 'switch', key: 'audio', label: '生成音频', value: false },
      { type: 'upload', key: 'image', label: '参考图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'mode', equals: '图生视频' } },
      { type: 'upload', key: 'firstFrame', label: '首帧图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'mode', equals: '首尾帧' } },
      { type: 'upload', key: 'lastFrame', label: '尾帧图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'mode', equals: '首尾帧' } }
    ]
  },
  {
    key: 'digital-human',
    path: '/digital-human',
    title: '数字人',
    category: 'video',
    icon: 'User',
    description: '上传形象与文案，自动生成自然口播视频与字幕。',
    model: 'Digital Human 2.0',
    price: '2.80',
    resultType: 'video',
    generateLabel: '生成口播视频',
    promptLabel: '口播文案',
    promptPlaceholder: '输入口播文案，例如：大家好，今天带你了解这款新品的三個亮点',
    promptIndex: 3,
    fields: [
      { type: 'select', key: 'model', label: '模型', options: ['Digital Human 2.0', 'Digital Human 1.5'], value: 'Digital Human 2.0' },
      { type: 'upload', key: 'avatar', label: '形象照片（正面清晰）', accept: 'image/*', text: '上传形象' },
      { type: 'select', key: 'voice', label: '音色', options: ['温柔女声', '磁性男声', '活力少女', '沉稳大叔', '英文女声'], value: '温柔女声' },
      { type: 'ratio', key: 'ratio', label: '画面比例', options: [
        { value: '9:16', w: 16, h: 28 },
        { value: '16:9', w: 28, h: 16 },
        { value: '1:1', w: 22, h: 22 }
      ], value: '9:16' },
      { type: 'select', key: 'background', label: '背景', options: ['纯色', '影棚', '办公室', '户外', '自定义上传'], value: '影棚' },
      { type: 'upload', key: 'backgroundImage', label: '背景图片', accept: 'image/*', text: '上传', visibleWhen: { key: 'background', equals: '自定义上传' } },
      { type: 'select', key: 'gesture', label: '手势幅度', options: ['自然', '小幅', '大幅'], value: '自然' },
      { type: 'switch', key: 'subtitle', label: '自动字幕', value: true }
    ]
  },
  {
    key: 'suno',
    path: '/suno',
    title: 'Suno',
    category: 'music',
    icon: 'Headset',
    description: '从风格和歌词到完整音乐，风格覆盖广，人声自然。',
    model: 'v6',
    price: '0.56',
    resultType: 'music',
    layout: 'music',
    promptLabel: '歌曲描述',
    promptPlaceholder: '描述想要的歌曲氛围、主题与情绪',
    promptIndex: 4,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['简单定制', '高级参数'], value: '简单定制' },
      { type: 'select', key: 'model', label: '模型', options: ['v6', 'v5.5', 'v5', 'v4.5'], value: 'v6' },
      { type: 'switch', key: 'instrumental', label: '纯音乐', value: false },
      { type: 'upload', key: 'referenceAudio', label: '参考音乐', accept: 'audio/*', text: '+ 音频' },
      { type: 'tags', key: 'quickStyles', label: '灵感创作', options: ['Lullaby', 'Afrobeat', 'Bossa Nova', 'J-Pop', 'Hip Hop', 'Jazz Night', 'Trap', 'Folk Story'], value: [] },
      { type: 'textarea', key: 'lyrics', label: '歌词', rows: 5, maxlength: 5000, placeholder: '输入歌词，或让 AI 根据描述生成' },
      { type: 'textarea', key: 'style', label: '风格', rows: 2, maxlength: 1000, placeholder: '输入音乐风格标签，例如：民谣、温暖、木吉他' },
      { type: 'text', key: 'title', label: '标题', placeholder: '歌曲标题（留空自动生成）' },
      { type: 'segmented', key: 'vocalGender', label: '人声性别', options: ['自动', '女声', '男声'], value: '自动' },
      { type: 'segmented', key: 'variant', label: '变体类型', options: ['自动', '高变体', '低变体'], value: '自动' },
      { type: 'text', key: 'singerStyle', label: '歌手风格', placeholder: '输入歌手风格 ID', visibleWhen: { key: 'mode', equals: '高级参数' } }
    ],
    actions: ['AI 生成歌词', '混搭歌词', '优化风格']
  },
  {
    key: 'producer',
    path: '/producer',
    title: 'Producer',
    category: 'music',
    icon: 'Microphone',
    description: '单曲最长八分钟，可调节人声强度与歌词表现力。',
    model: 'FUZZ-2.0 Pro',
    price: '0.58',
    resultType: 'music',
    layout: 'music',
    promptLabel: '歌曲描述',
    promptPlaceholder: '描述歌曲主题、情绪与使用场景',
    promptIndex: 3,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['简单模式', '定制模式'], value: '简单模式' },
      { type: 'select', key: 'model', label: '模型', options: ['FUZZ-2.0 Pro', 'FUZZ-2.0', 'FUZZ-1.5'], value: 'FUZZ-2.0 Pro' },
      { type: 'upload', key: 'reference', label: '参考音乐', accept: 'audio/*', text: '上传' },
      { type: 'number', key: 'creativity', label: '创意度', min: 0, max: 1, step: 0.1, value: 0.5 },
      { type: 'number', key: 'voiceStrength', label: '声音强度', min: 0, max: 1, step: 0.1, value: 0.7 },
      { type: 'number', key: 'seed', label: '种子', min: 0, max: 999999, value: 0 },
      { type: 'textarea', key: 'lyrics', label: '歌词', rows: 4, maxlength: 5000, placeholder: '输入歌词，或让 AI 生成' },
      { type: 'textarea', key: 'style', label: '风格', rows: 2, maxlength: 1000, placeholder: '输入音乐风格标签' },
      { type: 'text', key: 'title', label: '标题', placeholder: '歌曲标题' },
      { type: 'segmented', key: 'vocalGender', label: '人声性别', options: ['自动', '女声', '男声'], value: '自动' }
    ],
    actions: ['AI 生成歌词']
  },
  {
    key: 'udio',
    path: '/udio',
    title: 'Udio',
    category: 'music',
    icon: 'Bell',
    description: '人声质感与编曲层次细腻，适合流行与电子风格。',
    model: 'Udio 3',
    price: '0.50',
    resultType: 'music',
    layout: 'music',
    promptLabel: '歌曲描述',
    promptPlaceholder: '描述歌曲风格、情绪与使用场景',
    promptIndex: 4,
    fields: [
      { type: 'modeTabs', key: 'mode', options: ['简单模式', '自定义模式'], value: '简单模式' },
      { type: 'select', key: 'model', label: '模型', options: ['Udio 3', 'Udio 2.5', 'Udio 2'], value: 'Udio 3' },
      { type: 'switch', key: 'instrumental', label: '纯音乐', value: false },
      { type: 'tags', key: 'quickStyles', label: '灵感创作', options: ['City Pop', 'Lo-Fi', '摇滚', '国风', '电子', 'R&B'], value: [] },
      { type: 'textarea', key: 'lyrics', label: '歌词', rows: 4, maxlength: 5000, placeholder: '输入歌词，或让 AI 生成' },
      { type: 'textarea', key: 'style', label: '风格', rows: 2, maxlength: 1000, placeholder: '输入音乐风格标签' },
      { type: 'text', key: 'title', label: '标题', placeholder: '歌曲标题' },
      { type: 'segmented', key: 'vocalGender', label: '人声性别', options: ['自动', '女声', '男声'], value: '自动' }
    ],
    actions: ['AI 生成歌词']
  }
]

export const toolCategories = [
  {
    key: 'chat',
    title: '和 AI 对话',
    description: '写作、分析、学习和灵感碰撞',
    icon: 'ChatDotRound',
    image: '/images/category-chat.jpg',
    tools: ['chatgpt', 'claude', 'gemini', 'grok', 'deepseek', 'kimi', 'qwen', 'doubao']
  },
  {
    key: 'image',
    title: '生成一张图片',
    description: '海报、商品图、角色和视觉设计',
    icon: 'Picture',
    image: '/images/category-image.jpg',
    tools: ['nanobanana', 'openai-image', 'seedream', 'flux', 'ideogram']
  },
  {
    key: 'video',
    title: '制作一段视频',
    description: '文生视频、图生视频和参考素材创作',
    icon: 'VideoCamera',
    image: '/images/category-video.jpg',
    tools: ['seedance', 'sora', 'kling', 'veo', 'runway', 'luma']
  },
  {
    key: 'music',
    title: '创作一首歌曲',
    description: '从风格和歌词到完整音乐',
    icon: 'Headset',
    image: '/images/category-music.jpg',
    tools: ['suno', 'producer', 'udio']
  }
]

export const showcases = [
  {
    id: 'sc-maestro-china',
    title: '一席中国味',
    subtitle: '五个地域片段以清晰节奏呈现川味、粤菜、江南与面食。',
    toolKey: 'maestro',
    prompt: '一席中国味：从川味、粤菜、江南到面食，五个地域片段节奏清晰，温暖纪实风格',
    params: { ratio: '9:16', videoType: '成片', subtitle: true, style: '纪实' },
    image: '/images/showcase-sc-maestro-china.jpg'
  },
  {
    id: 'sc-nano-lodge',
    title: '蓝调雪后山地旅舍',
    subtitle: '覆雪木石旅舍坐落冷蓝山林，窗内暖光与入口灯笼营造安静归宿感。',
    toolKey: 'nanobanana',
    prompt: '覆雪木石旅舍坐落冷蓝山林，窗内暖光与入口灯笼，安静归宿感，电影感',
    params: { ratio: '3:2', resolution: '2K' },
    image: '/images/showcase-sc-nano-lodge.jpg'
  },
  {
    id: 'sc-nano-portrait',
    title: '沉静高管办公室肖像',
    subtitle: '深色短发高管身着炭灰西装端坐办公室，直视镜头并露出沉稳浅笑。',
    toolKey: 'nanobanana',
    prompt: '深色短发高管身着炭灰西装端坐办公室，直视镜头，沉稳浅笑，专业肖像摄影',
    params: { ratio: '3:4', resolution: '2K' },
    image: '/images/showcase-sc-nano-portrait.jpg'
  },
  {
    id: 'sc-gpt-lamp',
    title: '森林绿陶艺台灯',
    subtitle: '深绿手作陶瓷灯座搭配天然亚麻灯罩，温暖灯光在石灰岩台面投下柔和阴影。',
    toolKey: 'openai-image',
    prompt: '深绿手作陶瓷灯座搭配天然亚麻灯罩，温暖灯光在石灰岩台面投下柔和阴影，产品摄影',
    params: { model: 'gpt-image-2', size: '1024x1024', quality: '高' },
    image: '/images/showcase-sc-gpt-lamp.jpg'
  },
  {
    id: 'sc-seedance-ink',
    title: '墨河山径初醒',
    subtitle: '黑色河流笔触向下流动，纸层山峦徐徐展开，朱红小径从雾中浮现。',
    toolKey: 'seedance',
    prompt: '黑色河流笔触向下流动，纸层山峦徐徐展开，朱红小径从雾中浮现，东方水墨动画',
    params: { ratio: '16:9', resolution: '1080p', duration: '5s' },
    image: '/images/showcase-sc-seedance-ink.jpg'
  },
  {
    id: 'sc-seedream-desert',
    title: '星河沙海旅人营地',
    subtitle: '月隐沙海上银河横贯夜空，毡帐、骆驼与微小旅人围绕暖色营火休憩。',
    toolKey: 'seedream',
    prompt: '月隐沙海上银河横贯夜空，毡帐、骆驼与微小旅人围绕暖色营火休憩，超广角',
    params: { size: '2K', count: '1', format: 'PNG' },
    image: '/images/showcase-sc-seedream-desert.jpg'
  },
  {
    id: 'sc-nano-rain',
    title: '折叠雨水收集器',
    subtitle: '原创家用雨水收集器以折叠织物漏斗、透明储水腔和稳定底座展示工业设计。',
    toolKey: 'nanobanana',
    prompt: '原创家用雨水收集器，折叠织物漏斗、透明储水腔与稳定底座，工业设计渲染图',
    params: { ratio: '4:3', resolution: '2K' },
    image: '/images/showcase-sc-nano-rain.jpg'
  },
  {
    id: 'sc-gpt-poster',
    title: '静默之形陶艺展海报',
    subtitle: '以留白与器物轮廓构成展览主视觉，中文标题排版克制清晰。',
    toolKey: 'openai-image',
    prompt: '静默之形陶艺展海报，大量留白，器物轮廓，中文标题排版克制清晰，极简设计',
    params: { model: 'gpt-image-2', size: '1024x1536', quality: '高' },
    image: '/images/showcase-sc-gpt-poster.jpg'
  }
]
