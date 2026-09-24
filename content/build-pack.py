"""Merges approved passages from content/drafts into the content pack bundled in the APK."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parent / "app" / "src" / "main" / "assets" / "passages.json"

passages = []
for batch in sorted((ROOT / "drafts").glob("batch-*.json")):
    for entry in json.loads(batch.read_text(encoding="utf-8")):
        if entry.pop("approved"):
            passages.append(entry)

ids = [passage["id"] for passage in passages]
duplicates = {id for id in ids if ids.count(id) > 1}
if duplicates:
    raise SystemExit(f"duplicate ids: {sorted(duplicates)}")

PACK.parent.mkdir(parents=True, exist_ok=True)
PACK.write_text(json.dumps(passages, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"{len(passages)} passages -> {PACK.relative_to(ROOT.parent)}")
