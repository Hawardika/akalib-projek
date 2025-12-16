package service

import model.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ============================================================
// OOP CONCEPT: SINGLE RESPONSIBILITY PRINCIPLE
// Class khusus untuk ekspor data ke CSV
// Memisahkan concern export dari business logic
// ============================================================
class CSVExporter(
    private val catalogService: CatalogService,
    private val memberService: MemberService,
    private val circulationService: CirculationService,
    private val reservationService: ReservationService
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk export semua laporan ke CSV
    // ============================================================
    fun eksporSemuaLaporan(direktori: String = "."): Map<String, String> {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val hasil = mutableMapOf<String, String>()

        try {
            // Export katalog buku
            val fileBuku = "$direktori/laporan_buku_$timestamp.csv"
            eksporKatalogBuku(fileBuku)
            hasil["buku"] = fileBuku

            // Export daftar anggota
            val fileAnggota = "$direktori/laporan_anggota_$timestamp.csv"
            eksporDaftarAnggota(fileAnggota)
            hasil["anggota"] = fileAnggota

            // Export peminjaman
            val filePeminjaman = "$direktori/laporan_peminjaman_$timestamp.csv"
            eksporPeminjaman(filePeminjaman)
            hasil["peminjaman"] = filePeminjaman

            // Export reservasi
            val fileReservasi = "$direktori/laporan_reservasi_$timestamp.csv"
            eksporReservasi(fileReservasi)
            hasil["reservasi"] = fileReservasi

            // Export statistik
            val fileStatistik = "$direktori/laporan_statistik_$timestamp.csv"
            eksporStatistik(fileStatistik)
            hasil["statistik"] = fileStatistik

            println("\n Semua laporan berhasil diekspor!")
        } catch (e: Exception) {
            println(" Error saat ekspor: ${e.message}")
        }

        return hasil
    }

    // ============================================================
    // Export katalog buku dengan informasi lengkap
    // ============================================================
    private fun eksporKatalogBuku(namaFile: String) {
        val buku = catalogService.getAllBuku()

        File(namaFile).bufferedWriter().use { writer ->
            // Header
            writer.write("ID,Judul,Penulis,Tahun,Kategori,Tipe,Detail\n")

            // Data
            buku.forEach { b ->
                val detail = when (b) {
                    is BukuCetak -> "Cetak|${b.jumlahHalaman}hal|Stok:${b.stok}"
                    is BukuDigital -> "Digital|${b.ukuranFileMb}MB|${b.formatDigital}"
                    is BukuAudio -> "Audio|${b.durasi}min|${b.narator}|${b.formatAudio}"
                    else -> "Unknown"
                }

                writer.write("${b.id},\"${b.judul}\",\"${b.penulis}\",${b.tahun},\"${b.kategori}\",$detail\n")
            }
        }

        println(" Katalog buku diekspor ke: $namaFile")
    }

    // ============================================================
    // Export daftar anggota
    // ============================================================
    private fun eksporDaftarAnggota(namaFile: String) {
        val anggota = memberService.getAllAnggota()

        File(namaFile).bufferedWriter().use { writer ->
            // Header
            writer.write("ID,Nama,Tier,MaxPinjam,DurasiHari,Status\n")

            // Data
            anggota.forEach { a ->
                writer.write("${a.id},\"${a.nama}\",${a.tier},${a.tier.maxLoans},${a.tier.loanDays},${a.statusAktif}\n")
            }
        }

        println(" Daftar anggota diekspor ke: $namaFile")
    }

    // ============================================================
    // Export data peminjaman
    // ============================================================
    private fun eksporPeminjaman(namaFile: String) {
        val peminjaman = circulationService.getAllPeminjaman()

        File(namaFile).bufferedWriter().use { writer ->
            // Header
            writer.write("ID,AnggotaID,BukuID,TipeBuku,TglPinjam,JatuhTempo,TglKembali,Denda,Status\n")

            // Data
            peminjaman.forEach { p ->
                val status = if (p.aktif()) "AKTIF" else "SELESAI"
                val tglKembali = p.tanggalKembali?.toString() ?: "-"

                writer.write("${p.id},${p.anggotaId},${p.bukuId},${p.tipeBuku}," +
                        "${p.tanggalPinjam},${p.jatuhTempo},$tglKembali,${p.denda},$status\n")
            }
        }

        println(" Data peminjaman diekspor ke: $namaFile")
    }

    // ============================================================
    // Export data reservasi
    // ============================================================
    private fun eksporReservasi(namaFile: String) {
        val reservasi = reservationService.getAllReservasi()

        File(namaFile).bufferedWriter().use { writer ->
            // Header
            writer.write("BukuID,JumlahAntrian,DaftarAnggota\n")

            // Data
            reservasi.forEach { r ->
                val daftarAnggota = r.antrian.joinToString(";")
                writer.write("${r.bukuId},${r.jumlah()},\"$daftarAnggota\"\n")
            }
        }

        println(" Data reservasi diekspor ke: $namaFile")
    }

    // ============================================================
    // Export statistik perpustakaan
    // ============================================================
    private fun eksporStatistik(namaFile: String) {
        val totalDenda = circulationService.getTotalDenda()
        val top3 = circulationService.getTop3BukuTerpopuler()
        val totalPeminjaman = circulationService.getAllPeminjaman().size
        val totalBuku = catalogService.getAllBuku().size
        val totalAnggota = memberService.getAllAnggota().size

        File(namaFile).bufferedWriter().use { writer ->
            // Header
            writer.write("Metrik,Nilai\n")

            // Data
            writer.write("Total Buku,$totalBuku\n")
            writer.write("Total Anggota,$totalAnggota\n")
            writer.write("Total Peminjaman,$totalPeminjaman\n")
            writer.write("Total Denda Terkumpul,$totalDenda\n")

            // Top 3 buku
            top3.forEachIndexed { index, (bukuId, count) ->
                val buku = catalogService.cariBukuById(bukuId)
                writer.write("Buku Terpopuler #${index + 1},\"${buku?.judul ?: bukuId}\",$count\n")
            }
        }

        println("📄 Statistik diekspor ke: $namaFile")
    }
}