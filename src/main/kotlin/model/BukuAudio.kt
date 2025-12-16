package model

// ============================================================
// OOP CONCEPT: INHERITANCE + POLYMORPHISM
// BukuAudio mewarisi dari abstract class Buku
// Implementasi berbeda untuk audiobook
// ============================================================
class BukuAudio(
    // Properties dari parent class
    id: String,
    judul: String,
    penulis: String,
    tahun: Int,
    kategori: String,

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties spesifik untuk BukuAudio
    // ============================================================
    val durasi: Int,           // Durasi dalam menit
    val narator: String,       // Nama narator/voice actor
    val formatAudio: FormatAudio
) : Buku(id, judul, penulis, tahun, kategori) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Validasi data spesifik untuk buku audio
    // ============================================================
    init {
        require(durasi > 0) { "Durasi harus lebih dari 0 menit" }
        require(narator.isNotBlank()) { "Narator tidak boleh kosong" }
    }

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM (Method Overriding)
    // Override method info() dari parent class
    // Menampilkan informasi khusus untuk audiobook
    // ============================================================
    override fun info(): String {
        val jam = durasi / 60
        val menit = durasi % 60
        return "AudioBook: \"$judul\" ($tahun) oleh $penulis, " +
                "dibacakan $narator, ${jam}j ${menit}m, $formatAudio"
    }
}

// ============================================================
// ENUM untuk format audio
// Type safety untuk format audiobook
// ============================================================
enum class FormatAudio {
    MP3,
    AAC,
    FLAC,
    M4B
}