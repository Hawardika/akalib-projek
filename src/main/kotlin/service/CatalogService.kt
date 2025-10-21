package service

import model.*
import repository.Repository

// ============================================================
// OOP CONCEPT: COMPOSITION + DEPENDENCY INJECTION
// CatalogService "has-a" Repository (tidak inherit, tapi compose)
// Repository di-inject via constructor (loosely coupled)
// ============================================================

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Service layer untuk enkapsulasi business logic terkait katalog buku
// Memisahkan logic dari presentation layer (Separation of Concerns)
// ============================================================
class CatalogService(
    // ============================================================
    // OOP CONCEPT: COMPOSITION
    // Service bergantung pada Repository (Dependency)
    // Private property, hanya digunakan internal
    // ============================================================
    private val repo: Repository<Buku>
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk menambah buku ke repository
    // Business logic di-encapsulate dalam service
    // ============================================================
    fun tambahBuku(buku: Buku) {
        repo.save(buku)
        println("✅ Buku '${buku.judul}' berhasil ditambahkan ke katalog.")
    }

    fun cariBukuById(id: String): Buku? = repo.findById(id)

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Search functionality dengan business logic
    // ============================================================
    fun cariBukuByJudul(keyword: String): List<Buku> {
        return repo.findAll().filter {
            it.judul.contains(keyword, ignoreCase = true)
        }
    }

    fun cariBukuByPenulis(penulis: String): List<Buku> {
        return repo.findAll().filter {
            it.penulis.contains(penulis, ignoreCase = true)
        }
    }

    fun cariBukuByKategori(kategori: String): List<Buku> {
        return repo.findAll().filter {
            it.kategori.equals(kategori, ignoreCase = true)
        }
    }

    fun getAllBuku(): List<Buku> = repo.findAll()

    fun hapusBuku(id: String): Boolean {
        val berhasil = repo.delete(id)
        if (berhasil) {
            println("✅ Buku dengan ID '$id' berhasil dihapus.")
        } else {
            println("❌ Buku dengan ID '$id' tidak ditemukan.")
        }
        return berhasil
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION + POLYMORPHISM
    // Method untuk cek stok (hanya untuk BukuCetak)
    // Menggunakan type checking dan smart cast
    // ============================================================
    fun cekStokBukuCetak(id: String): Int {
        val buku = repo.findById(id)
        return if (buku is BukuCetak) buku.stok else -1
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Business logic untuk mengurangi stok buku cetak
    // ============================================================
    fun kurangiStok(id: String): Boolean {
        val buku = repo.findById(id)
        if (buku is BukuCetak && buku.stok > 0) {
            buku.stok--
            repo.update(id, buku)
            return true
        }
        return false
    }

    fun tambahStok(id: String): Boolean {
        val buku = repo.findById(id)
        if (buku is BukuCetak) {
            buku.stok++
            repo.update(id, buku)
            return true
        }
        return false
    }
}