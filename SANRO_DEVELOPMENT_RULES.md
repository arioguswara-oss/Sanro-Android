# SANRO Development Rules

Dokumen ini adalah aturan kerja kanonik untuk seluruh pengembangan SANRO POS, SANRO STOCK, SANRO SuperAdmin, SANRO Android, dan GAS Reference.

## 1. Baseline dan LOCKED

- Jangan mengubah modul, behavior, atau file yang sudah berstatus **LOCKED** kecuali ada permintaan eksplisit.
- Setiap repo SANRO hanya menyimpan **maksimal 3 baseline LOCKED terbaru** pada branch aktif.
- Jika LOCKED baru membuat jumlah menjadi 4, LOCKED tertua dikeluarkan dari branch aktif setelah dipastikan sudah tergantikan.
- Riwayat versi lama tetap dipertahankan melalui Git history/commit/tag; jangan rewrite history hanya untuk cleanup.
- Untuk repo **GAS-Referensi**, branch aktif hanya menyimpan **3 file LOCKED terbaru**.

## 2. Status versi

Gunakan status berikut secara konsisten:
- **WORK**: perubahan sudah dikerjakan, belum final.
- **TESTING**: siap atau sedang diuji.
- **LOCKED**: sudah disetujui sebagai baseline stabil dan tidak boleh diubah sembarangan.

Jangan menyatakan LOCKED tanpa persetujuan eksplisit.

## 3. GitHub workflow

- Gunakan GitHub sebagai version control utama.
- Commit hanya perubahan yang relevan dengan task.
- Gunakan format commit yang jelas, misalnya:
  - `V1.10.264 WORK - Fix mapping pembelian PKP`
  - `V1.10.264 LOCKED - Pembelian parity checkpoint`
- Jangan menimpa baseline stabil secara diam-diam.
- Jangan force-push atau rewrite history kecuali diminta secara eksplisit dan dampaknya sudah dijelaskan.
- Untuk perubahan aktif, utamakan source yang dapat ditinjau; ZIP dipakai untuk checkpoint/release/handoff bila diperlukan.

## 4. Cleanup repository

- Maksimal 3 LOCKED terbaru per repo.
- Maksimal 1 WORK/TESTING aktif bila masih diperlukan.
- Hapus file sementara, duplikat, cache, log runtime, dan artefak yang jelas sudah tidak dipakai.
- Jangan menghapus file yang statusnya belum jelas.
- Sebelum cleanup, pastikan versi yang dipertahankan benar-benar lebih baru dan dapat menggantikan versi lama.

## 5. Security dan secrets

Jangan commit:
- `.env`
- password database
- API token / API key
- credential Hostinger
- credential OAuth
- session secret
- private key
- nomor/token akses sensitif
- backup database yang berisi credential atau data pribadi tanpa alasan dan proteksi yang jelas

Gunakan `.gitignore` atau mekanisme secret/environment variable yang sesuai.

## 6. Testing dan regression

Sebelum menyatakan task selesai:
- pastikan perubahan sesuai scope;
- uji fungsi yang diubah;
- lakukan regression test pada behavior LOCKED yang berdekatan;
- jangan menyatakan DONE jika pengujian gagal, belum dilakukan, atau terhalang akses;
- catat hasil test secara jujur.

Untuk WhatsApp QA SANRO, jangan mengirim pesan otomatis tanpa izin eksplisit.

## 7. Handoff dan progress

Pada checkpoint penting, perbarui dokumentasi progress/handoff yang relevan dengan:
- versi;
- status WORK / TESTING / LOCKED;
- perubahan;
- hasil testing;
- regression test;
- deployment path bila relevan;
- keputusan penting;
- next step;
- overall project progress bila sedang membuat laporan progress.

Untuk perpindahan chat atau handoff besar, pertahankan artefak yang diperlukan seperti ZIP frontend, ZIP backend, dan roadmap/handoff sesuai kebutuhan.

## 8. Source of truth

- Hosting LIVE PRODUCTION / source-runtime terbaru adalah source of truth operasional tertinggi untuk SANRO POS yang sudah live.
- Hasil user-test terbaru mengalahkan dokumentasi/source lama.
- WORK/LOCK terbaru mengalahkan checkpoint lama.
- **GAS-Referensi** hanya fallback/historis bila Hosting sudah menjadi production source of truth.
- **Sanro-POS-Stock**: source dan progress SANRO POS / SANRO STOCK.
- **Sanro-SuperAdmin**: source SANRO SuperAdmin.
- **Sanro-Android**: source aplikasi Android/PWA SANRO.
- **SanroTicket**: source SANRO Ticket.

