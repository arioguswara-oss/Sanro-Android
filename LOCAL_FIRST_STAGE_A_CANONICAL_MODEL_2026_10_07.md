# SANRO Android — Local-First Stage A Canonical Model Audit

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Progress: **10% PLANNED**
Revision: **v0.2 mode-aware**
Scope: documentation/audit only; no Android source, Hosting frontend/backend, or database mutation.

## Source-of-truth rule

Physical MariaDB mapping remains provisional until captured READ-ONLY from the latest Hosting LIVE database/runtime. Hosting LIVE + latest user verification outrank this document, older Git files, and GAS history.

Do not use GAS / Google Sheet mirror design as the Android database model.

Canonical Hybrid authority rules in `SANRO_DEVELOPMENT_RULES.md` are mandatory for this model.

## Architecture is mode-aware

The Android shell always provides the native foundation (Native Bridge, Room/SQLite capability, device identity, local service/sync hooks), but transaction authority differs by plan/runtime mode.

### Free / Starter

- single device;
- operational transaction database is local Room/SQLite;
- initial login/activation requires internet;
- operational transactions continue offline;
- there is no operational cloud transaction database for Free/Starter;
- cloud/account services may still be used for authentication, entitlement, backup/recovery, and other explicitly supported non-operational services.

### Professional+ — Multi Device Hybrid ON

- one Perangkat Utama maximum per branch;
- Perangkat Utama Room/SQLite is authoritative local operational database for POS operations routed locally;
- Perangkat Kasir talks to Perangkat Utama through Local API/LAN/Wi-Fi;
- Perangkat Kasir does not direct-sync Penjualan transactions to cloud;
- Perangkat Utama is the only operational transaction sync gateway to SANRO Cloud for locally authoritative POS operations;
- Back Office such as Pembelian may remain direct-cloud according to canonical rules;
- cloud-origin changes affecting cashier operation must synchronize back to Perangkat Utama before becoming locally authoritative.

### Tunas Baru / Direct Cloud mode

- current LIVE Production remains direct-cloud until explicit controlled rollout;
- Multi Device Hybrid remains disabled;
- existing cloud runtime/database remains authoritative;
- introducing Room capability into APK must not silently reroute production writes.

Therefore there is no single unconditional `Room -> Cloud` write path for every mode. Transaction ownership must be selected by entitlement/runtime mode and document route.

## Canonical semantic entities — draft v0.2

The semantic model remains stable even when physical authority differs by mode:

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
- `document_sequence` (only for document types whose current allocator authority is local)
- `sync_queue` (only for operations that actually sync)
- `sync_log`
- `device_state`

A semantic entity being present does not mean Room owns writes in every mode. For direct-cloud routes, Room may contain synchronized/cache/read models only, or may not persist that entity locally until required.

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
- `sync_status` where synchronization exists
- `last_synced_at` where synchronization exists
- `deleted_at` / tombstone where applicable

Do not use `source_row_idx` or `device_id` as global business identity.

Visible `transaction_no`, `so_no`, `po_no`, or nota numbers remain separate from UUID.

## Sales rules that local POS mode must preserve

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

Current SO numbering evidence is protected separately in the Stage A numbering strategy. Moving the SO generator from server to Room in future Hybrid mode requires an explicit implementation decision; this audit does not silently change R125 ownership.

## Purchases rules — semantic parity, authority depends mode

Business behavior to preserve:

- Lunas is default settlement;
- Tempo has no payment method at initial save and default due date = purchase date + 45 days;
- valid transaction purchase price becomes latest Master Barang purchase price;
- new purchases create real current FIFO layers even when historical FIFO gaps exist; never fabricate historical FIFO layers;
- protected PO format is `PO-YYYYMMDD-NNN`, daily sequence; legacy/random/old-format PO remains readable; edit retains original PO number.

Authority is mode-specific:

- Free/Starter local operational mode may require local purchase persistence because there is no operational cloud transaction DB;
- Professional+ Back Office Pembelian may remain direct-cloud;
- Tunas Baru remains direct-cloud until controlled rollout;
- local Room must never become a second independent writer for a cloud-owned purchase route.

Exact Free/Starter purchase UI/write routing still requires explicit implementation mapping before Stage B reaches transaction writes.

## Payment model — Stage A decision

Do not model payment as only a mutable `paid amount` field on a sale/purchase.

`payments` represents one real cash/bank receipt or disbursement.

`payment_allocations` represents allocation of one payment to one or many documents.

For receivables:

1. allocate to positive adjustment/reconciliation balance first;
2. then oldest real sales note;
3. continue FIFO across notes until payment is exhausted.

For payables the same concept applies per supplier.

One payment may allocate to multiple invoices while still being one cash/bank movement.

Positive/negative receivable/payable adjustments are not fake payments and must not fabricate cashflow rows. Negative adjustment changes effective balance virtually; it must not mark a sale/purchase LUNAS merely because effective balance is covered.

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

Each movement should preserve source transaction/detail UUID, item UUID, qty delta, stock before/after where authoritative, cost/HPP references, device, timestamps, revision, and sync state where relevant.

`inventory_layers` keeps FIFO acquisition layers and remaining quantity.

Current/future FIFO rules:

