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

sexual = [x for x in data if x.get("sexual", False)]
assert all(x["type"] == "ACTION" for x in sexual)
assert all(x["slot"] == 1 for x in sexual)
assert all(x.get("sexRelated", False) for x in sexual)

allowed_practices = {"CARESSES_INTIMES", "MASTURBATION", "MASTURBATION_MUTUELLE", "SEXE_ORAL", "PENETRATION", "CHOICE"}
assert all(x.get("sexualPractice") in allowed_practices for x in sexual)

tail_starts = {"SENSUEL": 10, "TORRIDE": 9, "VERY_HOT": 8}
for intensity, start_level in tail_starts.items():
    for level in range(1, 11):
        for slot in range(1, 4):
            exact = [
                x for x in data
                if x["level"] == level and x["slot"] == slot and x["intensity"] == intensity
            ]
            if level >= start_level:
                assert any(x.get("sexRelated", False) for x in exact), (
                    f"Missing sex-related {intensity} variant at level {level}, slot {slot}"
                )
            elif level >= 8:
                assert any(not x.get("sexRelated", False) for x in exact) or not exact, (
                    f"All {intensity} variants became sexual too early at level {level}, slot {slot}"
                )

print("OK: 150 challenges, 30 positions, 5 variants per position")
print("Intensities:", Counter(x["intensity"] for x in data))
print("Sexual actions:", len(sexual))
print("Sex-related prompts:", sum(bool(x.get("sexRelated", False)) for x in data))
