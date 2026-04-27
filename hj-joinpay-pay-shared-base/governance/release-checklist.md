# 发布检查清单

发布前逐项检查，确保所有项通过后才可发布。

## 结构检查

- [ ] 所有 Skill 目录包含 `SKILL.md`
- [ ] 所有 SKILL.md 包含 frontmatter（`---` 包裹的 YAML 头部）
- [ ] frontmatter 中的 `version` 与正文中版本表一致
- [ ] 所有 Skill 目录在 README.md 中有记录
- [ ] LICENSE、CHANGELOG.md、README.md 存在

## 内容检查

- [ ] 所有相对链接指向的文件存在
- [ ] license 字段值为 `MIT`
- [ ] 无重复内容（共享资料统一在 shared-base 维护）
- [ ] 凭据边界说明正确指向 `credential-boundary.md`

## 版本检查

- [ ] 所有 Skill 版本号一致
- [ ] CHANGELOG.md 包含本次版本变更记录
- [ ] frontmatter version 与 body version 一致

## 自动化校验

运行 `scripts/validate-skills.sh` 自动检查以上项目。
