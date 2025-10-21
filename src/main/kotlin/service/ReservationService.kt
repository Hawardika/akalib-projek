package service

import model.Reservasi
import repository.Repository

// ============================================================
// OOP CONCEPT: COMPOSITION + DEPENDENCY INJECTION
// ReservationService "has-a" Repository<Reservasi>
// ============================================================

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Service layer untuk business logic reservasi buku
// Mengelola antrian (queue) untuk buku yang stoknya habis
// ============================================================
class ReservationService(
    // ============================================================
    // OOP CONCEPT: COMPOSITION
    // Service bergantung pada Repository (loosely coupled)
    // ============================================================
    private val repo: Repository<Reservasi>
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk membuat atau menambah reservasi
    // Jika belum ada reservasi untuk buku, buat baru
    // Jika sudah ada, tambahkan anggota ke antrian
    // ============================================================
    fun buatReservasi(idBuku: String, idAnggota: String) {
        var reservasi = repo.findById(idBuku)

        if (reservasi == null) {
            // Buat reservasi baru untuk buku ini
            reservasi = Reservasi(id = idBuku, bukuId = idBuku)
            repo.save(reservasi)
        }

        // Tambahkan anggota ke antrian
        reservasi.daftar(idAnggota)
        repo.update(idBuku, reservasi)
    }

    fun getReservasiBuku(idBuku: String): Reservasi? = repo.findById(idBuku)

    fun getAllReservasi(): List<Reservasi> = repo.findAll()

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk mengambil anggota berikutnya dari antrian
    // FIFO: First In First Out
    // ============================================================
    fun ambilAntrianBerikutnya(idBuku: String): String? {
        val reservasi = repo.findById(idBuku)
        if (reservasi != null) {
            val next = reservasi.ambilBerikutnya()
            repo.update(idBuku, reservasi)
            return next
        }
        return null
    }

    fun hapusReservasi(idBuku: String): Boolean {
        return repo.delete(idBuku)
    }
}