#!/usr/bin/env bash
# Compile lcm-kotoba to wasm (kotoba CLI 0.7.2) and run fixture main.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="${ROOT}/lcm.kotoba"
OUT="${ROOT}/lcm.wasm"
KOTOBA_BIN="${KOTOBA:-kotoba}"

if ! command -v "${KOTOBA_BIN}" >/dev/null 2>&1; then
  echo "kotoba CLI not found (set KOTOBA= or install kotoba 0.7.2)" >&2
  exit 2
fi

if ! command -v node >/dev/null 2>&1; then
  echo "node is required to instantiate the import-free wasm fixture module" >&2
  exit 2
fi

"${KOTOBA_BIN}" compile "${SRC}" --target wasm --output "${OUT}" --json
test -s "${OUT}"

export LCM_KOTOBA_WASM="${OUT}"
node --input-type=module <<'JS'
import { readFileSync } from "node:fs";

const wasmPath = process.env.LCM_KOTOBA_WASM;
const bytes = readFileSync(wasmPath);
const { instance } = await WebAssembly.instantiate(bytes);
if (typeof instance.exports.main !== "function") {
  console.error("wasm module does not export main");
  process.exit(3);
}
const code = Number(instance.exports.main());
if (code !== 0) {
  console.error(`lcm-kotoba fixtures failed, case ${code}`);
  process.exit(code);
}
console.log("lcm-kotoba fixtures passed");
JS
