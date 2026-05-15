#!/usr/bin/env bash
# Build an importable zip whose top level is hj-payment-skills/SKILL.md.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PACKAGE_NAME="hj-payment-skills"
OUT_DIR="$REPO_ROOT/dist"
OUT_ZIP="$OUT_DIR/${PACKAGE_NAME}-importable.zip"

if [ ! -f "$REPO_ROOT/SKILL.md" ]; then
  echo "ERROR: missing root SKILL.md. Importable zip requires ${PACKAGE_NAME}/SKILL.md" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"
rm -f "$OUT_ZIP"

(
  cd "$REPO_ROOT/.."
  zip -r "$OUT_ZIP" "$PACKAGE_NAME" \
    -x "$PACKAGE_NAME/.git/*" \
    -x "$PACKAGE_NAME/dist/*" \
    -x "$PACKAGE_NAME/**/*.class" \
    -x "$PACKAGE_NAME/.DS_Store" \
    -x "*/.DS_Store"
)

echo "$OUT_ZIP"
