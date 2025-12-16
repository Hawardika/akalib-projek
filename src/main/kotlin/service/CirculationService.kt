package service

import model.*
import repository.Repository
import java.time.LocalDate

// ============================================================
// OOP CONCEPT: COMPOSITION + AGGREGATION + STRATEGY PATTERN
// CirculationService menggunakan multiple services (has-a relationship)
// UPDATED: Menggunakan StrategiDenda untuk flexible penalty calculation
// ============================================================
class CirculationService(
    private val peminjamanRepo: Repository<Peminjaman>,
    private val catalogService: CatalogService,
    private val memberService: MemberService,
    private val reservationService: ReservationService
) {

    // ============================================================
    // FITUR BARU: Strategy Pattern untuk Perhitungan Denda
    // Default menggunakan DendaStandar, bisa diubah saat runtime
    // ============================================================
    private var strategiDenda: StrategiDenda = DendaStandar()

    // ============================================================
    // Method untuk mengubah strategi denda (Dependency Injection)
    // ============================================================
    fun setStrategiDenda(strategi: StrategiDenda) {
        this.strategiDenda = strategi
    }

    fun getStrategiDenda(): StrategiDenda = strategiDenda

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
        // Validasi 1: Cek anggota exists dan aktif
        val anggota = memberService.cariAnggotaById(idAnggota)
        if (anggota == null) {
            println(" Anggota dengan ID '$idAnggota' tidak ditemukan.")
            return false
        }

        if (!memberService.cekStatusAktif(idAnggota)) {
            println(" Anggota '$idAnggota' tidak aktif.")
            return false
        }

        // Validasi 2: Cek buku exists
        val buku = catalogService.cariBukuById(idBuku)
        if (buku == null) {
            println(" Buku dengan ID '$idBuku' tidak ditemukan.")
            return false
        }

        // Validasi 3: Cek batas maksimal peminjaman berdasarkan tier
        val pinjamanAktif = getPinjamanAktifByAnggota(idAnggota).size
        if (pinjamanAktif >= anggota.tier.maxLoans) {
            println(" Anggota sudah mencapai batas maksimal peminjaman (${anggota.tier.maxLoans} buku).")
            return false
        }

        val tipeBuku: TipeBuku

        // ============================================================
        // OOP CONCEPT: POLYMORPHISM
        // Type checking untuk menentukan behavior berbeda
        // BukuCetak, BukuDigital, BukuAudio memiliki aturan berbeda
        // ============================================================
        when (buku) {
            is BukuCetak -> {
                // Buku cetak: cek stok dan kurangi
                if (buku.stok <= 0) {
                    println(" Stok buku cetak habis.")
                    println(" Apakah ingin mendaftar reservasi? (y/n): ")
                    return false
                }
                catalogService.kurangiStok(idBuku)
                tipeBuku = TipeBuku.CETAK
            }

            is BukuAudio -> {
                // ============================================================
                // FITUR BARU: Buku Audio tidak ada stok limit
                // Bisa dipinjam unlimited concurrent users
                // ============================================================
                tipeBuku = TipeBuku.DIGITAL  // Treat as digital for penalty logic
                println(" Buku audio dapat dipinjam tanpa batasan stok.")
            }

            else -> {
                // Buku digital: tidak ada stok limit
                tipeBuku = TipeBuku.DIGITAL
            }
        }

        // Hitung jatuh tempo berdasarkan tier
        val tanggalPinjam = LocalDate.now()
        val jatuhTempo = tanggalPinjam.plusDays(anggota.tier.loanDays.toLong())

        // Buat object Peminjaman
        val peminjaman = Peminjaman(
            id = idPeminjaman,
            anggotaId = idAnggota,
            bukuId = idBuku,
            tipeBuku = tipeBuku,
            tanggalPinjam = tanggalPinjam,
            jatuhTempo = jatuhTempo,
            tanggalKembali = null,
            denda = 0
        )

        peminjamanRepo.save(peminjaman)
        println(" Peminjaman berhasil! Buku '${buku.judul}' dipinjam hingga $jatuhTempo")

        return true
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION + STRATEGY PATTERN
    // Business logic untuk pengembalian buku
    // Menggunakan Strategy Pattern untuk perhitungan denda
    // ============================================================
    fun kembalikanBuku(idPeminjaman: String, tanggalKembali: LocalDate): Int {
        val peminjaman = peminjamanRepo.findById(idPeminjaman)

        if (peminjaman == null) {
            println(" Peminjaman dengan ID '$idPeminjaman' tidak ditemukan.")
            return -1
        }

        // Update tanggal kembali
        peminjaman.tanggalKembali = tanggalKembali

        // ============================================================
        // FITUR BARU: Gunakan Strategy Pattern untuk hitung denda
        // Hanya untuk buku cetak, buku digital/audio tidak kena denda
        // ============================================================
        var denda = 0
        if (peminjaman.tipeBuku == TipeBuku.CETAK) {
            val hariTerlambat = peminjaman.hariTerlambat(tanggalKembali)
            if (hariTerlambat > 0) {
                denda = strategiDenda.hitung(hariTerlambat)
                println(" Keterlambatan: $hariTerlambat hari")
                println(" Denda (${strategiDenda.javaClass.simpleName}): Rp$denda")
            }
        }

        peminjaman.denda = denda
        peminjamanRepo.update(idPeminjaman, peminjaman)

        // Handle stok dan reservasi untuk buku cetak
        if (peminjaman.tipeBuku == TipeBuku.CETAK) {
            catalogService.tambahStok(peminjaman.bukuId)

            // Cek reservasi
            val reservasi = reservationService.getReservasiBuku(peminjaman.bukuId)
            if (reservasi != null && !reservasi.kosong()) {
                val nextAnggota = reservasi.next()
                println(" Buku tersedia untuk anggota berikutnya dalam antrian: $nextAnggota")
            }
        }

        val buku = catalogService.cariBukuById(peminjaman.bukuId)
        println(" Buku '${buku?.judul}' telah dikembalikan.")

        if (denda > 0) {
            println(" Total denda yang harus dibayar: Rp$denda")
        } else {
            println(" Tidak ada denda! Terima kasih telah mengembalikan tepat waktu.")
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

    // ============================================================
    // FITUR BARU: Method untuk mendapatkan total denda
    // Mendukung reporting dengan berbagai strategi denda
    // ============================================================
    fun getTotalDenda(): Int = peminjamanRepo.findAll().sumOf { it.denda }

    // ============================================================
    // Method tambahan untuk analisis denda berdasarkan strategi
    // ============================================================
    fun simulasiDenda(hariTerlambat: Long, strategi: StrategiDenda? = null): Int {
        val strategiYangDigunakan = strategi ?: this.strategiDenda
        return strategiYangDigunakan.hitung(hariTerlambat)
    }

    // ============================================================
    // Method untuk perbandingan semua strategi denda
    // Berguna untuk reporting dan decision making
    // ============================================================
    fun bandingkanStrategiDenda(hariTerlambat: Long): Map<String, Int> {
        return mapOf(
            "Standar" to DendaStandar().hitung(hariTerlambat),
            "Progresif" to DendaProgresif().hitung(hariTerlambat),
            "Weekend" to DendaWeekend().hitung(hariTerlambat)
        )
    }

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

    // ============================================================
    // FITUR BARU: Statistik peminjaman berdasarkan tipe buku
    // ============================================================
    fun getStatistikByTipeBuku(): Map<TipeBuku, Int> {
        return peminjamanRepo.findAll()
            .groupingBy { it.tipeBuku }
            .eachCount()
    }

    // ============================================================
    // FITUR BARU: Daftar anggota dengan denda terbanyak
    // ============================================================
    fun getTopPenunggakDenda(limit: Int = 5): List<Pair<String, Int>> {
        return peminjamanRepo.findAll()
            .groupBy { it.anggotaId }
            .mapValues { (_, peminjaman) -> peminjaman.sumOf { it.denda } }
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }
}