# MEMORY.md - 长期记忆

## 项目信息
- 项目路径: `e:\xyzw_web_helper-2.1.0\换阵容版本\xyzw_web_helper-main`
- 技术栈: Vue 3 + Vite + TypeScript + Pinia
- 包管理: pnpm
- 默认端口: 10035 (`pnpm run dev`)

## 用户偏好
- 中文用户，习惯用中文沟通
- 偏好深度调试，追踪错误堆栈解决 Bug
- 倾向于结构化解释和代码块展示

## 功能扩展 (2026-04-13)
- 已添加 Capacitor 打包支持，可生成 Android APK
- 新增 GitHub Actions 工作流: `.github/workflows/build-apk.yml`
- 可用脚本:
  - `pnpm android:init` - 构建 Web 并同步到 Android
  - `pnpm android:open` - 用 Android Studio 打开项目
  - `pnpm android:build` - 构建 Debug APK
  - `pnpm apk` - 一键构建 APK

## 最近关注
- `PeachInfo.vue` 组件调试，涉及日期选择器和 WebSocket 逻辑
- `Dashboard.vue` 组件响应式代理 Vue 警告修复
- **APK 微信登录 CORS 问题** (2026-04-13 → 2026-04-14)

## APK 微信登录解决方案

### 架构
- **浏览器开发模式**：`/api/weixin/*` → Vite proxy 转发
- **APK 模式**：自定义 `NativeHttp` JavaScript 接口，绕过 CORS

### 关键文件
- `android/app/src/main/java/com/xyzw/app/MainActivity.java` - NativeHttp接口实现
- `src/views/TokenImport/wxqrcode.vue` - 微信扫码组件
- `index.html` - NativeHttp就绪检测脚本
- `android/app/src/main/res/xml/network_security_config.xml` - 网络安全配置

### 调试日志 (2026-04-14)
添加了详细的调试日志：
- Java层：`android.util.Log.d("NativeHttp", ...)` 
- JS层：`console.log("[微信扫码] ...")`
- 入口检测：`window.__isCapacitorApp`, `window.NativeHttpAvailable`

### 排查方向
如果APK扫码失败，检查：
1. Logcat过滤`NativeHttp`关键字查看注册状态
2. Chrome DevTools远程调试APK WebView
3. 确认APK为最新构建版本
