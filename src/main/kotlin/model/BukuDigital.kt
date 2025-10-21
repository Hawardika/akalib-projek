package model

// ============================================================
// ENUM untuk format digital
// Memastikan type safety (hanya PDF atau EPUB yang valid)
// ============================================================
enum class FormatDigital { PDF, EPUB }

// ============================================================
// OOP CONCEPT: INHERITANCE
// BukuDigital mewarisi dari abstract class Buku
// ============================================================
class BukuDigital(
    // Properties dari parent class
    id: String,
    judul: String,
    penulis: String,
    tahun: Int,
    kategori: String,

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties spesifik untuk BukuDigital
    // Data spesifik yang hanya dimiliki buku digital
    // ============================================================
    val ukuranFileMb: Double,
    val formatDigital: FormatDigital
) : Buku(id, judul, penulis, tahun, kategori) {  // Inheritance dari Buku

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Validasi ukuran file harus positif
    // ============================================================
    init {
        require(ukuranFileMb > 0) { "ukuranFileMb harus > 0" }
    }

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM (Method Overriding)
    // Override method info() dari parent class
    // Implementasi berbeda dari BukuCetak (polymorphic behavior)
    // Runtime akan menentukan method mana yang dipanggil
    // ============================================================
    override fun info(): String =
        "Digital: \"$judul\" ($tahun) oleh $penulis, ${"%.1f".format(ukuranFileMb)} MB, $formatDigital"
}