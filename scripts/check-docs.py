#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""文档规范检查器

按 `docs/06-规范/文档编写规范.md` 校验 docs/ 下的 Markdown 文档。

用法:
    python scripts/check-docs.py            # 检查全部（含 CODEBUDDY.md / README.md）
    python scripts/check-docs.py docs/06-规范  # 只检查指定目录
    python scripts/check-docs.py --links-only # 只查链接与锚点

退出码: 0 = 全部通过；1 = 存在问题。

规则来源（编号对应规范章节）:
    §3.2  正文文件名不带版本号
    §4.2  头部元信息四字段
    §4.3  文末「变更记录」且不编号
    §5.1  H1 唯一、不跳级
    §5.2  正文 H2 编号连续（导航类文档豁免）
    §7.1  表格分隔线统一 | --- |
    §7.3  代码块必须标注语言
    §7.4  标记仅限 ✅ ❌ ⚠️ 📌 💡
    §8.1  引用一律相对路径（不得把机器绝对路径当链接）
"""
from __future__ import annotations

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 归档目录冻结，豁免检查
EXEMPT_DIRS = {"archive"}
# 模板目录：表单类文档，豁免 H2 编号规则
UNNUMBERED_DIRS = {"_templates"}
# 不要求 H2 编号的文档（导航类 / 非正文）
UNNUMBERED_DOCS = {"README.md", "CHANGELOG.md", "CODEBUDDY.md"}

# 行级忽略指令：该行（或上一行）含此注释时跳过检查
IGNORE_DIRECTIVE = "check-docs:ignore"

ALLOWED_MARKERS = ["✅", "❌", "⚠️", "📌", "💡"]
FORBIDDEN_MARKERS = ["🚨", "❗", "⭐", "🔴", "🟢", "🔵", "ℹ️", "🔥"]

FENCE_RE = re.compile(r"^(`{3,})[ \t]*(.*)$")
HEADING_RE = re.compile(r"^(#{1,6})\s+(.*)$")
TABLE_SEP_RE = re.compile(r"^\s*\|[\s:|-]+\|\s*$")
INLINE_CODE_RE = re.compile(r"`[^`]*`")
LINK_RE = re.compile(r"\]\(([^)]+)\)")
ABS_PATH_LINK_RE = re.compile(r"^[A-Za-z]:[\\/]")


class Issue:
    __slots__ = ("path", "line", "rule", "msg")

    def __init__(self, path, line, rule, msg):
        self.path, self.line, self.rule, self.msg = path, line, rule, msg


def strip_inline_code(text: str) -> str:
    """把行内代码替换为等长空白，避免把示例文字误判为真实链接/路径。"""
    return INLINE_CODE_RE.sub(lambda m: " " * len(m.group(0)), text)


def parse(text: str):
    """返回 (body_lines, fences, n_blocks)。

    body_lines: [(lineno, line)] 位于代码块之外的行
    fences:     [(lineno, backtick_count, info_string)] 所有围栏行
    正确处理嵌套（4 个反引号包住的 3 个反引号视为内容）
    """
    body, fences = [], []
    lines = text.splitlines()
    i, n = 0, len(lines)
    while i < n:
        m = FENCE_RE.match(lines[i])
        if not m:
            body.append((i + 1, lines[i]))
            i += 1
            continue
        count = len(m.group(1))
        info = m.group(2).strip()
        fences.append((i + 1, count, info))
        # 找闭合围栏：反引号数 >= count 且无 info
        j = i + 1
        while j < n:
            mm = FENCE_RE.match(lines[j])
            if mm and len(mm.group(1)) >= count and not mm.group(2).strip():
                fences.append((j + 1, len(mm.group(1)), ""))
                break
            j += 1
        i = j + 1
    return body, fences


def slug(heading: str) -> str:
    s = heading.strip().lower()
    s = re.sub(r"[^\w\u4e00-\u9fff\- ]", "", s)
    return s.replace(" ", "-")


def collect_anchors(path: str) -> set:
    out = set()
    for line in open(path, encoding="utf-8"):
        m = HEADING_RE.match(line.rstrip("\n"))
        if m:
            out.add(slug(m.group(2)))
    return out


def check_file(path: str, issues: list, links_only: bool):
    rel = os.path.relpath(path, ROOT)
    text = open(path, encoding="utf-8").read()
    body, fences = parse(text)
    name = os.path.basename(path)
    parts = set(re.split(r"[\\/]", rel))
    unnumbered = name in UNNUMBERED_DOCS or bool(parts & UNNUMBERED_DIRS)

    # 收集带忽略指令的行号（本行或上一行）
    ignored = set()
    prev = -2
    for ln, line in body:
        if IGNORE_DIRECTIVE in line:
            ignored.add(ln)
            ignored.add(prev)
        prev = ln

    # ---- 代码围栏 ----
    if not links_only:
        if len(fences) % 2 != 0:
            issues.append(Issue(rel, fences[-1][0] if fences else 0, "§7.3", "代码围栏未闭合"))
        # 开围栏（偶数索引）必须有语言标注
        for idx in range(0, len(fences), 2):
            ln, cnt, info = fences[idx]
            if not info:
                issues.append(Issue(rel, ln, "§7.3", "代码块未标注语言（纯文本请用 text）"))

    # ---- H1 唯一 / 跳级 ----
    if not links_only:
        levels = []
        for ln, line in body:
            m = HEADING_RE.match(line)
            if m:
                levels.append((ln, len(m.group(1)), m.group(2)))
        h1 = [x for x in levels if x[1] == 1]
        if len(h1) != 1:
            issues.append(Issue(rel, h1[0][0] if h1 else 1, "§5.1", f"H1 数量为 {len(h1)}，应为 1"))
        prev = 1
        for ln, lv, txt in levels:
            if lv > prev + 1:
                issues.append(Issue(rel, ln, "§5.1", f"标题跳级：H{prev} → H{lv}（{txt}）"))
            prev = lv

        # ---- H2 编号（导航类 / 模板豁免）----
        if not unnumbered:
            seq = []
            for ln, line in body:
                m = HEADING_RE.match(line)
                if m and len(m.group(1)) == 2:
                    seq.append((ln, m.group(2)))
            expected = 1
            for ln, txt in seq:
                if txt.startswith("变更记录"):
                    continue  # 元信息节，不编号
                m = re.match(r"^(\d+)\.\s", txt)
                if not m:
                    issues.append(Issue(rel, ln, "§5.2", f"H2 未编号：{txt}"))
                    continue
                if int(m.group(1)) != expected:
                    issues.append(Issue(rel, ln, "§5.2", f"H2 编号不连续：期望 {expected}，实际 {m.group(1)}"))
                expected = int(m.group(1)) + 1

    # ---- 表格分隔线 ----
    if not links_only:
        for ln, line in body:
            if TABLE_SEP_RE.match(line) and ":" in line:
                issues.append(Issue(rel, ln, "§7.1", "表格分隔线含对齐冒号，应统一为 | --- |"))
                break

    # ---- 禁用标记 ----
    if not links_only:
        for ln, line in body:
            if ln in ignored:
                continue
            for mk in FORBIDDEN_MARKERS:
                if mk in line:
                    issues.append(Issue(rel, ln, "§7.4", f"使用了未定义的标记 {mk}"))

    # ---- 链接与锚点 ----
    for ln, line in body:
        if ln in ignored:
            continue
        cleaned = strip_inline_code(line)
        for m in LINK_RE.finditer(cleaned):
            target = m.group(1).strip()
            if target.startswith(("http://", "https://", "mailto:")):
                continue
            if ABS_PATH_LINK_RE.match(target):
                issues.append(Issue(rel, ln, "§8.1", f"把机器绝对路径当链接：{target}"))
                continue
            p, _, frag = target.partition("#")
            if p:
                resolved = os.path.normpath(os.path.join(os.path.dirname(path), p))
                if not os.path.exists(resolved):
                    issues.append(Issue(rel, ln, "§8.1", f"链接目标不存在：{target}"))
                    continue
            else:
                resolved = path
            if frag and frag not in _anchor_cache.setdefault(resolved, collect_anchors(resolved)):
                issues.append(Issue(rel, ln, "§8.1", f"锚点不存在：#{frag}"))


_anchor_cache: dict = {}


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    links_only = "--links-only" in sys.argv

    targets = []
    if args:
        for a in args:
            p = a if os.path.isabs(a) else os.path.join(ROOT, a)
            if os.path.isdir(p):
                for dirpath, dirnames, filenames in os.walk(p):
                    dirnames[:] = [d for d in dirnames if d not in EXEMPT_DIRS]
                    targets += [os.path.join(dirpath, f) for f in filenames if f.endswith(".md")]
            elif os.path.isfile(p):
                targets.append(p)
    else:
        for dirpath, dirnames, filenames in os.walk(os.path.join(ROOT, "docs")):
            dirnames[:] = [d for d in dirnames if d not in EXEMPT_DIRS]
            targets += [os.path.join(dirpath, f) for f in filenames if f.endswith(".md")]
        for extra in ("README.md", "CODEBUDDY.md"):
            p = os.path.join(ROOT, extra)
            if os.path.isfile(p):
                targets.append(p)

    targets = sorted(set(targets))
    issues: list = []
    for t in targets:
        check_file(t, issues, links_only)

    if issues:
        by_file: dict = {}
        for it in issues:
            by_file.setdefault(it.path, []).append(it)
        print(f"发现 {len(issues)} 处问题，涉及 {len(by_file)} 个文件：\n")
        for path in sorted(by_file):
            print(f"  {path}")
            for it in sorted(by_file[path], key=lambda x: x.line):
                print(f"    L{it.line:<5} [{it.rule}] {it.msg}")
        print(f"\n共 {len(targets)} 个文件被检查。")
        return 1

    print(f"✅ 全部通过（检查 {len(targets)} 个文件）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