Jika ada konflik, prioritaskan Hosting LIVE terbaru → hasil user-test terbaru → WORK/LOCK terbaru → checkpoint terbaru → GitHub/dokumentasi lama.

## 9. Rule View Android — LOCKED

- Berlaku untuk **SanroPOS** dan **SanroStock**.
- Perangkat **phone** (`smallestScreenWidthDp < 600`) wajib **portrait only**.
- Perangkat **tablet** (`smallestScreenWidthDp >= 600`) mengikuti preferensi orientasi pengguna/Android: dapat portrait atau landscape dan tetap mendukung auto-rotate.
- Klasifikasi tidak menggunakan ukuran inci fisik layar.
- Rule ini berstatus **LOCKED** dan tidak boleh diubah tanpa instruksi eksplisit.

## 10. RULE WAJIB — Android Hybrid Mode, Multi Device, SANRO Stock, dan Connection Status

### SANRO POS Free / Starter
- Single device.
- Database operasional lokal di perangkat; tidak memiliki database cloud operasional untuk transaksi.
- Login/aktivasi awal wajib internet ON untuk autentikasi akun, tenant, paket/lisensi, entitlement, dan registrasi device.
- Setelah login berhasil, transaksi single-device wajib tetap dapat berjalan offline menggunakan database lokal.
- Tidak ada topologi Perangkat Utama / Perangkat Kasir pada Free/Starter.

### SANRO POS Professional dan di atasnya
- Multi Device adalah kemampuan paket, bukan kewajiban. Professional boleh tetap single device dengan `Multi Device = OFF`.
- Jika Multi Device ON: **1 cabang = maksimal 1 Perangkat Utama aktif**.
- Perangkat Utama adalah Android SANRO POS yang di-lock/bind ke cabang, menetap di lokasi toko, memegang authoritative local database, local API/service, dan menjadi satu-satunya gateway sync database/transaksi operasional ke SANRO Cloud.
- Perangkat Kasir adalah Android SANRO POS Mode Kasir yang terhubung ke Perangkat Utama via LAN/Wi‑Fi/local API.
- User tetap login dengan email masing-masing. Device role (Utama/Kasir) dan user role (Administrator/Kasir/dll.) adalah dua hal berbeda.
- Tambah Perangkat Kasir memakai QR pairing sekali pakai + approval Administrator. IP manual bukan alur normal user; discovery ideal memakai device identity + hostname/mDNS + last-known IP/fallback discovery.
- Penjualan Mode Kasir wajib lewat Perangkat Utama dan tidak boleh sync transaksi Penjualan langsung ke cloud saat Multi Device Hybrid aktif.
- Setting penuh tenant/bisnis tidak tersedia di Mode Kasir. Kasir hanya boleh mengatur hardware lokal terminal seperti printer thermal Bluetooth/USB/LAN, barcode scanner, cash drawer, customer display, atau peripheral kasir lain yang didukung.
- Format/ukuran kertas, template nota, header/footer, aturan cetak, dan konfigurasi bisnis berasal dari Perangkat Utama/Admin.
- Perangkat Utama tidak boleh berpindah bebas. Ganti device hanya melalui recovery/transfer resmi jika perangkat rusak/diganti.

### SANRO Stock
- Saat Tunas Baru/current runtime masih direct-cloud dan Multi Device Hybrid DISABLED, SANRO Stock tetap direct ke cloud seperti runtime yang berlaku.
- Saat Professional Multi Device Hybrid ON, operasi SANRO Stock yang mengubah authoritative stock (mis. Stock Opname/koreksi/mutasi inventory) wajib menuju Perangkat Utama melalui Local API Inventory agar stok lokal kasir langsung konsisten.
- SANRO Stock dapat memiliki pairing/authorization ke Perangkat Utama yang terpisah secara capability dari POS Mode Kasir.
- Read-only/reporting tertentu boleh direct-cloud jika tidak mengubah authoritative stock.

