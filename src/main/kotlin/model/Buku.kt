package model

// ============================================================
// OOP CONCEPT: ABSTRACTION
// Abstract class sebagai blueprint untuk semua jenis buku
// Mendefinisikan kontrak yang harus diimplementasi subclass
// ============================================================
abstract class Buku(
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties dengan visibility modifier (open) untuk diakses subclass
    // Data disembunyikan dan hanya diakses melalui struktur class
    // ============================================================
    open val id: String,
    open val judul: String,
    open val penulis: String,
    open val tahun: Int,
    open val kategori: String
) {
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Validasi data dilakukan di dalam class (data validation)
    // Memastikan integritas data saat object dibuat
    // ============================================================
    init {
        require(id.isNotBlank()) { "id buku tidak boleh kosong" }
        require(judul.isNotBlank()) { "judul tidak boleh kosong" }
        require(penulis.isNotBlank()) { "penulis tidak boleh kosong" }
        require(tahun in 1000..2100) { "tahun tidak valid" }
        require(kategori.isNotBlank()) { "kategori tidak boleh kosong" }
    }

    // ============================================================
    // OOP CONCEPT: ABSTRACTION + POLYMORPHISM
    // Abstract method yang harus diimplementasi oleh subclass
    // Setiap subclass akan memiliki implementasi berbeda (polymorphism)
    // ============================================================
    abstract fun info(): String
}

// ============================================================
// ENUM untuk tipe buku (digunakan dalam Peminjaman)
// ============================================================
enum class TipeBuku { CETAK, DIGITAL }