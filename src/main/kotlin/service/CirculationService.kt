package service

import model.*
import repository.Repository
import java.time.LocalDate

// ============================================================
// OOP CONCEPT: COMPOSITION + AGGREGATION
// CirculationService menggunakan multiple services (has-a relationship)
// Service ini mengkoordinasi operasi dari beberapa service lain
// Contoh Aggregation: CirculationService aggregate CatalogService, MemberService, ReservationService
// ============================================================

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Enkapsulasi complex business logic untuk peminjaman dan pengembalian
// Separation of Concerns: memisahkan logic circulation dari service lain
// ============================================================
class CirculationService(
    // ============================================================
    // OOP CONCEPT: COMPOSITION + DEPENDENCY INJECTION
    // Service ini "has-a" repository dan services lain
    // Dependencies di-inject via constructor (Dependency Inversion Principle)
    // ============================================================
    private val peminjamanRepo: Repository<Peminjaman>,
    private val catalogService: CatalogService,
    private val memberService: MemberService,
    private val reservationService: ReservationService
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Complex business logic untuk peminjaman buku
    // Validasi: anggota, buku, tier limit, stok
    // ============================================================
    fun pinjamBuku(
        idPeminjaman: String,
        idAnggota: String,
        idBuku: String
    ): Boolean {
        // ============================================================
        // Validasi 1: Cek anggota exists dan aktif
        // ============================================================
        val anggota = memberService.cariAnggotaById(idAnggota)
        if (anggota == null) {
            println("❌ Anggota dengan ID '$idAnggota' tidak ditemukan.")
            return false
        }

        if (!memberService.cekStatusAktif(idAnggota)) {
            println("❌ Anggota '$idAnggota' tidak aktif.")
            return false
        }

        // ============================================================
        // Validasi 2: Cek buku exists
        // ============================================================
        val buku = catalogService.cariBukuById(idBuku)
        if (buku == null) {
            println("❌ Buku dengan ID '$idBuku' tidak ditemukan.")
            return false
        }

        // ============================================================
        // Validasi 3: Cek batas maksimal peminjaman berdasarkan tier
        // Business rule dari Tier enum
        // ============================================================
        val pinjamanAktif = getPinjamanAktifByAnggota(idAnggota).size
        if (pinjamanAktif >= anggota.tier.maxLoans) {
            println("❌ Anggota sudah mencapai batas maksimal peminjaman (${anggota.tier.maxLoans} buku).")
            return false
        }

        val tipeBuku: TipeBuku

        // ============================================================
        // OOP CONCEPT: POLYMORPHISM
        // Type checking untuk menentukan behavior berbeda
        // BukuCetak vs BukuDigital memiliki aturan berbeda
        // ============================================================
        if (buku is BukuCetak) {
            // Buku cetak: cek stok dan kurangi
            if (buku.stok <= 0) {
                println("❌ Stok buku cetak habis.")
                println("💡 Apakah ingin mendaftar reservasi? (y/n): ")
                return false
            }
            catalogService.kurangiStok(idBuku)
            tipeBuku = TipeBuku.CETAK
        } else {
            // Buku digital: tidak ada stok limit
            tipeBuku = TipeBuku.DIGITAL
        }

        // ============================================================
        // Business logic: Hitung jatuh tempo berdasarkan tier
        // ============================================================
        val tanggalPinjam = LocalDate.now()
        val jatuhTempo = tanggalPinjam.plusDays(anggota.tier.loanDays.toLong())

        // ============================================================
        // OOP CONCEPT: ENCAPSULATION
        // Buat object Peminjaman dengan semua data terenkapsulasi
        // ============================================================
        val peminjaman = Peminjaman(
            id = idPeminjaman,
            anggotaId = idAnggota,
            bukuId = idBuku,
            tipeBuku = tipeBuku,
            tanggalPinjam = tanggalPinjam,
            jatuhTempo = jatuhTempo,
            tanggalKembali = null,  // Belum dikembalikan
            denda = 0
        )

        peminjamanRepo.save(peminjaman)
        println("✅ Peminjaman berhasil! Buku '${buku.judul}' dipinjam hingga $jatuhTempo")

        return true
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk pengembalian buku
    // Termasuk perhitungan denda dan notifikasi reservasi
    // ============================================================
    fun kembalikanBuku(idPeminjaman: String, tanggalKembali: LocalDate): Int {
        val peminjaman = peminjamanRepo.findById(idPeminjaman)

        if (peminjaman == null) {
            println("❌ Peminjaman dengan ID '$idPeminjaman' tidak ditemukan.")
            return -1
        }

        // Update tanggal kembali dan hitung denda
        peminjaman.tanggalKembali = tanggalKembali
        val denda = peminjaman.hitungDenda(1000, tanggalKembali)
        peminjaman.denda = denda

        peminjamanRepo.update(idPeminjaman, peminjaman)

        // ============================================================
        // Business logic: Handle stok dan reservasi untuk buku cetak
        // ============================================================
        if (peminjaman.tipeBuku == TipeBuku.CETAK) {
            catalogService.tambahStok(peminjaman.bukuId)

            // ============================================================
            // OOP CONCEPT: AGGREGATION
            // Menggunakan ReservationService untuk cek antrian
            // ============================================================
            val reservasi = reservationService.getReservasiBuku(peminjaman.bukuId)
            if (reservasi != null && !reservasi.kosong()) {
                val nextAnggota = reservasi.next()
                println("📢 Buku tersedia untuk anggota berikutnya dalam antrian: $nextAnggota")
            }
        }

        val buku = catalogService.cariBukuById(peminjaman.bukuId)
        println("✅ Buku '${buku?.judul}' telah dikembalikan.")
        if (denda > 0) {
            println("💰 Total denda: Rp$denda")
        }

        return denda
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Query methods untuk mendapatkan data peminjaman
    // ============================================================
    fun getPinjamanAktifByAnggota(idAnggota: String): List<Peminjaman> {
        return peminjamanRepo.findAll().filter {
            it.anggotaId == idAnggota && it.aktif()
        }
    }

    fun getAllPeminjaman(): List<Peminjaman> = peminjamanRepo.findAll()

    fun getTotalDenda(): Int = peminjamanRepo.findAll().sumOf { it.denda }

    // ============================================================
    // Business logic: Hitung buku terpopuler
    // ============================================================
    fun getTop3BukuTerpopuler(): List<Pair<String, Int>> {
        return peminjamanRepo.findAll()
            .groupingBy { it.bukuId }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }
    }
}