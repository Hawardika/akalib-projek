import auth.*
import model.*
import repository.*
import service.*
import util.IDGenerator
import java.time.LocalDate
import java.util.*

// ============================================================
// OOP CONCEPT: ENCAPSULATION + COMPOSITION + AGGREGATION
// AkasiaLibCLI dengan fitur tambahan:
// 1. Strategy Pattern untuk perhitungan denda
// 2. BukuAudio dengan aturan berbeda
// 3. Ekspor CSV
// 4. Auto-number ID
// 5. Sistem login Admin/Pustakawan
// ============================================================
class AkasiaLibCLI {
    private val scanner = Scanner(System.`in`)

    // Repositories
    private val bukuRepo = InMemoryRepo<Buku> { it.id }
    private val anggotaRepo = InMemoryRepo<Anggota> { it.id }
    private val peminjamanRepo = InMemoryRepo<Peminjaman> { it.id }
    private val reservasiRepo = InMemoryRepo<Reservasi> { it.id }

    // Services
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
    // FITUR BARU: CSV Exporter
    // ============================================================
    private val csvExporter = CSVExporter(
        catalogService,
        memberService,
        circulationService,
        reservationService
    )

    // ============================================================
    // FITUR BARU: Strategy Pattern untuk Denda
    // Default menggunakan DendaStandar
    // ============================================================
    private var strategiDenda: StrategiDenda = DendaStandar()

    fun run() {
        println("======================================")
        println("+     Selamat Datang di AkasiaLib    +")
        println("======================================")

        // ============================================================
        // FITUR BARU: Login System
        // ============================================================
        if (!loginMenu()) {
            println(" Terima kasih!")
            return
        }

        println("\nKetik 'help' untuk melihat daftar perintah.\n")

        while (true) {
            print("AkasiaLib [${AuthManager.getCurrentUserInfo()}]> ")
            val input = scanner.nextLine().trim().lowercase()

            when (input) {
                "help" -> showHelp()
                "tambahbuku" -> checkPermissionAndExecute(Permission.TAMBAH_BUKU) { tambahBuku() }
                "listbuku" -> listBuku()
                "caribuku" -> cariBuku()
                "cekstok" -> cekStok()
                "kelolabuku" -> checkPermissionAndExecute(Permission.TAMBAH_BUKU) { kelolaBuku() }
                "tambahanggota" -> checkPermissionAndExecute(Permission.TAMBAH_ANGGOTA) { tambahAnggota() }
                "listanggota" -> listAnggota()
                "kelolaanggota" -> checkPermissionAndExecute(Permission.TAMBAH_ANGGOTA) { kelolaAnggota() }
                "pinjam" -> checkPermissionAndExecute(Permission.PINJAM_BUKU) { pinjamBuku() }
                "kembali" -> checkPermissionAndExecute(Permission.KEMBALI_BUKU) { kembalikanBuku() }
                "reservasi" -> checkPermissionAndExecute(Permission.RESERVASI) { buatReservasi() }
                "kelolareservasi" -> checkPermissionAndExecute(Permission.RESERVASI) { kelolaReservasi() }
                "laporan" -> checkPermissionAndExecute(Permission.LIHAT_LAPORAN) { tampilkanLaporan() }
                "cekdenda" -> checkPermissionAndExecute(Permission.LIHAT_LAPORAN) { cekDenda() }
                "eksporcsv" -> checkPermissionAndExecute(Permission.EKSPOR_CSV) { eksporCSV() }
                "setdenda" -> checkPermissionAndExecute(Permission.TAMBAH_BUKU) { setStrategiDenda() }
                "demo" -> runDemo()
                "logout" -> {
                    AuthManager.logout()
                    if (!loginMenu()) break
                }
                "exit", "quit" -> {
                    println("\n Terima kasih telah menggunakan AkasiaLib!")
                    break
                }
                else -> println(" Perintah tidak dikenal. Ketik 'help' untuk bantuan.")
            }
            println()
        }
    }

