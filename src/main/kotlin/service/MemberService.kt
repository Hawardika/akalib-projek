package service

import model.Anggota
import repository.Repository

// ============================================================
// OOP CONCEPT: COMPOSITION + DEPENDENCY INJECTION
// MemberService "has-a" Repository<Anggota>
// Service layer pattern untuk business logic anggota
// ============================================================

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Enkapsulasi semua operasi terkait anggota dalam satu class
// ============================================================
class MemberService(
    // ============================================================
    // OOP CONCEPT: COMPOSITION
    // Service bergantung pada Repository (loosely coupled)
    // ============================================================
    private val repo: Repository<Anggota>
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk tambah anggota
    // ============================================================
    fun tambahAnggota(anggota: Anggota) {
        repo.save(anggota)
        println(" Anggota '${anggota.nama}' berhasil ditambahkan.")
    }

    fun cariAnggotaById(id: String): Anggota? = repo.findById(id)

    fun getAllAnggota(): List<Anggota> = repo.findAll()

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk update status anggota
    // Validasi dilakukan di service layer
    // ============================================================
    fun updateStatusAktif(id: String, status: String): Boolean {
        val anggota = repo.findById(id)
        if (anggota != null) {
            anggota.statusAktif = status
            repo.update(id, anggota)
            println(" Status anggota '$id' diubah menjadi '$status'.")
            return true
        }
        println(" Anggota dengan ID '$id' tidak ditemukan.")
        return false
    }

    fun hapusAnggota(id: String): Boolean {
        val berhasil = repo.delete(id)
        if (berhasil) {
            println(" Anggota dengan ID '$id' berhasil dihapus.")
        } else {
            println(" Anggota dengan ID '$id' tidak ditemukan.")
        }
        return berhasil
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk validasi status aktif
    // ============================================================
    fun cekStatusAktif(id: String): Boolean {
        val anggota = repo.findById(id)
        return anggota?.statusAktif?.lowercase() == "aktif"
    }
}
