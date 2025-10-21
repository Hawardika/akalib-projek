package app

import model.*
import repository.*
import service.*
import java.time.LocalDate
import java.util.*

// ============================================================
// OOP CONCEPT: ENCAPSULATION + COMPOSITION + AGGREGATION
// AkasiaLibCLI adalah main controller yang mengkoordinasi semua komponen
// Composition: CLI "has-a" repositories dan services
// Aggregation: CLI mengagregasi multiple services
// ============================================================
class AkasiaLibCLI {
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Private properties untuk internal use only
    // ============================================================
    private val scanner = Scanner(System.`in`)

    // ============================================================
    // OOP CONCEPT: COMPOSITION + DEPENDENCY INJECTION
    // Membuat instance repositories dengan generic type
    // Lambda expression untuk ID extraction
    // ============================================================
    private val bukuRepo = InMemoryRepo<Buku> { it.id }
    private val anggotaRepo = InMemoryRepo<Anggota> { it.id }
    private val peminjamanRepo = InMemoryRepo<Peminjaman> { it.id }
    private val reservasiRepo = InMemoryRepo<Reservasi> { it.id }

    // ============================================================
    // OOP CONCEPT: COMPOSITION + AGGREGATION
    // Services dibuat dengan dependency injection
    // CirculationService mengagregasi CatalogService, MemberService, ReservationService
    // ============================================================
    private val catalogService = CatalogService(bukuRepo)
    private val memberService = MemberService(anggotaRepo)
    private val reservationService = ReservationService(reservasiRepo)
    private val circulationService = CirculationService(
        peminjamanRepo,
        catalogService,
        memberService,
        reservationService
    )

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Public method sebagai entry point aplikasi
    // ============================================================
    fun run() {
        println("╔════════════════════════════════════╗")
        println("║     Selamat Datang di AkasiaLib    ║")
        println("╚════════════════════════════════════╝")
        println("Ketik 'help' untuk melihat daftar perintah.\n")

        while (true) {
            print("AkasiaLib> ")
            val input = scanner.nextLine().trim().lowercase()

            // Command dispatcher
            when (input) {
                "help" -> showHelp()
                "tambahbuku" -> tambahBuku()
                "listbuku" -> listBuku()
                "tambahanggota" -> tambahAnggota()
                "listanggota" -> listAnggota()
                "pinjam" -> pinjamBuku()
                "kembali" -> kembalikanBuku()
                "reservasi" -> buatReservasi()
                "laporan" -> tampilkanLaporan()
                "demo" -> runDemo()
                "exit", "quit" -> {
                    println("\n👋 Terima kasih telah menggunakan AkasiaLib!")
                    break
                }
                else -> println("❌ Perintah tidak dikenal. Ketik 'help' untuk bantuan.")
            }
            println()
        }
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Private helper methods untuk berbagai operasi
    // ============================================================

    private fun showHelp() {
        println("""
        ╔══════════════════════════════════════════════════════════════╗
        ║                    DAFTAR PERINTAH                           ║
        ╠══════════════════════════════════════════════════════════════╣
        ║  tambahBuku      : Tambah buku baru ke katalog               ║
        ║  listBuku        : Tampilkan semua buku                      ║
        ║  tambahAnggota   : Tambah anggota baru                       ║
        ║  listAnggota     : Tampilkan semua anggota                   ║
        ║  pinjam          : Pinjam buku                               ║
        ║  kembali         : Kembalikan buku                           ║
        ║  reservasi       : Buat reservasi buku                       ║
        ║  laporan         : Tampilkan laporan aktivitas               ║
        ║  demo            : Jalankan skenario demo otomatis           ║
        ║  exit / quit     : Keluar aplikasi                           ║
        ╚══════════════════════════════════════════════════════════════╝
        """.trimIndent())
    }

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM + ENCAPSULATION
    // Method untuk tambah buku dengan type checking
    // Membuat BukuCetak atau BukuDigital berdasarkan input
    // ============================================================
    private fun tambahBuku() {
        println("\n📘 === TAMBAH BUKU BARU ===")

        print("ID Buku: ")
        val id = scanner.nextLine().trim()

        print("Judul: ")
        val judul = scanner.nextLine().trim()

        print("Penulis: ")
        val penulis = scanner.nextLine().trim()

        print("Tahun: ")
        val tahun = scanner.nextLine().toIntOrNull() ?: 2024

        print("Kategori: ")
        val kategori = scanner.nextLine().trim()

        print("Tipe (1=Cetak, 2=Digital): ")
        val tipe = scanner.nextLine().trim()

        try {
            if (tipe == "1") {
                // ============================================================
                // OOP CONCEPT: POLYMORPHISM
                // Membuat instance BukuCetak (subclass dari Buku)
                // ============================================================
                print("Jumlah Halaman: ")
                val halaman = scanner.nextLine().toInt()

                print("Stok: ")
                val stok = scanner.nextLine().toInt()

                val buku = BukuCetak(id, judul, penulis, tahun, kategori, halaman, stok)
                catalogService.tambahBuku(buku)
                println("\n${buku.info()}")
            } else {
                // ============================================================
                // OOP CONCEPT: POLYMORPHISM
                // Membuat instance BukuDigital (subclass dari Buku)
                // ============================================================
                print("Ukuran File (MB): ")
                val ukuran = scanner.nextLine().toDouble()

                print("Format (PDF/EPUB): ")
                val format = scanner.nextLine().uppercase()

                val buku = BukuDigital(
                    id, judul, penulis, tahun, kategori,
                    ukuran, FormatDigital.valueOf(format)
                )
                catalogService.tambahBuku(buku)
                println("\n${buku.info()}")
            }
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM
    // Method yang menggunakan polymorphic behavior
    // info() method akan dipanggil sesuai tipe object (BukuCetak/BukuDigital)
    // ============================================================
    private fun listBuku() {
        println("\n📚 === DAFTAR BUKU ===")
        val bukuList = catalogService.getAllBuku()

        if (bukuList.isEmpty()) {
            println("Belum ada buku dalam katalog.")
            return
        }

        bukuList.forEachIndexed { index, buku ->
            // Polymorphic call: info() akan memanggil implementasi di subclass
            println("${index + 1}. ${buku.info()}")
        }
    }

    private fun tambahAnggota() {
        println("\n👤 === TAMBAH ANGGOTA BARU ===")

        print("ID Anggota: ")
        val id = scanner.nextLine().trim()

        print("Nama: ")
        val nama = scanner.nextLine().trim()

        print("Tier (REGULAR/PREMIUM/STAFF): ")
        val tierInput = scanner.nextLine().uppercase()

        print("Status Aktif (aktif/nonaktif): ")
        val status = scanner.nextLine().trim()

        try {
            val tier = Tier.valueOf(tierInput)
            val anggota = Anggota(id, nama, tier, status)
            memberService.tambahAnggota(anggota)
            println("\nDetail Anggota:")
            println("- ID: ${anggota.id}")
            println("- Nama: ${anggota.nama}")
            println("- Tier: ${anggota.tier} (Max: ${anggota.tier.maxLoans} buku, ${anggota.tier.loanDays} hari)")
            println("- Status: ${anggota.statusAktif}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }

    private fun listAnggota() {
        println("\n👥 === DAFTAR ANGGOTA ===")
        val anggotaList = memberService.getAllAnggota()

        if (anggotaList.isEmpty()) {
            println("Belum ada anggota terdaftar.")
            return
        }

        anggotaList.forEachIndexed { index, anggota ->
            println("${index + 1}. [${anggota.id}] ${anggota.nama} - ${anggota.tier} (${anggota.statusAktif})")
        }
    }

    // ============================================================
    // OOP CONCEPT: AGGREGATION
    // Method ini menggunakan circulationService yang mengagregasi services lain
    // ============================================================
    private fun pinjamBuku() {
        println("\n📚 === PEMINJAMAN BUKU ===")

        print("ID Peminjaman: ")
        val idPeminjaman = scanner.nextLine().trim()

        print("ID Anggota: ")
        val idAnggota = scanner.nextLine().trim()

        print("ID Buku: ")
        val idBuku = scanner.nextLine().trim()

        circulationService.pinjamBuku(idPeminjaman, idAnggota, idBuku)
    }

    private fun kembalikanBuku() {
        println("\n📦 === PENGEMBALIAN BUKU ===")

        print("ID Peminjaman: ")
        val idPeminjaman = scanner.nextLine().trim()

        print("Tanggal Kembali (yyyy-mm-dd) atau tekan Enter untuk hari ini: ")
        val tanggalInput = scanner.nextLine().trim()

        val tanggalKembali = if (tanggalInput.isEmpty()) {
            LocalDate.now()
        } else {
            LocalDate.parse(tanggalInput)
        }

        circulationService.kembalikanBuku(idPeminjaman, tanggalKembali)
    }

    private fun buatReservasi() {
        println("\n📝 === BUAT RESERVASI ===")

        print("ID Buku: ")
        val idBuku = scanner.nextLine().trim()

        print("ID Anggota: ")
        val idAnggota = scanner.nextLine().trim()

        reservationService.buatReservasi(idBuku, idAnggota)
    }

    private fun tampilkanLaporan() {
        println("\n" + "=".repeat(60))
        println("📊 LAPORAN AKTIVITAS PERPUSTAKAAN")
        println("=".repeat(60))

        // Total Denda
        val totalDenda = circulationService.getTotalDenda()
        println("\n💰 Total Denda Terkumpul: Rp$totalDenda")

        // Top 3 Buku
        val top3 = circulationService.getTop3BukuTerpopuler()
        println("\n📚 Top 3 Buku Paling Sering Dipinjam:")
        if (top3.isEmpty()) {
            println("   Belum ada data peminjaman.")
        } else {
            top3.forEachIndexed { index, (bukuId, count) ->
                val buku = catalogService.cariBukuById(bukuId)
                println("   ${index + 1}. ${buku?.judul ?: bukuId} ($count kali)")
            }
        }

        // Pinjaman Aktif
        val pinjamanAktif = circulationService.getAllPeminjaman()
        println("\n📖 Daftar Peminjaman Aktif: ${pinjamanAktif.size} peminjaman")

        // Reservasi
        val reservasiList = reservationService.getAllReservasi()
        println("\n📝 Daftar Reservasi:")
        if (reservasiList.isEmpty()) {
            println("   Tidak ada reservasi aktif.")
        } else {
            reservasiList.forEach { reservasi ->
                val buku = catalogService.cariBukuById(reservasi.bukuId)
                println("   📚 ${buku?.judul ?: reservasi.bukuId}: ${reservasi.jumlah()} anggota dalam antrian")
            }
        }

        println("=".repeat(60))
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION + POLYMORPHISM
    // Demo method yang mendemonstrasikan semua fitur OOP
    // Menggunakan inheritance, polymorphism, dan composition
    // ============================================================
    private fun runDemo() {
        println("\n🎬 === MENJALANKAN SKENARIO DEMO ===\n")

        // ============================================================
        // DEMO 1: POLYMORPHISM & INHERITANCE
        // Membuat instance dari subclass (BukuCetak & BukuDigital)
        // ============================================================
        println("1️⃣ Menambahkan 5 buku (3 cetak, 2 digital)...")

        val buku1 = BukuCetak("BK001", "Pemrograman Kotlin", "John Doe", 2023, "Programming", 350, 2)
        val buku2 = BukuCetak("BK002", "Algoritma Dasar", "Jane Smith", 2022, "Programming", 280, 1)
        val buku3 = BukuCetak("BK003", "Database MySQL", "Bob Johnson", 2024, "Database", 420, 0)
        val buku4 = BukuDigital("BK004", "Web Development", "Alice Brown", 2023, "Programming", 15.5, FormatDigital.PDF)
        val buku5 = BukuDigital("BK005", "Machine Learning", "Charlie Wilson", 2024, "AI", 22.3, FormatDigital.EPUB)

        listOf(buku1, buku2, buku3, buku4, buku5).forEach { catalogService.tambahBuku(it) }

        // ============================================================
        // DEMO 2: ENCAPSULATION & COMPOSITION
        // Membuat anggota dengan tier system (enum encapsulation)
        // ============================================================
        println("\n2️⃣ Menambahkan 3 anggota dengan tier berbeda...")
        val anggota1 = Anggota("AG001", "Budi Santoso", Tier.REGULAR, "aktif")
        val anggota2 = Anggota("AG002", "Siti Nurhaliza", Tier.PREMIUM, "aktif")
        val anggota3 = Anggota("AG003", "Ahmad Pustakawan", Tier.STAFF, "aktif")

        listOf(anggota1, anggota2, anggota3).forEach { memberService.tambahAnggota(it) }

        // ============================================================
        // DEMO 3: AGGREGATION
        // CirculationService menggunakan multiple services
        // ============================================================
        println("\n3️⃣ REGULAR (Budi) pinjam 2 buku cetak...")
        circulationService.pinjamBuku("PM001", "AG001", "BK001")
        circulationService.pinjamBuku("PM002", "AG001", "BK002")

        // ============================================================
        // DEMO 4: POLYMORPHISM
        // Behavior berbeda untuk BukuDigital (tidak kurangi stok)
        // ============================================================
        println("\n4️⃣ PREMIUM (Siti) pinjam buku digital...")
        circulationService.pinjamBuku("PM003", "AG002", "BK004")

        // ============================================================
        // DEMO 5: COMPOSITION & ENCAPSULATION
        // ReservationService dengan Queue (ArrayDeque)
        // ============================================================
        println("\n5️⃣ STAFF (Ahmad) mencoba pinjam buku stok 0...")
        val berhasil = circulationService.pinjamBuku("PM004", "AG003", "BK003")
        if (!berhasil) {
            println("💡 Sistem menawarkan reservasi...")
            reservationService.buatReservasi("BK003", "AG003")
        }

        // ============================================================
        // DEMO 6: ENCAPSULATION
        // Business logic perhitungan denda di class Peminjaman
        // ============================================================
        println("\n6️⃣ REGULAR (Budi) mengembalikan buku terlambat 3 hari...")
        val tanggalTerlambat = LocalDate.now().plusDays(10)
        circulationService.kembalikanBuku("PM001", tanggalTerlambat)

        // ============================================================
        // DEMO 7: AGGREGATION
        // Laporan menggunakan data dari multiple services
        // ============================================================
        println("\n7️⃣ Menampilkan Laporan...")
        tampilkanLaporan()

        println("\n✅ Demo selesai!")
        println("\n📝 KONSEP OOP YANG DIDEMONSTRASIKAN:")
        println("   ✓ Abstraction: Abstract class Buku")
        println("   ✓ Inheritance: BukuCetak & BukuDigital extends Buku")
        println("   ✓ Polymorphism: Method info() berbeda di setiap subclass")
        println("   ✓ Encapsulation: Private properties, business logic di class")
        println("   ✓ Composition: Service has-a Repository")
        println("   ✓ Aggregation: CirculationService aggregate multiple services")
    }
}

// ============================================================
// ENTRY POINT
// Membuat instance AkasiaLibCLI dan menjalankan aplikasi
// ============================================================
fun main() {
    AkasiaLibCLI().run()
}