- deterministic, transaction-safe, idempotent, rollback-safe, multi-device safe where multi-device applies;
- sales may split consumption across multiple FIFO layers;
- Owner, supplier tax origin, unit conversion, cost/HPP come from proven authoritative relations;
- LEGACY/UNVERIFIED relations fail closed and are not guessed;
- cancellation/return must not contaminate active cost/Owner/tax reporting.

### SANRO Stock authority

- Tunas Baru/current direct-cloud SANRO Stock remains direct-cloud while Hybrid is disabled;
- Professional Hybrid ON: stock-mutating SANRO Stock operations must target Perangkat Utama Local API so authoritative local stock changes immediately;
- read-only/reporting paths may remain direct-cloud if they do not mutate authoritative stock;
- Free/Starter SANRO Stock remains unavailable under current product requirement.

## Point Reward model

Point Reward requires its own ledger; do not derive entire state only from current balance.

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

For a transaction whose authority is local Room (for example local-first POS sale), one Room transaction should atomically commit all locally authoritative effects required by that operation:

- Sale
- SaleDetails
- Payment when immediate settlement exists
- PaymentAllocations
- InventoryMovements
- FIFO layer updates/consumption
- Point ledger mutation/pending entry when applicable
- local document sequence update when locally owned
- SyncQueue outbox event(s) only when that mode actually synchronizes operational transactions

No partial commit is acceptable.

Do not dual-write the same business transaction from Web UI directly to cloud while Native Bridge separately mirrors it to Room.

For Professional Hybrid locally authoritative POS writes, preferred ownership is Room/Perangkat Utama commit -> transactional outbox -> sync engine -> Cloud API.

For Free/Starter, the transaction remains local operationally; do not invent cloud transaction sync merely because `sync_queue` exists in the shared architecture.

For Direct Cloud mode, existing cloud write ownership remains active until explicitly switched.

## Transactional outbox

Outbox is required only for operations that have a downstream synchronization target.

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
- Inventory: movement sync + reconciliation where synchronization exists.
- Return/payment: stable UUID + source refs + idempotency.
- Important conflicts must remain auditable.

Direct-cloud and local-only modes should not manufacture sync conflicts for entities that do not sync.

## Numeric model — proposal, not LOCK

Do not use floating point for core money/qty.

Candidate model:

- Money: fixed-point `Long` minor units / defined decimal scale.
- Quantity: fixed-scale `Long` (candidate scale 1e6 for fractional units).
- Percent: fixed-scale integer / basis points where suitable.
- Time: UTC epoch millis plus separate business date where business-day semantics matter.

Final scale remains pending exact LIVE schema/runtime audit.

## Backup/restore requirements

Backup must be a transactionally consistent Room/SQLite snapshot, not an arbitrary raw copy while writers are active.

Required concept:

1. gate/pause local writers;
2. drain active transaction;
3. controlled checkpoint/close or supported safe snapshot mechanism;
4. snapshot database;
5. checksum;
6. encrypt;
7. validate encrypted artifact and metadata;
8. reopen writers;
9. upload to user-owned Google Drive when backup integration is ready and applicable.

Do not implement encryption until cross-device key recovery is designed. Android Keystore alone is insufficient for lost-device replacement.

Room migrations must be explicit/versioned. `fallbackToDestructiveMigration()` is forbidden for the transaction database.

## Device replacement

Identity must be independent of hardware device ID.

Free/Starter target:

install -> login/verify entitlement -> restore/replace device -> validated encrypted backup restore -> revoke old device slot -> activate new device.

Because operational transactions are local-only for Free/Starter, recovery quality is critical; a cloud transaction database must not be assumed as the restore source.

Professional Hybrid Perangkat Utama replacement target:

controlled recovery/transfer -> authenticate -> validate branch/device authority -> rebuild/restore authoritative Room state from approved cloud/local recovery sources -> reconcile pending sync -> revoke old Perangkat Utama -> activate replacement.

Perangkat Kasir replacement is separate from Perangkat Utama replacement because device role and user role are different.

## Android Studio readiness gate

Do not start Room/Bluetooth implementation from an incomplete V1.0.5 project snapshot.

Current GitHub `WORK_V1_0_5` is protected but not a complete Android Studio project. Before source implementation:

1. obtain FULL V1.0.5 source/project that produced the user-tested WORK APK;
2. verify Manifest, root Gradle/settings, wrapper, resources, signing-neutral project structure, modules and package IDs;
3. create complete Android checkpoint/rollback;
4. build V1.0.5 unchanged first;
5. install over existing V1.0.5 and run regression smoke;
6. only then open Stage B implementation.

Protected Android behavior to preserve includes WebView/session, camera/scan, upload/file chooser, download handling, safe browsing, SSL guard, external URL handling, Android PrintManager, safe-area handling, launcher identity, Back behavior, and orientation: phone <600dp portrait; tablet >=600dp follows Android/user orientation.

## Remaining Stage A blockers

1. Exact LIVE Hosting schema/runtime capture via READ-ONLY metadata/`SHOW CREATE TABLE` and source verification for required routes.
2. FULL buildable Android V1.0.5 source/project matching the tested APK.
3. Per-document authority matrix and exact final transaction-number generators must be confirmed before local transaction implementation.
4. Kasir behavior when Perangkat Utama is unreachable remains an explicit product decision; do not invent it.

## Rollback/deployment status for this document

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE

This document does not authorize production migration or Room implementation yet.
