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

- **GAS-Referensi**: referensi GAS/Golden Reference, hanya 3 LOCKED terbaru di branch aktif.
- **Sanro-POS-Stock**: source dan progress SANRO POS / SANRO STOCK.
- **Sanro-SuperAdmin**: source SANRO SuperAdmin.
- **Sanro-Android**: source aplikasi Android/PWA SANRO.

Jika ada konflik antara file lama, chat lama, dan baseline LOCKED terbaru, prioritaskan baseline LOCKED terbaru yang telah disetujui.

## 9. Aturan sebelum perubahan

Sebelum mengubah source:
1. identifikasi baseline/checkpoint terbaru;
2. baca aturan ini;
3. pastikan scope perubahan;
4. jangan mengubah area yang sudah stabil di luar scope;
5. setelah selesai, dokumentasikan hasil dan simpan progress ke GitHub sesuai aturan di atas.

---
Canonical rules file: `SANRO_DEVELOPMENT_RULES.md`
