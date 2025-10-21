package model

// ============================================================
// OOP CONCEPT: INHERITANCE
// BukuCetak mewarisi (extends) dari abstract class Buku
// Menggunakan operator ':' untuk inheritance
// ============================================================
class BukuCetak(
    // ============================================================
    // Properties dari parent class (Buku)
    // ============================================================
    id: String,
    judul: String,
    penulis: String,
    tahun: Int,
    kategori: String,

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties spesifik untuk BukuCetak
    // val = immutable, var = mutable (controlled access)
    // ============================================================
    val jumlahHalaman: Int,
    var stok: Int  // var karena stok akan berubah saat dipinjam/dikembalikan
) : Buku(id, judul, penulis, tahun, kategori) {  // Memanggil constructor parent

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Validasi data spesifik untuk buku cetak
    // Business rule: halaman > 0, stok >= 0
    // ============================================================
    init {
        require(jumlahHalaman > 0) { "Jumlah halaman harus lebih dari 0" }
        require(stok >= 0) { "Stok tidak boleh negatif" }
    }

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM (Method Overriding)
    // Implementasi method abstract dari parent class
    // Setiap subclass punya cara berbeda untuk menampilkan info
    // Menggunakan keyword 'override' untuk mengganti implementasi parent
    // ============================================================
    override fun info(): String =
        "Buku Cetak: \"$judul\" oleh $penulis ($tahun), $jumlahHalaman halaman, stok=$stok"
}