    // ============================================================
    // FITUR BARU: Login Menu
    // ============================================================
    private fun loginMenu(): Boolean {
        println("\n========================================")
        println("+           LOGIN AKASILIB             +")
        println("========================================")

        // Cek apakah sudah login
        if (AuthManager.isLoggedIn()) {
            println("\n Anda sudah login sebagai ${AuthManager.getCurrentUserInfo()}")
            print("Logout dan login ulang? (y/n): ")
            if (scanner.nextLine().trim().lowercase() != "y") {
                return true
            }
            AuthManager.logout()
        }

        println("\n Default credentials:")
        println("   Admin: username='admin', password='admin123'")
        println("   Pustakawan: username='pustakawan', password='pustaka123'\n")

        var attempts = 0
        while (attempts < 3) {
            print("Username: ")
            val username = scanner.nextLine().trim()

            if (username.lowercase() == "exit") return false

            print("Password: ")
            val password = scanner.nextLine().trim()

            if (AuthManager.login(username, password)) {
                return true
            }

            attempts++
            if (attempts < 3) {
                println("Silakan coba lagi (${3 - attempts} percobaan tersisa)")
            }
        }

        println("\n Terlalu banyak percobaan gagal!")
        return false
    }

    // ============================================================
    // FITUR BARU: Check Permission Helper
    // ============================================================
    private fun checkPermissionAndExecute(permission: Permission, action: () -> Unit) {
        if (AuthManager.hasPermission(permission)) {
            action()
        } else {
            println(" Anda tidak memiliki izin untuk melakukan aksi ini!")
            println(" Izin diperlukan: $permission")
        }
    }

    private fun showHelp() {
        val user = AuthManager.getCurrentUser()
        println("""
        ================================================================
        +                    DAFTAR PERINTAH                           +
        +              User: ${user?.nama ?: "Guest"} (${user?.role ?: "-"})
        ================================================================
        """.trimIndent())

        // Tampilkan perintah sesuai permission
        if (AuthManager.hasPermission(Permission.TAMBAH_BUKU)) {
            println("+  tambahBuku      : Tambah buku baru ke katalog               +")
            println("+  kelolaBuku      : Update/hapus buku                         +")
        }
        println("+  listBuku        : Tampilkan semua buku                      +")
        println("+  cariBuku        : Cari buku (judul/penulis/kategori)        +")
        println("+  cekStok         : Cek stok buku cetak                       +")

        if (AuthManager.hasPermission(Permission.TAMBAH_ANGGOTA)) {
            println("+  tambahAnggota   : Tambah anggota baru                       +")
        }
        println("+  listAnggota     : Tampilkan semua anggota                   +")

        if (AuthManager.hasPermission(Permission.TAMBAH_ANGGOTA)) {
            println("+  kelolaaAnggota  : Update/hapus anggota                      +")
        }

        if (AuthManager.hasPermission(Permission.PINJAM_BUKU)) {
            println("+  pinjam          : Pinjam buku                               +")
        }
        if (AuthManager.hasPermission(Permission.KEMBALI_BUKU)) {
            println("+  kembali         : Kembalikan buku                           +")
        }
        if (AuthManager.hasPermission(Permission.RESERVASI)) {
            println("+  reservasi       : Buat reservasi buku                       +")
            println("+  kelolaReservasi : Kelola antrian reservasi                  +")
        }
        if (AuthManager.hasPermission(Permission.LIHAT_LAPORAN)) {
            println("+  laporan         : Tampilkan laporan aktivitas               +")
            println("+  cekDenda        : Cek perhitungan denda                     +")
        }
        if (AuthManager.hasPermission(Permission.EKSPOR_CSV)) {
            println("+  eksporCSV       : Ekspor laporan ke CSV                     +")
        }
        if (AuthManager.hasPermission(Permission.TAMBAH_BUKU)) {
            println("+  setDenda        : Ubah strategi perhitungan denda           +")
        }
        println("+  demo            : Jalankan skenario demo otomatis           +")
        println("+  logout          : Logout dan ganti user                     +")
        println("+  exit / quit     : Keluar aplikasi                           +")
        println("================================================================")
    }

    // ============================================================
    // FITUR BARU: Set Strategi Denda
    // ============================================================
    private fun setStrategiDenda() {
        println("\n === PILIH STRATEGI PERHITUNGAN DENDA ===")
        println("1. Standar (Rp 1000/hari)")
        println("2. Progresif (1-3 hari: Rp1000, 4-7: Rp2000, >7: Rp3000)")
        println("3. Weekend (Tidak kena denda di weekend)")

        print("\nPilihan (1-3): ")
        when (scanner.nextLine().trim()) {
            "1" -> {
                strategiDenda = DendaStandar()
                circulationService.setStrategiDenda(strategiDenda)
                println(" Strategi denda diubah ke: STANDAR")
            }
            "2" -> {
                strategiDenda = DendaProgresif()
                circulationService.setStrategiDenda(strategiDenda)
                println(" Strategi denda diubah ke: PROGRESIF")
            }
            "3" -> {
                strategiDenda = DendaWeekend()
                circulationService.setStrategiDenda(strategiDenda)
                println(" Strategi denda diubah ke: WEEKEND")
            }
            else -> println(" Pilihan tidak valid")
        }
    }

