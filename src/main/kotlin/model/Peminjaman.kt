package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Data class untuk enkapsulasi data peminjaman
// Semua data dan behavior peminjaman dalam satu class
// ============================================================
data class Peminjaman(
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties dengan visibility control
    // Menyimpan semua informasi terkait peminjaman
    // ============================================================
    val id: String,
    val anggotaId: String,           // Foreign key ke Anggota
    val bukuId: String,              // Foreign key ke Buku
    val tipeBuku: TipeBuku,          // CETAK atau DIGITAL
    val tanggalPinjam: LocalDate,
    val jatuhTempo: LocalDate,
    var tanggalKembali: LocalDate?,  // Nullable: null jika belum dikembalikan
    var denda: Int = 0               // Default value 0
) {
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk cek status peminjaman
    // Method untuk mengecek apakah peminjaman masih aktif
    // ============================================================
    fun aktif(): Boolean {
        return tanggalKembali == null || tanggalKembali == tanggalPinjam
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk hitung hari keterlambatan
    // Perhitungan dilakukan di dalam class (information hiding)
    // ============================================================
    fun hariTerlambat(asOf: LocalDate): Long {
        val akhir = tanggalKembali ?: asOf
        val selisihHari = ChronoUnit.DAYS.between(jatuhTempo, akhir)
        return max(0, selisihHari)  // Return 0 jika tidak terlambat
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk perhitungan denda
    // Aturan bisnis: hanya buku cetak yang kena denda
    // Rate: Rp 1000 per hari keterlambatan
    // ============================================================
    fun hitungDenda(rate: Int, asOf: LocalDate): Int {
        // Buku digital tidak kena denda
        if (tipeBuku != TipeBuku.CETAK) return 0

        val jumlahHariTerlambat = hariTerlambat(asOf)
        return (jumlahHariTerlambat * rate).toInt()
    }
}