# SANRO Android — Local-First Stage A Canonical Model Audit

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Progress: **10% PLANNED**
Scope: documentation/audit only; no Android source, Hosting frontend/backend, or database mutation.

## Source-of-truth rule

Physical MariaDB mapping remains provisional until captured READ-ONLY from the latest Hosting LIVE database/runtime. Hosting LIVE + latest user verification outrank this document, older Git files, and GAS history.

Do not use GAS / Google Sheet mirror design as the Android database model.

## Architecture direction

SANRO POS Web/PWA -> Native Bridge -> Domain/Repository -> Room -> SQLite local Android -> Sync Engine -> SANRO Cloud API -> MariaDB Hosting.

Room/SQLite is the Android operational store. IndexedDB is only browser/PWA fallback/cache, not the Android primary database.

Local-first does not mean local-only. Offline operations commit locally and sync when online.

## Canonical semantic entities — draft v0.1

- `items` <- Master Barang
- `contacts` <- Kontak
- `sales`
- `sale_details`
- `purchases`
- `purchase_details`
- `returns`
- `return_details`
- `payments`
- `payment_allocations`
- `receivable_adjustments`
- `payable_adjustments`
- `inventory_movements`
- `inventory_layers`
- `stock_opname`
- `item_units`
- `price_tiers`
- `point_ledger`
- `point_balance_snapshot` (derived/cache only where useful)
- `settings` (offline-safe subset only)
- `access_policy_snapshot`
- `sync_queue`
- `sync_log`
- `device_state`

## Identity and sync metadata

Every syncable entity should use a stable technical identity independent of visible document numbers and physical device rows:

- `uuid`
- `tenant_id`
- `device_id_created`
- `server_id` optional
- `legacy_key` / `source_row` optional
- `created_at`
- `updated_at`
- `revision`
- `sync_status`
- `last_synced_at`
- `deleted_at` / tombstone

Do not use `source_row_idx` or `device_id` as global business identity.

Visible `transaction_no` / `po_no` remains separate from UUID.

## Sales rules that Room must preserve

The protected Penjualan baseline includes:

- direct add/scan with qty default 1;
- duplicate item adds qty instead of adding another row;
- Hold and Pesanan rules;
- Retail may Hold but Pesanan requires registered customer;
- LUNAS sales are not cancelled from Penjualan; correction uses Retur;
- Tempo sales may only be cancelled when no payment has ever been applied;
- cancelled Tempo reverses/purges pending Point Reward according to protected runtime behavior;
- cancelled sales remain audit history but must not be active receivables or active sales;
- legacy FIFO relations must never be guessed.

## Purchases rules that Room must preserve

- Lunas is the default settlement.
- Tempo has no payment method at initial save and default due date = purchase date + 45 days.
- valid transaction purchase price becomes the latest Master Barang purchase price.
- new purchases create real current FIFO layers even when historical FIFO gaps exist; never fabricate historical FIFO layers.
- current protected PO format is `PO-YYYYMMDD-NNN`, sequence resets by transaction date; legacy/random/old-format PO remains readable; edit retains original PO number.

## Payment model — important Stage A decision

Do not model payment as only a mutable "paid amount" field on a sale/purchase.

`payments` represents one real cash/bank receipt or disbursement.

`payment_allocations` represents allocation of one payment to one or many documents.

For receivables:

1. allocate to positive adjustment/reconciliation balance first;
2. then to oldest real sales note;
3. continue FIFO across notes until payment is exhausted.

For payables the same concept applies per supplier.

One payment may allocate to multiple invoices while still being one cash/bank movement.

Positive/negative receivable/payable adjustments are not fake payments and must not fabricate cashflow rows. Negative adjustment changes effective balance virtually; it must not mark a sale/purchase LUNAS merely because the effective balance is covered.

Current Hosting operational payment history uses `gs_cicilan`. `gs_riwayat_cicilan` is legacy/parked and must not become the Room source model.

## Inventory/FIFO model

Use a movement ledger, not only current stock balance.

`inventory_movements` should support at least:

- PURCHASE
- SALE
- SALES_RETURN
- PURCHASE_RETURN
- STOCK_OPNAME
- ADJUSTMENT
- future TRANSFER

Each movement should preserve source transaction/detail UUID, item UUID, qty delta, stock before/after where authoritative, cost/HPP references, device, timestamps, revision, and sync status.

`inventory_layers` keeps FIFO acquisition layers and remaining quantity.

Current/future FIFO rules:

- current and future flows must be deterministic, transaction-safe, idempotent, rollback-safe, and multi-device safe;
- sales may split consumption across multiple FIFO layers;
- Owner, supplier tax origin, unit conversion, cost/HPP must come from proven authoritative relations;
- LEGACY/UNVERIFIED relationships fail closed and are not guessed;
- cancellation/return must not contaminate active cost/Owner/tax reporting.

