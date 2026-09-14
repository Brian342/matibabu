#!/usr/bin/env python3
"""
Turns a pharmacist-reviewed needs_review.csv (with the
'confirmed_atc_code' column filled in) into a follow-up Flyway
migration that updates those rows to CONFIRMED.

Rows left blank in confirmed_atc_code are treated as "no ATC code
exists for this entry" and marked UNMAPPED rather than left as
NEEDS_REVIEW indefinitely.

USAGE
    python apply_review_corrections.py \
        --review reviewed_needs_review.csv \
        --keml-version "KEML 2023" \
        --out-migration V18__confirm_keml_2023_atc_mappings.sql
"""

import argparse
import csv


def sql_escape(value: str | None) -> str:
    if value is None or value == "":
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--review", required=True, help="Reviewed CSV with confirmed_atc_code filled in")
    parser.add_argument("--keml-version", required=True)
    parser.add_argument("--out-migration", required=True)
    args = parser.parse_args()

    with open(args.review, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))

    statements = []
    confirmed, unmapped = 0, 0

    for row in rows:
        keml_code = row["keml_code"].strip()
        confirmed_code = row.get("confirmed_atc_code", "").strip()

        if not keml_code:
            # Can't safely target an UPDATE without a stable key.
            continue

        if confirmed_code:
            statements.append(
                "UPDATE medicines SET "
                f"atc_code = {sql_escape(confirmed_code)}, "
                "atc_mapping_status = 'CONFIRMED', "
                f"keml_version = {sql_escape(args.keml_version)} "
                f"WHERE keml_code = {sql_escape(keml_code)};"
            )
            confirmed += 1
        else:
            statements.append(
                "UPDATE medicines SET "
                "atc_mapping_status = 'UNMAPPED' "
                f"WHERE keml_code = {sql_escape(keml_code)};"
            )
            unmapped += 1

    with open(args.out_migration, "w", encoding="utf-8") as f:
        f.write(f"-- Pharmacist-reviewed ATC corrections for {args.keml_version}.\n")
        f.write(f"-- Confirmed: {confirmed}   Left unmapped: {unmapped}\n\n")
        f.write("\n".join(statements))
        f.write("\n")

    print(f"Confirmed: {confirmed}, unmapped: {unmapped}")
    print(f"Migration written to {args.out_migration}")


if __name__ == "__main__":
    main()