# 英语冒险岛 Android V1.2

适用：6岁、英语零基础儿童；建议每天约15分钟，采用“认识单词—听音辨认—小游戏复习”的轻量节奏。

## V1.2 更新
- 新增 **单词气球大冒险**：听系统朗读的英语动物单词，从三个气球中戳中正确动物；共5关，答对获得星星。
- 动物单词卡支持点击大动物图标，触发角色自我介绍（例如 “Hello! I am a cat.”）。
- 游戏中心更新为4种小游戏。
- 修复记忆翻翻乐配对成功时重复刷新界面的问题。
- 星星继续保存在本机，不需要账号或网络。

## 当前功能
- 动物单词卡：CAT、DOG、BIRD、FISH、RABBIT，中英文、音标、例句。
- Android 系统 TTS：美式英语单词和句子朗读，可重复播放。
- 单词复习闯关：看动物选英文。
- 听声音找动物：听英语单词后选择对应动物。
- 记忆翻翻乐：翻卡配对相同动物。
- 给动物送礼物：根据动物选择食物，并听简单英语句子。
- 单词气球大冒险：听音戳中正确动物气球。
- 星星奖励：正确答题/配对/喂食/戳中气球可获得星星，SharedPreferences 本地保存。

## 构建 APK
1. 安装 Android Studio 和 Android SDK Platform 35。
2. Android Studio 打开本文件夹 `EnglishAdventureAndroid`。
3. 等待 Gradle Sync 完成。
4. Build > Build Bundle(s) / APK(s) > Build APK(s)。
5. APK 通常位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 发音
使用 Android TextToSpeech，设置语言 Locale.US。若没有声音，请在手机设置搜索“文字转语音输出”，选择 Google 语音服务或厂商 TTS，并下载 English (United States) 语音数据。不同手机的音色由系统 TTS 引擎决定。

## 版本边界
- 原生 Android Java 项目源码，尚未在 Android Studio / 真机编译验证，不是已签名 APK。
- 需要 Android Studio / Gradle 构建。
- 当前小游戏使用点击交互；尚无麦克风跟读评分、云端 AI 对话和家长统计面板。