    // ============================================================
    // FITUR BARU: Ekspor CSV
    // ============================================================
    private fun eksporCSV() {
        println("\n === EKSPOR LAPORAN KE CSV ===")
        print("Direktori tujuan (Enter untuk direktori saat ini): ")
        val direktori = scanner.nextLine().trim().ifEmpty { "." }

        println("\n Mengekspor laporan...")
        val hasil = csvExporter.eksporSemuaLaporan(direktori)

        println("\n Ekspor selesai! File yang dibuat:")
        hasil.forEach { (jenis, path) ->
            println("    $jenis: $path")
        }
    }

    private fun tambahBuku() {
        println("\n === TAMBAH BUKU BARU ===")

        // ============================================================
        // FITUR BARU: Auto-generate ID
        // ============================================================
        print("Generate ID otomatis? (y/n): ")
        val autoId = scanner.nextLine().trim().lowercase() == "y"

        val id = if (autoId) {
            val generatedId = IDGenerator.generateBukuID()
            println(" ID otomatis: $generatedId")
            generatedId
        } else {
            print("ID Buku: ")
            scanner.nextLine().trim()
        }

        print("Judul: ")
        val judul = scanner.nextLine().trim()

        print("Penulis: ")
        val penulis = scanner.nextLine().trim()

        print("Tahun: ")
        val tahun = scanner.nextLine().toIntOrNull() ?: 2024

        print("Kategori: ")
        val kategori = scanner.nextLine().trim()

        // ============================================================
        // FITUR BARU: Pilihan untuk BukuAudio
        // ============================================================
        print("Tipe (1=Cetak, 2=Digital, 3=Audio): ")
        val tipe = scanner.nextLine().trim()

        try {
            when (tipe) {
                "1" -> {
                    print("Jumlah Halaman: ")
                    val halaman = scanner.nextLine().toInt()

                    print("Stok: ")
                    val stok = scanner.nextLine().toInt()

                    val buku = BukuCetak(id, judul, penulis, tahun, kategori, halaman, stok)
                    catalogService.tambahBuku(buku)
                    println("\n${buku.info()}")
                }
                "2" -> {
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
                "3" -> {
                    // ============================================================
                    // FITUR BARU: Tambah BukuAudio
                    // ============================================================
                    print("Durasi (menit): ")
                    val durasi = scanner.nextLine().toInt()

                    print("Narator: ")
                    val narator = scanner.nextLine().trim()

                    print("Format (MP3/AAC/FLAC/M4B): ")
                    val format = scanner.nextLine().uppercase()

                    val buku = BukuAudio(
                        id, judul, penulis, tahun, kategori,
                        durasi, narator, FormatAudio.valueOf(format)
                    )
                    catalogService.tambahBuku(buku)
                    println("\n${buku.info()}")
                }
                else -> println(" Tipe tidak valid")
            }
        } catch (e: Exception) {
            println(" Error: ${e.message}")
        }
    }

    private fun listBuku() {
        println("\n === DAFTAR BUKU ===")
        val bukuList = catalogService.getAllBuku()

        if (bukuList.isEmpty()) {
            println("Belum ada buku dalam katalog.")
            return
        }

        bukuList.forEachIndexed { index, buku ->
            println("${index + 1}. ${buku.info()}")
        }
    }

    // ============================================================
    // FITUR BARU: Cari Buku
    // ============================================================
    private fun cariBuku() {
        println("\n === CARI BUKU ===")
        println("1. Cari berdasarkan Judul")
        println("2. Cari berdasarkan Penulis")
        println("3. Cari berdasarkan Kategori")

        print("\nPilihan (1-3): ")
        val pilihan = scanner.nextLine().trim()

        print("Kata kunci: ")
        val keyword = scanner.nextLine().trim()

        if (keyword.isEmpty()) {
            println(" Kata kunci tidak boleh kosong!")
            return
        }

        val hasil = when (pilihan) {
            "1" -> catalogService.cariBukuByJudul(keyword)
            "2" -> catalogService.cariBukuByPenulis(keyword)
            "3" -> catalogService.cariBukuByKategori(keyword)
            else -> {
                println(" Pilihan tidak valid!")
                return
            }
        }

        if (hasil.isEmpty()) {
            println("\n Tidak ada buku yang ditemukan dengan kata kunci '$keyword'")
        } else {
            println("\n Ditemukan ${hasil.size} buku:")
            hasil.forEachIndexed { index, buku ->
                println("${index + 1}. ${buku.info()}")
            }
        }
    }

    private fun tambahAnggota() {
        println("\n === TAMBAH ANGGOTA BARU ===")

        // ============================================================
        // FITUR BARU: Auto-generate ID
        // ============================================================
        print("Generate ID otomatis? (y/n): ")
        val autoId = scanner.nextLine().trim().lowercase() == "y"

        val id = if (autoId) {
            val generatedId = IDGenerator.generateAnggotaID()
            println(" ID otomatis: $generatedId")
            generatedId
        } else {
            print("ID Anggota: ")
            scanner.nextLine().trim()
        }

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
            println(" Error: ${e.message}")
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
    // FITUR BARU: Kelola Anggota
    // ============================================================
    private fun kelolaAnggota() {
        println("\n === KELOLA ANGGOTA ===")
        println("1. Update Status Anggota")
        println("2. Hapus Anggota")

        print("\nPilihan (1-2): ")
        val pilihan = scanner.nextLine().trim()

        when (pilihan) {
            "1" -> {
                print("ID Anggota: ")
                val id = scanner.nextLine().trim()

                print("Status baru (aktif/nonaktif): ")
                val status = scanner.nextLine().trim()

                memberService.updateStatusAktif(id, status)
            }
            "2" -> {
                print("ID Anggota yang akan dihapus: ")
                val id = scanner.nextLine().trim()

                print("Yakin ingin menghapus? (y/n): ")
                if (scanner.nextLine().trim().lowercase() == "y") {
                    memberService.hapusAnggota(id)
                } else {
                    println(" Pembatalan penghapusan")
                }
            }
            else -> println(" Pilihan tidak valid!")
        }
    }

    // ============================================================
    // FITUR BARU: Kelola Reservasi
    // ============================================================
    private fun kelolaReservasi() {
        println("\n === KELOLA RESERVASI ===")
        println("1. Ambil Anggota Berikutnya dari Antrian")
        println("2. Hapus Reservasi Buku")

        print("\nPilihan (1-2): ")
        val pilihan = scanner.nextLine().trim()

        when (pilihan) {
            "1" -> {
                print("ID Buku: ")
                val idBuku = scanner.nextLine().trim()

                val nextAnggota = reservationService.ambilAntrianBerikutnya(idBuku)
                if (nextAnggota != null) {
                    println(" Anggota berikutnya: $nextAnggota")
                    println(" Silakan proses peminjaman untuk anggota ini")
                }
            }
            "2" -> {
                print("ID Buku: ")
                val idBuku = scanner.nextLine().trim()

                print("Yakin ingin menghapus semua reservasi buku ini? (y/n): ")
                if (scanner.nextLine().trim().lowercase() == "y") {
                    if (reservationService.hapusReservasi(idBuku)) {
                        println(" Reservasi berhasil dihapus")
                    } else {
                        println(" Reservasi tidak ditemukan")
                    }
                } else {
                    println(" Pembatalan penghapusan")
                }
            }
            else ->             println(" Pilihan tidak valid!")
        }
    }

    // ============================================================
    // FITUR BARU: Cek Stok Buku
    // ============================================================
    private fun cekStok() {
        println("\n === CEK STOK BUKU CETAK ===")

        print("ID Buku: ")
        val idBuku = scanner.nextLine().trim()

        val buku = catalogService.cariBukuById(idBuku)

        if (buku == null) {
            println(" Buku tidak ditemukan!")
            return
        }

        println("\n Detail Buku:")
        println(buku.info())

        if (buku is BukuCetak) {
            val stok = catalogService.cekStokBukuCetak(idBuku)
            println("\n Stok Tersedia: $stok eksemplar")

            if (stok == 0) {
                println(" Stok habis! Silakan buat reservasi.")
            } else if (stok <= 2) {
                println(" Stok terbatas!")
            }
        } else {
            println("\n Buku digital/audio - tidak ada batasan stok")
        }
    }

    // ============================================================
    // FITUR BARU: Kelola Buku
    // ============================================================
    private fun kelolaBuku() {
        println("\n === KELOLA BUKU ===")
        println("1. Hapus Buku")

        print("\nPilihan: ")
        val pilihan = scanner.nextLine().trim()

        when (pilihan) {
            "1" -> {
                print("ID Buku yang akan dihapus: ")
                val id = scanner.nextLine().trim()

                val buku = catalogService.cariBukuById(id)
                if (buku != null) {
                    println("\nBuku yang akan dihapus:")
                    println(buku.info())

                    print("\nYakin ingin menghapus? (y/n): ")
                    if (scanner.nextLine().trim().lowercase() == "y") {
                        catalogService.hapusBuku(id)
                    } else {
                        println(" Pembatalan penghapusan")
                    }
                } else {
                    println(" Buku tidak ditemukan!")
                }
            }
            else -> println(" Pilihan tidak valid!")
        }
    }

    private fun pinjamBuku() {
        println("\n === PEMINJAMAN BUKU ===")

        // ============================================================
        // FITUR BARU: Auto-generate ID
        // ============================================================
        print("Generate ID peminjaman otomatis? (y/n): ")
        val autoId = scanner.nextLine().trim().lowercase() == "y"

        val idPeminjaman = if (autoId) {
            val generatedId = IDGenerator.generatePeminjamanID()
            println(" ID otomatis: $generatedId")
            generatedId
        } else {
            print("ID Peminjaman: ")
            scanner.nextLine().trim()
        }

        print("ID Anggota: ")
        val idAnggota = scanner.nextLine().trim()

        print("ID Buku: ")
        val idBuku = scanner.nextLine().trim()

        circulationService.pinjamBuku(idPeminjaman, idAnggota, idBuku)
    }

    private fun kembalikanBuku() {
        println("\n === PENGEMBALIAN BUKU ===")

        print("ID Peminjaman: ")
        val idPeminjaman = scanner.nextLine().trim()

        print("Tanggal Kembali (yyyy-mm-dd) atau tekan Enter untuk hari ini: ")
        val tanggalInput = scanner.nextLine().trim()

        val tanggalKembali = if (tanggalInput.isEmpty()) {
            LocalDate.now()
        } else {
            LocalDate.parse(tanggalInput)
        }

        // ============================================================
        // FITUR BARU: Gunakan Strategy Pattern untuk denda
        // ============================================================
        val peminjaman = circulationService.getAllPeminjaman()
            .find { it.id == idPeminjaman }

        if (peminjaman != null && peminjaman.tipeBuku == TipeBuku.CETAK) {
            val hariTerlambat = peminjaman.hariTerlambat(tanggalKembali)
            if (hariTerlambat > 0) {
                val denda = strategiDenda.hitung(hariTerlambat)
                println(" Hari terlambat: $hariTerlambat hari")
                println(" Denda (strategi ${strategiDenda.javaClass.simpleName}): Rp$denda")
            }
        }

        circulationService.kembalikanBuku(idPeminjaman, tanggalKembali)
    }

    private fun buatReservasi() {
        println("\n === BUAT RESERVASI ===")

        print("ID Buku: ")
        val idBuku = scanner.nextLine().trim()

        print("ID Anggota: ")
        val idAnggota = scanner.nextLine().trim()

        reservationService.buatReservasi(idBuku, idAnggota)
    }

    private fun tampilkanLaporan() {
        println("\n" + "=".repeat(60))
        println(" LAPORAN AKTIVITAS PERPUSTAKAAN")
        println("=".repeat(60))

        // Total Denda
        val totalDenda = circulationService.getTotalDenda()
        println("\n Total Denda Terkumpul: Rp$totalDenda")
        println("   (Strategi: ${circulationService.getStrategiDenda().javaClass.simpleName})")

        // Top 3 Buku
        val top3 = circulationService.getTop3BukuTerpopuler()
        println("\n Top 3 Buku Paling Sering Dipinjam:")
        if (top3.isEmpty()) {
            println("   Belum ada data peminjaman.")
        } else {
            top3.forEachIndexed { index, (bukuId, count) ->
                val buku = catalogService.cariBukuById(bukuId)
                println("   ${index + 1}. ${buku?.judul ?: bukuId} ($count kali)")
            }
        }

        // Pinjaman Aktif
        val pinjamanAktif = circulationService.getAllPeminjaman().filter { it.aktif() }
        println("\n Daftar Peminjaman Aktif: ${pinjamanAktif.size} peminjaman")

        // Statistik by Tipe Buku
        val statsByTipe = circulationService.getStatistikByTipeBuku()
        println("\n Statistik Peminjaman by Tipe Buku:")
        statsByTipe.forEach { (tipe, count) ->
            println("   - $tipe: $count peminjaman")
        }

        // Top Penunggak Denda
        val topPenunggak = circulationService.getTopPenunggakDenda(3)
        if (topPenunggak.isNotEmpty()) {
            println("\n Top 3 Penunggak Denda:")
            topPenunggak.forEachIndexed { index, (anggotaId, denda) ->
                val anggota = memberService.cariAnggotaById(anggotaId)
                println("   ${index + 1}. ${anggota?.nama ?: anggotaId}: Rp$denda")
            }
        }

        // Reservasi
        val reservasiList = reservationService.getAllReservasi()
        println("\n Daftar Reservasi:")
        if (reservasiList.isEmpty()) {
            println("   Tidak ada reservasi aktif.")
        } else {
            reservasiList.forEach { reservasi ->
                val buku = catalogService.cariBukuById(reservasi.bukuId)
                println("    ${buku?.judul ?: reservasi.bukuId}: ${reservasi.antrian.size} anggota dalam antrian")
            }
        }

        println("=".repeat(60))
    }

    // ============================================================
    // FITUR BARU: Cek Perhitungan Denda
    // ============================================================
    private fun cekDenda() {
        println("\n === CEK PERHITUNGAN DENDA ===")

        print("ID Peminjaman: ")
        val idPeminjaman = scanner.nextLine().trim()

        val peminjaman = circulationService.getAllPeminjaman()
            .find { it.id == idPeminjaman }

        if (peminjaman == null) {
            println(" Peminjaman tidak ditemukan!")
            return
        }

        val buku = catalogService.cariBukuById(peminjaman.bukuId)
        val anggota = memberService.cariAnggotaById(peminjaman.anggotaId)

        println("\n Detail Peminjaman:")
        println("   Buku: ${buku?.judul ?: peminjaman.bukuId}")
        println("   Anggota: ${anggota?.nama ?: peminjaman.anggotaId}")
        println("   Tanggal Pinjam: ${peminjaman.tanggalPinjam}")
        println("   Jatuh Tempo: ${peminjaman.jatuhTempo}")
        println("   Tanggal Kembali: ${peminjaman.tanggalKembali ?: "Belum dikembalikan"}")
        println("   Tipe Buku: ${peminjaman.tipeBuku}")

        if (peminjaman.tipeBuku == TipeBuku.CETAK) {
            val tanggalCek = peminjaman.tanggalKembali ?: java.time.LocalDate.now()
            val hariTerlambat = peminjaman.hariTerlambat(tanggalCek)

            println("\n Hari Terlambat: $hariTerlambat hari")

            if (hariTerlambat > 0) {
                println("\n💸 Perhitungan Denda:")

                // Hitung dengan strategi aktif
                val dendaAktif = circulationService.simulasiDenda(hariTerlambat)
                println("   Strategi Aktif (${circulationService.getStrategiDenda().javaClass.simpleName}): Rp$dendaAktif")

                // Bandingkan semua strategi
                println("\n   Perbandingan Semua Strategi:")
                val perbandingan = circulationService.bandingkanStrategiDenda(hariTerlambat)
                perbandingan.forEach { (nama, denda) ->
                    val marker = if (nama == circulationService.getStrategiDenda().javaClass.simpleName.replace("Denda", "")) "👉" else "  "
                    println("   $marker $nama: Rp$denda")
                }

                // Denda aktual yang tercatat
                if (peminjaman.denda > 0) {
                    println("\n    Denda Tercatat: Rp${peminjaman.denda}")
                }
            } else {
                println("\n Tidak ada keterlambatan, tidak ada denda!")
            }
        } else {
            println("\n Buku digital/audio tidak dikenakan denda")
        }
    }

    private fun runDemo() {
        println("\n === MENJALANKAN SKENARIO DEMO ===\n")

        println("1 Menambahkan 6 buku (3 cetak, 2 digital, 1 audio)...")

        val buku1 = BukuCetak("BK001", "Pemrograman Kotlin", "John Doe", 2023, "Programming", 350, 2)
        val buku2 = BukuCetak("BK002", "Algoritma Dasar", "Jane Smith", 2022, "Programming", 280, 1)
        val buku3 = BukuCetak("BK003", "Database MySQL", "Bob Johnson", 2024, "Database", 420, 0)
        val buku4 = BukuDigital("BK004", "Web Development", "Alice Brown", 2023, "Programming", 15.5, FormatDigital.PDF)
        val buku5 = BukuDigital("BK005", "Machine Learning", "Charlie Wilson", 2024, "AI", 22.3, FormatDigital.EPUB)

        // ============================================================
        // FITUR BARU: Demo BukuAudio
        // ============================================================
        val buku6 = BukuAudio("BK006", "Clean Code", "Robert Martin", 2008, "Programming", 780, "David Johnson", FormatAudio.M4B)

        listOf(buku1, buku2, buku3, buku4, buku5, buku6).forEach { catalogService.tambahBuku(it) }

        println("\n2 Menambahkan 3 anggota dengan tier berbeda...")
        val anggota1 = Anggota("AG001", "Budi Santoso", Tier.REGULAR, "aktif")
        val anggota2 = Anggota("AG002", "Siti Nurhaliza", Tier.PREMIUM, "aktif")
        val anggota3 = Anggota("AG003", "Ahmad Pustakawan", Tier.STAFF, "aktif")

        listOf(anggota1, anggota2, anggota3).forEach { memberService.tambahAnggota(it) }

        println("\n3 Testing berbagai strategi denda...")
        println("\n    Strategi 1: Denda Standar")
        strategiDenda = DendaStandar()
        circulationService.setStrategiDenda(strategiDenda)
        println("      5 hari terlambat = Rp${circulationService.simulasiDenda(5)}")

        println("\n    Strategi 2: Denda Progresif")
        strategiDenda = DendaProgresif()
        circulationService.setStrategiDenda(strategiDenda)
        println("      5 hari terlambat = Rp${circulationService.simulasiDenda(5)}")
        println("      10 hari terlambat = Rp${circulationService.simulasiDenda(10)}")

        println("\n    Strategi 3: Denda Weekend")
        strategiDenda = DendaWeekend()
        circulationService.setStrategiDenda(strategiDenda)
        println("      7 hari terlambat = Rp${circulationService.simulasiDenda(7)} (weekend excluded)")

        println("\n    Perbandingan Semua Strategi (10 hari):")
        val perbandingan = circulationService.bandingkanStrategiDenda(10)
        perbandingan.forEach { (nama, denda) ->
            println("      - $nama: Rp$denda")
        }

        println("\n4 REGULAR (Budi) pinjam buku cetak dan audio...")
        circulationService.pinjamBuku("PM001", "AG001", "BK001")
        circulationService.pinjamBuku("PM002", "AG001", "BK006")  // Audio book

        println("\n5 PREMIUM (Siti) pinjam buku digital...")
        circulationService.pinjamBuku("PM003", "AG002", "BK004")

        println("\n6 Testing auto-generate ID...")
        println("   Generated IDs:")
        println("   - Buku: ${IDGenerator.generateBukuID()}")
        println("   - Anggota: ${IDGenerator.generateAnggotaID()}")
        println("   - Peminjaman: ${IDGenerator.generatePeminjamanID()}")

        println("\n7 Menampilkan Laporan...")
        tampilkanLaporan()

        println("\n Demo selesai!")
        println("\n FITUR BARU YANG DIDEMONSTRASIKAN:")
        println("    Strategy Pattern: 3 strategi perhitungan denda berbeda")
        println("    BukuAudio: Inheritance dengan aturan peminjaman khusus")
        println("    Auto-generate ID: Format BK-XXXX, AG-XXXX, PM-XXXX")
        println("    Sistem Login: Role-based access (Admin vs Pustakawan)")
        println("    Ekspor CSV: Laporan dalam format CSV")
    }
}

fun main() {
    AkasiaLibCLI().run()
}