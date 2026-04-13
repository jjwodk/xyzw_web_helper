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
- **APK 微信登录 CORS 问题** (2026-04-13)

## APK 微信登录解决方案
微信 API (`open.weixin.qq.com`) 不支持 CORS，前端直接请求会被浏览器安全策略拦截。

**解决方案：后端代理**
- 已创建 `server/weixin_proxy.py` Flask 后端代理服务
- 前端 `wxqrcode.vue` 已修改为请求 `http://localhost:3000/api/weixin/*`
- 后端代理转发请求到微信/仙境服务器

**使用方法：**
1. 启动后端：`cd server && python weixin_proxy.py`（端口 3000）
2. 同时运行前端：`pnpm run dev`
3. 开发模式下正常可用

**APK 部署注意：**
- APK 中 localhost 指向模拟器本身
- 需要修改代码支持可配置的代理地址，或在 APK 中内置后端
