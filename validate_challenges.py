import json
from collections import Counter
from pathlib import Path

path = Path(__file__).parent / "app/src/main/assets/challenges.json"
data = json.loads(path.read_text(encoding="utf-8"))

assert len(data) == 150, f"Expected 150 challenges, got {len(data)}"
assert len({x["id"] for x in data}) == 150, "Challenge IDs must be unique"

for level in range(1, 11):
    for slot in range(1, 4):
        cell = [x for x in data if x["level"] == level and x["slot"] == slot]
        assert len(cell) == 5, f"Level {level}, slot {slot}: expected 5 variants, got {len(cell)}"
        expected_type = "ACTION" if slot == 1 else "TRUTH"
        assert all(x["type"] == expected_type for x in cell)
        assert sum(x["intensity"] == "SENSUEL" for x in cell) >= 2

print("OK: 150 challenges, 30 positions, 5 variants per position")
print("Intensities:", Counter(x["intensity"] for x in data))