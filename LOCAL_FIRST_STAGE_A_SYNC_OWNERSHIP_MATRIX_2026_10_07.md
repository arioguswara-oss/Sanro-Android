# SANRO Android — Stage A Sync / Transaction Ownership Matrix

Date: 2026-10-07
Status: **PLANNED / AUDIT**
Scope: architecture documentation only. No Android source, Hosting frontend/backend, or database mutation.

## Purpose

Prevent dual-write and authority ambiguity when SANRO POS evolves from current Direct Cloud WebView operation to plan-aware local-first / Hybrid operation.

This matrix must not be treated as permission to enable Hybrid on Tunas Baru or production.

## Authority principles

1. One business mutation must have one authoritative write path at a time.
2. Do not let Web/PWA POST directly to cloud while Native Bridge independently mirrors the same mutation into Room.
3. Technical UUID/idempotency key is separate from visible transaction/document number.
4. WORK/LOCK business behavior stays protected regardless of transport/storage mode.
5. Tunas Baru remains current Direct Cloud until explicit rollout decision and verified migration/testing.

## Mode matrix

| Runtime / Plan Mode | Penjualan / cashier mutation owner | Local Room role | Cloud operational role | Notes |
|---|---|---|---|---|
| Tunas Baru current production | Hosting Direct Cloud | Not authoritative for current runtime | Authoritative | Hybrid disabled. Do not reroute silently. |
| Free | Single Android device | Authoritative operational transaction DB | No cloud operational transaction DB; cloud used for account/license/services allowed by plan | Initial login/activation online; supported transactions must continue offline after activation. |
| Starter | Single Android device | Authoritative operational transaction DB | No cloud operational transaction DB for normal transaction operation under current mandatory rule | Same local-first single-device principle; backup/recovery and future upgrade path must be safe. |
| Professional+ Multi Device ON | Perangkat Utama per branch | Authoritative local operational DB for cashier operations | Cloud synchronization/central service; Perangkat Utama is the sole operational sync gateway for cashier transaction path | Perangkat Kasir talks to Perangkat Utama via LAN/Wi-Fi/local API and does not sync cashier transactions directly to cloud. |
| Professional+ Multi Device OFF | **OPEN implementation detail** | Room foundation is still required by product architecture, but exact authoritative write/sync routing must be confirmed before implementation | Cloud capability exists | Do not invent whether this mode behaves as one-device local-first+cloud-sync or pure direct-cloud. Resolve explicitly before activating Stage involving transaction routing. |

## Back Office / browser ownership

Under the current Hybrid rule for Professional+:

- Pembelian is Back Office and may write direct-to-cloud.
- Dashboard, Laporan, Master Data, Setting, and administration may operate direct-cloud according to access rights and plan design.
- Any cloud change affecting cashier operation (item, price, stock, contact, sales settings, etc.) must be synchronized back to the Perangkat Utama before it is treated as authoritative in cashier-local operation.

This means `purchases` may exist in Room as synchronized operational data required for FIFO/stock/cost behavior without implying that every Professional purchase must originate from Room.

For Free/Starter, exact offline/local ownership of modules beyond the required cashier transaction scope must be implemented according to plan requirements and tested behavior; do not blindly copy the Professional Back Office routing.

## SANRO Stock ownership

### Current Tunas Baru / Hybrid disabled
SANRO Stock follows the current verified Direct Cloud runtime.

### Professional+ Multi Device ON
Any SANRO Stock operation that changes authoritative inventory state, including Stock Opname/correction/mutation, must route to Perangkat Utama through the authorized local Inventory API/capability so cashier-local stock changes immediately and consistently.

Read-only/reporting operations may use cloud if allowed by architecture and do not mutate authoritative stock.

SANRO Stock pairing/authorization should be capability-specific and separate from POS cashier-device role.

## Perangkat Kasir rule

When Multi Device ON:

- Perangkat Kasir does not own the branch database.
- It must not allocate independent authoritative transaction state disconnected from Perangkat Utama.
- It must not sync cashier transactions directly to cloud.
- Full tenant/business/master/sync/user/package Setting is unavailable.
- Only local terminal hardware settings are allowed (printer/scanner/cash drawer/customer display/etc.).

Behavior when Perangkat Kasir cannot reach Perangkat Utama remains an explicit product decision. Do not implement an offline independent cashier queue that can later conflict with Perangkat Utama unless owner explicitly approves a deterministic protocol.

## Local outbox ownership

Where Room is authoritative for a transaction path:

- transaction + effects + outbox must commit atomically in one Room transaction;
- outbox event UUID becomes the idempotency key for cloud/local-gateway replay;
- same key + same payload = replay same logical result;
- same key + different payload = fail closed/conflict;
- no second independent writer may mutate the same business transaction concurrently.

Where cloud is authoritative (for example current Tunas Baru Direct Cloud or permitted Professional Back Office path), do not create a competing local mutation writer. Room may consume synchronized projections/cache as explicitly designed.

## Master-data synchronization

For any local cashier-authoritative runtime, cache/projection of at least the cashier-required subset must have revision/version information. Candidate cashier dependencies include:

- item identity/status/barcode;
- units/conversions;
- selling prices and applicable quantity tiers;
- contacts/customer tax status needed by Penjualan;
- Point Reward settings/rules required offline;
- PPN/sales settings required offline;
- access-policy snapshot required for offline authorization;
- inventory/FIFO information required for transaction correctness.

Do not use blind last-write-wins for important master data. Stale policy/price/inventory handling requires explicit fail-safe rules during later stages.

## Connection indicator implications

Connection status must reflect the target actually required by the current mode:

- Free/Starter: local DB health + account/license/internet as relevant; do not pretend transaction cloud sync exists if the plan has no cloud operational transaction DB.
- Professional Perangkat Utama: local DB, cloud reachability, sync state/pending, cashier-device connectivity.
- Professional Perangkat Kasir: LAN/Wi-Fi/local API reachability to Perangkat Utama is primary for cashier operation.
- Tunas Baru Direct Cloud: internet/backend/cloud DB reachability and mode shown as Direct Cloud / Hybrid Disabled.

## Stage B hard gates related to ownership

Before implementing transaction routing:

1. complete buildable V1.0.5 WORK source must be recovered/proven;
2. latest LIVE schema/runtime capture must close physical mapping gaps;
3. Professional+ Multi Device OFF authority path must be explicitly resolved before that mode is enabled;
4. Perangkat Kasir unreachable behavior must be explicitly resolved before multi-device transaction implementation;
5. complete Android checkpoint + rollback must exist;
6. feature flags/entitlement must ensure Tunas Baru remains Direct Cloud until separately approved.

## Rollback / deployment impact

- Frontend rollback: NO
- Backend rollback: NO
- Database rollback: NO
- Android rollback: NO
- Restart Node.js: NO
- Production deployment: NONE
