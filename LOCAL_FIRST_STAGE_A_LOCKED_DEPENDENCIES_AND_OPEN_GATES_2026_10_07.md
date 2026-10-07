# SANRO Android — Stage A Locked Dependencies & Open Gates

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Progress: **10% PLANNED**
Scope: evidence consolidation only; no Android source, Hosting frontend/backend, or database mutation.

## Purpose

Separate rules that are already protected/locked from facts that remain OPEN and must not be guessed during Room/Hybrid design.

## LOCKED dependency — Point Reward

Latest verified repository evidence records Point Reward as **LOCK BASELINE / production baseline / user-verified** at R91 / V1.10.419.

Room/local-first design must preserve, not redesign:

- existing transaction/Event reward behavior;
- Tempo pending point behavior;
- manual **Tambah Poin** and **Kurangi Poin**;
- manual input value is positive and operation direction determines add/subtract;
- manual subtraction must not produce a negative point balance;
- history/audit must distinguish `MANUAL TAMBAH` and `MANUAL KURANG`;
- correction notes remain auditable;
- manual adjustment must not fabricate Penjualan/Retur transactions;
- normal transaction Point Reward rules remain unchanged unless the LOCK is explicitly opened or a verified production bug requires correction.

The conceptual Room `point_ledger` is therefore a compatibility model for the LOCKED behavior, not permission to change Point Reward business logic.

### Physical Point Reward mapping is still OPEN

The exact latest Hosting LIVE physical table/column relationships for Point Reward are **not yet captured** in this Stage A audit.

Do not infer physical source tables from old GAS/Google mirror design, old docs, or names alone.

Final physical mapping requires current LIVE READ-ONLY schema/runtime evidence.

## Protected dependency — document numbering

Current latest verified numbering behavior that must be preserved:

- Sales Order R125 WORK: `SO-YYYYMMDD-###`, daily sequence, legacy SO remains valid.
- Purchase Order R115 WORK: `PO-YYYYMMDD-NNN`, daily sequence, legacy/random/old-format PO remains readable, edit keeps original PO.

Older documentation showing `PO-DDMMYYYY-NNN` is superseded by the later user-verified R115 WORK behavior and must not be copied into Room.

## OPEN gate — final Penjualan transaction number

The latest authoritative generator/format for the final committed Penjualan transaction/nota number has not been proven from current Hosting LIVE runtime/source in this audit.

Therefore:

- do not copy an old generator;
- do not invent a new format;
- do not use generic `MAX()+1`;
- do not assume the SO sequence is the final Penjualan transaction sequence;
- keep UUID technical identity separate from visible transaction number.

This gate remains OPEN until latest Hosting source/runtime evidence is captured.

## OPEN gate — final Pembelian transaction number

Likewise, PO numbering does not prove the final committed Pembelian transaction/nota number generator.

Do not equate PO number with final purchase transaction identity unless latest runtime evidence proves that relationship.

## OPEN gate — LIVE physical schema

Required READ-ONLY capture remains pending for current Hosting LIVE, including operational transaction, payment, inventory/FIFO, auth/access, idempotency, and Point Reward physical structures.

The prepared capture process permits only metadata/DDL reads such as:

- `SELECT`;
- `information_schema` reads;
- `SHOW CREATE TABLE`.

No migration/write statements are authorized.

## OPEN gate — full Android source

`WORK_V1_0_5` is an incremental evidence/delta snapshot, not a complete Android Studio project.

Stage B is blocked until the actual complete buildable V1.0.5 project matching the user-tested WORK APK lineage passes `FULL_SOURCE_V1_0_5_ACCEPTANCE_CHECKLIST_2026_10_07.md`.

Do not reconstruct authority from V1.0.2.

## OPEN product rule — Kasir loses Perangkat Utama connection

Professional Multi Device Hybrid defines Perangkat Utama as authoritative local operational database/gateway. Behavior when a Kasir terminal loses LAN/Wi-Fi reachability to Perangkat Utama is not finalized here.

Do not create an independent cashier-side transaction allocator/queue that would violate the one-authority rule without an explicit product decision.

## Stage B gate summary

Stage B may not begin until at minimum:

1. accepted FULL V1.0.5 buildable source baseline;
2. unchanged build + protected regression smoke PASS;
3. FULL Android checkpoint/rollback prepared;
4. latest LIVE schema/runtime physical mapping captured read-only enough for the Stage B scope;
5. no unresolved assumption is silently converted into implementation behavior.

## Rollback/deployment status

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE
