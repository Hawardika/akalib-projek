package model

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Enum dengan properties untuk mengenkapsulasi aturan bisnis
// Setiap tier memiliki batas pinjam dan durasi berbeda
// ============================================================
enum class Tier(
    val maxLoans: Int,     // Maksimal buku yang bisa dipinjam
    val loanDays: Int      // Durasi peminjaman (hari)
) {
    REGULAR(2, 7),         // 2 buku, 7 hari
    PREMIUM(3, 14),        // 3 buku, 14 hari
    STAFF(5, 21)           // 5 buku, 21 hari
}

// ============================================================
// OOP CONCEPT: ENCAPSULATION
// Data class untuk encapsulate data anggota
// Menggunakan data class untuk otomatis generate equals(), hashCode(), toString()
// ============================================================
data class Anggota(
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties dengan visibility control
    // val = immutable (tidak bisa diubah setelah dibuat)
    // var = mutable (bisa diubah, tapi tetap controlled)
    // ============================================================
    val id: String,
    val nama: String,
    val tier: Tier,           // Composition: Anggota "has-a" Tier
    var statusAktif: String   // var karena status bisa berubah
) {
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Validasi data di dalam class (information hiding)
    // Memastikan data valid saat object creation
    // ============================================================
    init {
        require(id.isNotBlank()) { "id anggota tidak boleh kosong" }
        require(nama.isNotBlank()) { "nama tidak boleh kosong" }
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk menampilkan info anggota
    // Behavior yang terikat dengan data
    // ============================================================
//    fun anggota() {
//        println("Anggota bernama $nama dengan id $id memiliki tier $tier dan status aktif: $statusAktif")
//    }
}