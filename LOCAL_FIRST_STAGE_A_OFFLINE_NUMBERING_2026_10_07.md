# SANRO Android — Local-First Stage A Offline Numbering Strategy

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Progress: **10% PLANNED**
Scope: design documentation only. No Android source, Hosting frontend/backend, or database mutation.

## Purpose

Define a safe visible-document numbering direction for offline/local-first operation without changing existing WORK/LOCK runtime behavior.

Technical UUID remains the authoritative identity for sync/idempotency. Visible business numbers remain separate and must never be used as the only global identity.

This document must follow the latest canonical Hybrid authority rules in `SANRO_DEVELOPMENT_RULES.md`.

## Protected current numbering evidence

Latest verified protected behavior takes priority over older docs:

- Sales Order / Pesanan Penjualan R125 WORK: `SO-YYYYMMDD-###`, sequence per business date, legacy SO retained.
- Purchase Order / Pesanan Pembelian R115 WORK: `PO-YYYYMMDD-NNN`, sequence per business date, legacy/random/old-format PO retained, edit keeps original No. PO.

Do not silently renumber already committed/printed documents during sync, restore, upgrade, or mode change.

Exact active formats for final Penjualan/Pembelian transaction numbers remain provisional until confirmed from latest Hosting LIVE source/runtime and READ-ONLY runtime audit.

## Numbering authority follows transaction authority

There must be only **one allocator authority** for one document type/scope at a time.

Do not let local Room and cloud independently generate the same business-number scope.

### Free / Starter

Current canonical rule:

- single device;
- operational transaction database is local on device;
- transactions must continue offline after initial online login/activation;
- there is no operational cloud transaction database for Free/Starter.

Therefore operational documents that belong to the local transaction database are allocated by the authoritative local device inside Room.

### Professional and above — Multi Device Hybrid ON

Current canonical rule:

- one Perangkat Utama maximum per branch;
- Perangkat Utama is authoritative local operational DB and the only operational transaction sync gateway;
- Penjualan Mode Kasir must go through Perangkat Utama;
- Perangkat Kasir must not sync Penjualan directly to cloud;
- Pembelian and Back Office may remain direct-cloud according to the canonical architecture.

Therefore numbering authority is route-specific:

- locally authoritative Penjualan/POS documents -> allocator owned by Perangkat Utama;
- direct-cloud Back Office documents -> allocator owned by cloud/server;
- Perangkat Kasir never independently allocates a branch business number;
- a local mirror of a cloud-owned document/sequence is read-only for numbering and must not become a second allocator.

The future Hybrid owner of SO generation must be decided explicitly before implementation. Current R125 production behavior is server-side and protected; this audit does not silently move it to Room.

PO/Pembelian must not be moved to local numbering merely because Room exists. Current Professional Back Office direction allows direct-cloud Pembelian, so server ownership can remain authoritative for that route.

### Tunas Baru current LIVE runtime

Tunas Baru remains **Direct Cloud / Multi Device Hybrid DISABLED** until an explicit controlled rollout decision.

Existing server-side numbering remains authoritative. This Stage A document must not reroute Tunas Baru numbering to local Room.

## Why generic MAX()+1 is forbidden

A local-first system must not allocate business numbers by unconstrained `MAX(number)+1` across independent authorities because retries, restores, concurrency, device replacement, and offline operation can create duplicates.

Number allocation must be transactional and scoped inside the current authoritative store.

## Proposed Room entity: document_sequence

For document types whose allocator authority is local Room, conceptual fields are:

- `tenant_id`
- `branch_id` / branch scope when applicable
- `document_type`
- `business_date`
- `last_sequence`
- `updated_at`
- `revision`

Candidate unique key:

`(tenant_id, branch_id, document_type, business_date)`

This remains a design proposal until Stage B implementation review.

Cloud-owned document types do not use this local sequence row as an allocator.

## Local allocation rule

For a locally owned transaction commit:

1. Begin one Room transaction.
2. Resolve tenant-local `business_date` from the approved time policy.
3. Read/update the `document_sequence` row in the same transactional boundary.
4. Increment sequence atomically.
5. Build visible document number using the protected format for that document type.
6. Persist header + detail + inventory/payment/point effects + sequence update + outbox in the same Room transaction.
7. Commit once.

If the business transaction rolls back, no partial business mutation may remain.

Sequence-gap behavior must be explicit before implementation. A harmless gap is preferable to duplicate identity or silent renumber of a document that has already reached UI/print/audit.

## Perangkat Kasir rule

When Multi Device Hybrid is ON:

- Kasir sends the operation to Perangkat Utama over Local API;
- Perangkat Utama owns the atomic commit and number allocation for locally authoritative POS documents;
- Kasir does not reserve a final branch number independently;
- Kasir does not direct-sync the transaction to cloud.

Behavior when a Kasir cannot reach Perangkat Utama is NOT finalized here and must not be invented during Stage B.

## UUID vs visible number

Every syncable transaction should carry at least:

- immutable technical `uuid` generated before/at authoritative commit;
- visible business number where applicable;
- tenant scope;
- branch scope where applicable;
- originating device/user metadata;
- revision/sync metadata;
- outbox event UUID/idempotency key for locally synced operations.

Cloud idempotency is based on technical identity/idempotency key, not on trusting the visible number alone.

## Conflict rule

If the receiving authority already contains the same visible number for a different UUID in the same uniqueness scope:

- do not overwrite;
- do not silently renumber a committed/printed transaction;
- mark sync/reconciliation as `CONFLICT`;
- preserve local and remote evidence;
- fail closed until deterministic reconciliation applies.

Same UUID + same payload hash = replay-safe and must return the same logical result, not duplicate.

Same idempotency key + different payload hash = conflict/fail closed.

## Clock / business-date safety

Daily sequence reset makes date handling part of transaction correctness.

Stage B must not depend blindly on a mutable wall clock. At minimum preserve:

- UTC event timestamp;
- tenant-local `business_date`;
- timezone configuration;
- device-time anomaly evidence when detected.

Exact anti-clock-tamper behavior remains open before public release.

## Restore and device replacement

For locally authoritative sequence scopes, backup/recovery must include:

- transactions;
- `document_sequence` state;
- outbox/sync state;
- device/tenant identity metadata needed for safe recovery.

Replacement activation must reconcile authoritative history before issuing new numbers when another authoritative history exists.

Perangkat Utama replacement is a controlled transfer/recovery flow, not ordinary second-device activation.

## Stage B implementation gate

Do NOT implement this into Room until all of the following are available:

1. complete buildable Android V1.0.5 WORK source matching the tested APK lineage;
2. READ-ONLY capture of latest Hosting LIVE schema/runtime relevant to transaction numbering;
3. verified current generators and ownership for final Penjualan/Pembelian transaction numbers, not only SO/PO;
4. per-document authority matrix for Free/Starter, Professional Hybrid, and Direct Cloud modes;
5. explicit decision for sequence-gap handling and Kasir behavior when Perangkat Utama is unreachable;
6. full Android checkpoint + rollback before source changes.

## Rollback impact of this documentation stage

Frontend rollback: NO
Backend rollback: NO
Database rollback: NO
Android rollback: NO
Restart Node.js: NO