### Back Office dan desktop/browser
- Pembelian adalah Back Office dan boleh direct-to-cloud.
- Dashboard, Laporan, Master Data, Setting, dan administrasi lain boleh direct-cloud sesuai desain/hak akses.
- Setiap perubahan cloud yang memengaruhi operasi kasir (barang, harga, stok, kontak, setting Penjualan, dll.) wajib disinkronkan kembali ke Perangkat Utama sebelum dianggap authoritative secara lokal.
- PC/laptop tetap boleh direct-cloud via browser sebagai Back Office. Target Perangkat Utama/Kasir Hybrid adalah Android.

### Global connection indicator — WAJIB
- **Icon/status koneksi wajib tampil di header semua modul, semua paket, semua tenant, termasuk Tunas Baru.**
- Warna dasar:
  - Hijau = koneksi/layanan normal.
  - Kuning = degraded, koneksi lemah/tidak stabil, latency tinggi, atau pending sync sesuai mode.
  - Merah = offline/layanan target tidak terjangkau.
- Detail popup menyesuaikan runtime:
  - Free/Starter: internet, SANRO account/auth/license, database lokal; jangan tampilkan cloud transaction sync.
  - Professional Perangkat Utama: internet, SANRO Cloud, database lokal, status sync, pending sync, jumlah/status Perangkat Kasir, tombol `Sync Sekarang`.
  - Professional Mode Kasir: fokus koneksi LAN/Wi‑Fi ke Perangkat Utama; bukan cloud transaction sync.
  - Tunas Baru direct-cloud: internet, backend/cloud reachability, database cloud, mode `Direct Cloud`, Multi Device `Disabled`.
- Sync Perangkat Utama berjalan otomatis di background; tombol Sync di header adalah manual force-sync, bukan syarat transaksi normal.

### Rollout Tunas Baru
- Tunas Baru LIVE PRODUCTION tetap direct-to-cloud seperti sekarang sampai ada keputusan rollout baru.
- `Multi Device Hybrid = DISABLED` secara sistem, walaupun user tenant adalah Administrator.
- Hanya SANRO Superadmin/entitlement backend yang boleh mengaktifkan Multi Device Hybrid setelah arsitektur selesai dan lolos testing/regression.
- Menampilkan menu Multi Device atau indikator koneksi tidak boleh secara diam-diam mengubah routing Tunas Baru ke local/hybrid.

### Play Store
- APK SANRO POS yang akan dirilis publik harus sudah membawa fondasi Hybrid yang dibutuhkan: local DB, Native Connector/Bridge, device identity, local service/API hooks, sync engine hooks, feature flag/entitlement, dan pairing/discovery hooks.
- Kemampuan tersebut boleh tetap disabled sampai entitlement/rollout diaktifkan; jangan membuat aplikasi ketiga hanya untuk server/hub.

## 11. RULE WAJIB lintas produk — SANRO Ticket

- SANRO Ticket **cloud-only** untuk database transaksi operasional. Tidak ada local transaction database dan tidak ada offline booking/payment/ticket queue.
- Booking/seat lock/payment/ticket issuance/refund/reschedule/cancellation wajib memakai authoritative cloud/server realtime untuk mencegah double booking.
- SANRO Ticket juga wajib memiliki global connection indicator di semua header modul: hijau normal, kuning degraded, merah offline/backend tidak terjangkau.
- Saat merah/offline, transaksi yang mengubah state harus diblokir aman; jangan dibuat lokal untuk disinkron kemudian.

## 12. Aturan sebelum perubahan

Sebelum mengubah source:
1. identifikasi source/baseline/checkpoint terbaru sesuai source of truth;
2. identifikasi WORK/LOCK yang wajib dilindungi;
3. buat checkpoint/rollback yang diperlukan;
4. pastikan scope perubahan minimal;
5. jangan mengubah area stabil di luar scope;
6. implementasi dan test;
7. regression test;
8. dokumentasikan hasil dan simpan progress ke GitHub.

Jika regression mengenai WORK/LOCK: STOP patch, rollback ke baseline bersih, cari root cause, lalu ulangi.

---
Canonical rules file: `SANRO_DEVELOPMENT_RULES.md`
Cross-product architecture must remain consistent with `arioguswara-oss/Sanro-POS-Stock` canonical roadmap/rules and `arioguswara-oss/SanroTicket/AGENTS.md`.
