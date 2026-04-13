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
