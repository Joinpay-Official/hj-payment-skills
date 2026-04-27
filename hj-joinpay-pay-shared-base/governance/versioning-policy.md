# 版本治理规则

## 版本号格式

遵循语义化版本（Semantic Versioning）：`MAJOR.MINOR.PATCH`

- **MAJOR**：不兼容的 API 变更
- **MINOR**：向后兼容的功能新增
- **PATCH**：向后兼容的问题修复

## 版本同步

- 同一产品包内所有 Skill 的版本号保持一致
- 发布时统一递增版本号，避免出现版本割裂

## 变更记录

所有变更记录在仓库根目录的 `CHANGELOG.md` 中，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

## 文档更新

- 接口参数变更必须同步更新对应的 references 文档
- 版本号变更必须同步更新 SKILL.md 的 frontmatter 和正文版本表
