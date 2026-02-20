#!/usr/bin/env python3
"""
Run JPF symbolic execution via ./tools/run-jpf.sh.

  python run_jpf.py --help       List available .jpf configs
  python run_jpf.py <config>     Run JPF with the given config (e.g. AccountTestSymbolic.jpf)
  python run_jpf.py <config> --  Pass extra args to JPF

Examples:
  python run_jpf.py AccountTestSymbolic.jpf
  python run_jpf.py MathTestSymbolic.jpf
"""

import argparse
import os
import re
import subprocess
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
JPF_DIR = os.path.join(SCRIPT_DIR, "src", "test", "resources", "jpf")
RUN_JPF = os.path.join(SCRIPT_DIR, "tools", "run-jpf.sh")

# Fallback descriptions (used if first comment line can't be parsed)
DEFAULT_DESCRIPTIONS = {
    "AccountTest.jpf": "Account.debit() – concrete (no symbolic path conditions)",
    "AccountTestSymbolic.jpf": "Account.debit() – symbolic → value > this.balance",
    "AccountTestViolation.jpf": "Account.debit() – exception propagates (property violation)",
    "MathTestSymbolic.jpf": "Math.sqrt(value) – symbolic → value <= 0",
    "MathSumTestSymbolic.jpf": "Math.sum(a, b) – symbolic → a + b < 0",
    "MathConjunctionTestSymbolic.jpf": "Math.requireBothNonNegative(a,b) – single if with &&",
    "MathNestedTestSymbolic.jpf": "Math.requireBothNonNegativeNested(a,b) – nested ifs",
    "StringUtilsTestSymbolic.jpf": "StringUtils.requireEmpty(argument) – string symbolic",
    "StringUtilsLengthTestSymbolic.jpf": "StringUtils.requireLengthZero(length) – int-based",
}


def list_jpf_configs():
    """Discover .jpf files and extract descriptions from first comment."""
    configs = []
    if not os.path.isdir(JPF_DIR):
        return configs

    for name in sorted(os.listdir(JPF_DIR)):
        if not name.endswith(".jpf"):
            continue
        path = os.path.join(JPF_DIR, name)
        if not os.path.isfile(path):
            continue

        desc = DEFAULT_DESCRIPTIONS.get(name)
        if not desc:
            try:
                with open(path, "r") as f:
                    for line in f:
                        line = line.strip()
                        if line.startswith("#") and "configuration for" in line.lower():
                            # e.g. "# JPF configuration for Math.sqrt symbolic execution (jpf-symbc)"
                            m = re.search(r"configuration for\s+(.+?)(?:\s*\(|$)", line, re.I)
                            if m:
                                desc = m.group(1).strip()
                            else:
                                desc = line.lstrip("# ").strip()
                            break
                        elif line.startswith("#") and len(line) > 2:
                            desc = line.lstrip("# ").strip()
                            break
            except OSError:
                pass
        if not desc:
            desc = name

        configs.append((name, desc))

    return configs


def print_help(configs):
    max_name = max(len(c[0]) for c in configs) if configs else 20
    print("Usage: python run_jpf.py <config> [-- JPF options...]")
    print()
    print("Available JPF configs (src/test/resources/jpf/):")
    print("-" * (max_name + 50))
    for name, desc in configs:
        print(f"  {name:<{max_name}}  {desc}")
    print("-" * (max_name + 50))
    print()
    print("Examples:")
    print("  python run_jpf.py AccountTestSymbolic.jpf")
    print("  python run_jpf.py MathTestSymbolic.jpf")
    print()
    print("Prereqs: mvn test-compile")
    print("Uses: ./tools/run-jpf.sh (requires Java 8)")


def main():
    configs = list_jpf_configs()

    # Show full help with available configs for -h/--help or --list
    if "-h" in sys.argv or "--help" in sys.argv or "-l" in sys.argv or "--list" in sys.argv:
        print_help(configs)
        return 0

    parser = argparse.ArgumentParser(description="Run JPF via ./tools/run-jpf.sh")
    parser.add_argument("config", nargs="?")
    args, rest = parser.parse_known_args()

    if not args.config:
        print_help(configs)
        return 1

    config = args.config
    if not config.endswith(".jpf"):
        config += ".jpf"

    full_path = os.path.join(JPF_DIR, config)
    if not os.path.isfile(full_path):
        # Allow full path
        if os.path.isfile(config):
            full_path = os.path.abspath(config)
            config = os.path.basename(config)
        else:
            print(f"Config not found: {config}", file=sys.stderr)
            print("Use --list to see available configs.", file=sys.stderr)
            return 1

    cmd = [RUN_JPF, config] + rest
    return subprocess.call(cmd)


if __name__ == "__main__":
    sys.exit(main() or 0)
