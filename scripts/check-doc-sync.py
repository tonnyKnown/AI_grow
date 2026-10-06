#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""迭代文档同步检查器

对比指定 git 区间内「代码改动」与「文档改动」，按路径规则映射，
输出疑似漏改的文档。用于迭代收尾的一致性检查（迭代流程的 G3 门禁）。

用法:
    python scripts/check-doc-sync.py --from v1.0.0                # 检查 v1.0.0..HEAD
    python scripts/check-doc-sync.py --from v1.0.0 --to v1.1.0    # 指定区间
    python scripts/check-doc-sync.py --from v1.0.0 --all          # 附加弱规则提示
    python scripts/check-doc-sync.py --from v1.0.0 --list         # 只列出改动文件
    python scripts/check-doc-sync.py --from v1.0.0 --warn-only    # 始终返回退出码 0

未指定 --from 时，自动取最近的 Git 标签作为起点。

退出码: 0 = 无强规则告警；1 = 存在强规则告警。

规则来源: docs/06-规范/迭代与版本规划规范.md §10.1 文档清算
"""
from __future__ import annotations

import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 文档改动统计范围
DOC_PREFIX = "docs/"
# 不参与"代码改动"统计的目录（工具与元数据）
NON_CODE_PREFIXES = ("docs/", ".github/", "scripts/", ".workbuddy/", ".trae/")


class Rule:
    """一条"代码路径 → 期望同步的文档"映射规则。"""

    __slots__ = ("key", "desc", "patterns", "doc", "level", "hint")

    def __init__(self, key, desc, patterns, doc, level, hint):
        self.key = key
        self.desc = desc
        self.patterns = [re.compile(p) for p in patterns]
        self.doc = doc
        self.level = level  # strong = 默认告警；weak = 需 --all
        self.hint = hint

    def match(self, path):
        return any(p.search(path) for p in self.patterns)


RULES = [
    Rule(
        "api",
        "接口相关代码",
        [r"(^|/)controller/", r"Controller\.java$"],
        "docs/03-接口/API-接口文档.md",
        "strong",
        "新增或修改接口必须同步 API 接口文档（路径、参数、响应示例）",
    ),
    Rule(
        "db",
        "数据模型 / SQL 脚本",
        [r"(^|/)entity/", r"(^|/)mapper/.*\.xml$", r"(^|/)entity/.*\.java$", r"^sql/", r"/sql/"],
        "docs/04-数据库/数据库设计文档.md",
        "strong",
        "表结构或字段变化必须同步数据库设计文档，并核对 sql/ 脚本",
    ),
    Rule(
        "ops",
        "部署与运行配置",
        [
            r"application[\w-]*\.(ya?ml|properties)$",
            r"(^|/)Dockerfile$",
            r"docker-compose[\w.-]*\.ya?ml$",
            r"\.env(\.[\w.-]+)?$",
        ],
        "docs/05-运维/部署运维手册.md",
        "strong",
        "端口、环境变量、依赖或部署方式变化必须同步部署运维手册",
    ),
    Rule(
        "frontend",
        "前端路由与页面",
        [r"^oa-frontend/src/(router|views)/"],
        "docs/03-接口/API-接口文档.md",
        "weak",
        "新增页面需确认其调用的接口已在 API 文档中登记",
    ),
    Rule(
        "service",
        "服务层实现",
        [r"(^|/)service/"],
        "docs/02-技术/TRD-技术设计文档.md",
        "weak",
        "涉及架构调整或技术方案变化时需更新 TRD",
    ),
]


def setup_stdout():
    """在 GBK 控制台下避免中文输出抛 UnicodeEncodeError。"""
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            try:
                stream.reconfigure(encoding="utf-8", errors="replace")
            except Exception:
                pass


def run_git(*args):
    """执行 git 命令，返回 (returncode, stdout, stderr)。"""
    cmd = ["git", "-c", "core.quotepath=false"] + list(args)
    proc = subprocess.run(cmd, cwd=ROOT, capture_output=True, text=True)
    return proc.returncode, proc.stdout, proc.stderr


def latest_tag():
    code, out, _ = run_git("describe", "--tags", "--abbrev=0")
    return out.strip() if code == 0 and out.strip() else None


def changed_files(from_ref, to_ref):
    code, out, err = run_git("diff", "--name-only", f"{from_ref}..{to_ref}")
    if code != 0:
        raise RuntimeError(err.strip() or f"无法比较 {from_ref}..{to_ref}")
    return [line.strip() for line in out.splitlines() if line.strip()]


def split_files(changed):
    """返回 (code_files, doc_files)。"""
    doc_files = [f for f in changed if f.startswith(DOC_PREFIX)]
    code_files = [
        f
        for f in changed
        if not f.startswith(NON_CODE_PREFIXES) and not f.endswith(".md")
    ]
    return code_files, doc_files


def parse_args(argv):
    opts = {"from": None, "to": "HEAD", "all": False, "list": False, "warn_only": False}
    i = 0
    while i < len(argv):
        a = argv[i]
        if a == "--from" and i + 1 < len(argv):
            opts["from"] = argv[i + 1]
            i += 2
        elif a.startswith("--from="):
            opts["from"] = a.split("=", 1)[1]
            i += 1
        elif a == "--to" and i + 1 < len(argv):
            opts["to"] = argv[i + 1]
            i += 2
        elif a.startswith("--to="):
            opts["to"] = a.split("=", 1)[1]
            i += 1
        elif a == "--all":
            opts["all"] = True
            i += 1
        elif a == "--list":
            opts["list"] = True
            i += 1
        elif a == "--warn-only":
            opts["warn_only"] = True
            i += 1
        elif a in ("-h", "--help"):
            print(__doc__)
            sys.exit(0)
        else:
            print(f"未知参数：{a}\n")
            print(__doc__)
            sys.exit(2)
    return opts


def main() -> int:
    setup_stdout()
    opts = parse_args(sys.argv[1:])

    from_ref = opts["from"] or latest_tag()
    if not from_ref:
        print("❌ 未指定 --from，且仓库中没有可用的 Git 标签。")
        print("   请显式指定起点，例如：python scripts/check-doc-sync.py --from v1.0.0")
        return 2

    to_ref = opts["to"]
    try:
        changed = changed_files(from_ref, to_ref)
    except RuntimeError as exc:
        print(f"❌ Git 命令失败：{exc}")
        return 2

    code_files, doc_files = split_files(changed)

    print(f"迭代文档同步检查  [{from_ref}..{to_ref}]")
    print("-" * 56)
    print(f"代码改动 {len(code_files)} 个文件，文档改动 {len(doc_files)} 个文件")

    if opts["list"]:
        print("\n代码改动：")
        for f in code_files:
            print(f"  {f}")
        print("\n文档改动：")
        for f in doc_files:
            print(f"  {f}")
        return 0

    rules = [r for r in RULES if opts["all"] or r.level == "strong"]
    alerts, passed = [], []

    for rule in rules:
        hits = [f for f in code_files if rule.match(f)]
        if not hits:
            passed.append((rule, "无相关改动"))
            continue
        if rule.doc in doc_files:
            passed.append((rule, f"已同步（{rule.doc}）"))
            continue
        alerts.append((rule, hits))

    if alerts:
        for rule, hits in alerts:
            print(f"\n[告警] {rule.desc}有改动，但 {rule.doc} 未更新")
            print("       命中文件：")
            for f in hits[:8]:
                print(f"         {f}")
            if len(hits) > 8:
                print(f"         ...（其余 {len(hits) - 8} 个）")
            print(f"       期望更新：{rule.doc}")
            print(f"       依据：{rule.hint}")

    for rule, note in passed:
        if note != "无相关改动":
            print(f"\n[通过] {rule.desc}：{note}")

    print("\n" + "-" * 56)
    if alerts:
        print(f"共 {len(alerts)} 条告警，{len(passed)} 项通过。")
        print("确认属于「无需改文档」时，请在对应 Issue 或迭代计划中注明理由。")
        return 0 if opts["warn_only"] else 1
    print("✅ 未发现疑似漏改的文档。")
    print("⚠️ 检查器只覆盖路径层面，无法判断文档内容是否写对。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