## Point Reward model

Point Reward requires its own ledger; do not derive the entire state only from current balance.

`point_ledger` entries must distinguish at least:

- sale/event-earned point;
- Tempo pending point;
- activation/settlement of pending point;
- reversal from eligible sale cancellation/return;
- redemption/use where applicable;
- MANUAL ADD;
- MANUAL SUBTRACT.

Manual subtraction must not create negative point balance. Manual adjustment must preserve operator, timestamp, reason, before/after, stable reference/idempotency key, and must not fabricate Penjualan/Retur transactions.

## Atomic local transaction boundary

When local-first sale write is activated, one Room transaction should atomically commit:

- Sale
- SaleDetails
- Payment (when immediate settlement exists)
- PaymentAllocations
- InventoryMovements
- FIFO layer updates/consumption
- Point ledger mutation/pending entry when applicable
- SyncQueue outbox event(s)

No partial commit is acceptable.

Do not dual-write the Web UI directly to cloud while Native Bridge separately mirrors the same transaction into Room. When local-first is activated, preferred ownership is: Room commit -> transactional outbox -> Sync Engine -> Cloud API.

## Transactional outbox

Draft `sync_queue` fields:

- `uuid`
- `tenant_id`
- `entity_type`
- `entity_uuid`
- `operation`
- `payload_version`
- `payload_hash`
- `base_revision`
- `status` = PENDING / SYNCING / SYNCED / FAILED / CONFLICT
- retry counter
- created/updated/synced timestamps
- error information

Queue/event UUID is the cloud idempotency key. Same key + same payload hash must replay the same result and never duplicate mutation.

## Conflict policy

No blind last-write-wins for financial/inventory transactions.

- Master data: revision-based conflict handling.
- Committed financial transactions: event/reversal model rather than destructive overwrite.
- Inventory: movement sync + reconciliation.
- Return/payment: stable UUID + source refs + idempotency.
- Important conflicts must remain auditable.

## Numeric model — proposal, not LOCK

Do not use floating point for core money/qty.

Candidate model:

- Money: fixed-point `Long` minor units / defined decimal scale.
- Quantity: fixed-scale `Long` (candidate scale 1e6 for fractional units).
- Percent: fixed-scale integer / basis points where suitable.
- Time: UTC epoch millis plus separate business date where business-day semantics matter.

Final scale remains pending exact LIVE schema/runtime audit.

## Backup/restore requirements

Backup must be a transactionally consistent Room/SQLite snapshot, not a raw arbitrary copy while writers are active.

Required sequence concept:

1. gate/pause writers;
2. drain active transaction;
3. controlled checkpoint/close or supported safe snapshot mechanism;
4. snapshot database;
5. checksum;
6. encrypt;
7. validate encrypted artifact and metadata;
8. reopen writers;
9. upload to user-owned Google Drive when backup integration is ready.

Do not implement encryption until cross-device key recovery is designed. Android Keystore alone is insufficient for lost-device replacement.

Room migrations must be explicit/versioned. `fallbackToDestructiveMigration()` is forbidden for the transaction database.

## Device replacement

Identity must be independent of hardware device ID.

Free/Starter replacement target:

install -> login -> restore/replace device -> Admin verification -> restore validated encrypted backup -> revoke old device -> activate new slot.

Professional+ target:

login -> verify -> register new device -> initial sync cloud -> populate Room -> replace/revoke old slot according to plan.

## Android Studio readiness gate

Do not start Room/Bluetooth implementation from an incomplete V1.0.5 project snapshot.

Current GitHub `WORK_V1_0_5` is a protected source patch/snapshot but is not a complete Android Studio project. Before source implementation:

1. obtain the FULL V1.0.5 source/project that produced the user-tested WORK APK;
2. verify Manifest, root Gradle/settings, wrapper, resources, signing-neutral project structure, app modules and package IDs;
3. create a complete Android rollback/checkpoint;
4. build V1.0.5 unchanged first;
5. install over existing V1.0.5 and run regression smoke;
6. only then create the next implementation stage.

Protected Android behavior to preserve includes WebView/session, camera/scan, upload/file chooser, download handling, safe browsing, SSL guard, external URL handling, Android PrintManager, safe-area handling, launcher identity, Back behavior, and orientation policy: phone <600dp portrait; tablet >=600dp follows Android/user orientation.

## Remaining Stage A blockers

1. Exact LIVE Hosting schema capture via `information_schema` + `SHOW CREATE TABLE` for target runtime tables; READ-ONLY only.
2. FULL buildable Android V1.0.5 source/project matching the tested APK.
3. Final offline visible transaction-number scheme must be audited before local multi-device support. UUID is identity, but visible No Transaksi/Nota still requires a separate deterministic strategy.

## Rollback/deployment status for this document

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE

This document does not authorize production migration or Room implementation yet.
