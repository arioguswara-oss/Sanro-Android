# SANRO Android — Local-First Stage A Offline Numbering Strategy

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Progress: **10% PLANNED**
Scope: design documentation only. No Android source, Hosting frontend/backend, or database mutation.

## Purpose

Define a safe visible-document numbering direction for offline/local-first operation without changing existing WORK/LOCK runtime behavior.

Technical UUID remains the authoritative identity for sync/idempotency. Visible business numbers remain separate and must never be used as the only global identity.

## Protected current numbering evidence

Latest verified protected behavior takes priority over older docs:

- Sales Order / Pesanan Penjualan R125 WORK: `SO-YYYYMMDD-###`, sequence per business date, legacy SO retained.
- Purchase Order / Pesanan Pembelian R115 WORK: `PO-YYYYMMDD-NNN`, sequence per business date, legacy/random/old-format PO retained, edit keeps original No. PO.

Do not silently renumber already committed/printed documents during sync or upgrade.

Exact active formats for other transaction types (for example final Penjualan and final Pembelian numbers) remain provisional until confirmed from latest Hosting LIVE source/runtime and READ-ONLY schema/runtime audit.

## Why generic MAX()+1 is forbidden

A local-first system must not allocate business numbers by unconstrained `MAX(number)+1` across devices/cloud because retries, restores, concurrent devices, and offline operation can create duplicates.

Number allocation must be transactional and scoped.

## Proposed Room entity: document_sequence

Conceptual fields:

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

## Allocation rule

For a locally owned transaction commit:

1. Begin one Room transaction.
2. Resolve tenant-local `business_date` from trusted app/device time policy.
3. Lock/read the `document_sequence` row for the required scope.
4. Increment sequence atomically.
5. Build visible document number using the protected format for that document type.
6. Persist header + detail + inventory/payment/point effects + sequence update + outbox inside the same Room transaction.
7. Commit once.

If the transaction rolls back, no partial business transaction may remain. Sequence-gap policy must be explicit before implementation; gaps are preferable to duplicate/renumber if a committed identifier has already escaped to UI/print/audit.

## Free / Starter

Free and Starter are single-device local-first in the current product direction.

Therefore the active device can be the sole local allocator for its sequence scope while offline. The cloud must accept replay by technical UUID/idempotency key and must not create a duplicate transaction on retry.

Device replacement/restore must restore sequence state together with the transaction database before the replacement device becomes authoritative.

If restore state is older than cloud state, activation must reconcile first and fail closed on a sequence conflict rather than silently issuing a duplicate visible number.

## Professional and above — Multi Device Hybrid direction

Current roadmap direction uses one **Perangkat Utama** per branch as the authoritative local operational database/gateway.

For this model:

- Perangkat Utama owns branch sequence allocation.
- Perangkat Kasir must request/commit operational transactions through the Perangkat Utama over LAN/Wi-Fi.
- Kasir terminals do not independently invent a branch business number and do not sync transactions directly to cloud.
- Cloud synchronization originates from the authoritative branch operational state/outbox.

This avoids multiple independent offline allocators for the same branch/date/document type.

Behavior when a Kasir terminal cannot reach the Perangkat Utama is NOT finalized here and must not be invented during Stage B. It requires a separate explicit product rule.

## UUID vs visible number

Every transaction must have at least:

- immutable technical `uuid` generated before/at local commit;
- visible business `transaction_no` / `po_no` / `so_no` where applicable;
- tenant scope;
- branch scope where applicable;
- originating device metadata;
- revision/sync metadata;
- outbox event UUID/idempotency key.

Cloud idempotency is based on technical identity/idempotency key, not on trusting the visible number alone.

## Conflict rule

If cloud already contains the same visible number for a different UUID in the same uniqueness scope:

- do not overwrite;
- do not silently renumber a committed/printed transaction;
- mark sync as `CONFLICT`;
- preserve both local and remote evidence;
- require deterministic reconciliation according to a later conflict policy.

Same UUID + same payload hash = replay-safe and must return the same logical result, not duplicate.

Same idempotency key + different payload hash = conflict/fail closed.

## Clock / business-date safety

Daily sequence reset means date handling is part of correctness.

Stage B must not depend blindly on a mutable wall clock. At minimum design must preserve:

- UTC event timestamp;
- tenant-local `business_date`;
- timezone configuration;
- audit of device time anomalies when detected.

Exact anti-clock-tamper behavior is still open and must be designed before public release.

## Restore and device replacement

Backup must include:

- transactions;
- `document_sequence` state;
- outbox/sync state;
- device/tenant identity metadata required for safe recovery.

Replacement activation must perform reconciliation before issuing new numbers when a cloud history exists.

## Stage B implementation gate

Do NOT implement this into Room until all of the following are available:

1. complete buildable Android V1.0.5 WORK source that matches the tested APK lineage;
2. READ-ONLY capture of latest Hosting LIVE schema/runtime relevant to transaction numbering;
3. verified current generators for final Penjualan/Pembelian transaction numbers, not only SO/PO;
4. explicit decision for sequence-gap handling and Kasir-terminal behavior when Perangkat Utama is unreachable;
5. full Android checkpoint + rollback before source changes.

## Rollback impact of this documentation stage

Frontend rollback: NO
Backend rollback: NO
Database rollback: NO
Android rollback: NO
Restart Node.js: NO
