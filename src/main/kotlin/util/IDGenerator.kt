package util

// ============================================================
// OOP CONCEPT: SINGLETON PATTERN
// Object untuk generate ID otomatis dengan format tertentu
// Thread-safe karena menggunakan Kotlin object
// ============================================================
object IDGenerator {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Private counters untuk setiap jenis entity
    // ============================================================
    private var bukuCounter = 0
    private var anggotaCounter = 0
    private var peminjamanCounter = 0
    private var reservasiCounter = 0

    // ============================================================
    // Method untuk generate ID buku dengan format BK-XXXX
    // ============================================================
    @Synchronized
    fun generateBukuID(): String {
        bukuCounter++
        return "BK-${bukuCounter.toString().padStart(4, '0')}"
    }

    // ============================================================
    // Method untuk generate ID anggota dengan format AG-XXXX
    // ============================================================
    @Synchronized
    fun generateAnggotaID(): String {
        anggotaCounter++
        return "AG-${anggotaCounter.toString().padStart(4, '0')}"
    }

    // ============================================================
    // Method untuk generate ID peminjaman dengan format PM-XXXX
    // ============================================================
    @Synchronized
    fun generatePeminjamanID(): String {
        peminjamanCounter++
        return "PM-${peminjamanCounter.toString().padStart(4, '0')}"
    }

    // ============================================================
    // Method untuk generate ID reservasi dengan format RS-XXXX
    // Utility method - bisa digunakan jika reservasi butuh unique ID
    // ============================================================
    @Suppress("unused")
    @Synchronized
    fun generateReservasiID(): String {
        reservasiCounter++
        return "RS-${reservasiCounter.toString().padStart(4, '0')}"
    }

    // ============================================================
    // Method untuk reset semua counter (untuk testing)
    // ============================================================
    @Suppress("unused")
    @Synchronized
    fun reset() {
        bukuCounter = 0
        anggotaCounter = 0
        peminjamanCounter = 0
        reservasiCounter = 0
    }

    // ============================================================
    // Method untuk set counter tertentu (untuk load dari database)
    // ============================================================
    @Suppress("unused")
    @Synchronized
    fun setBukuCounter(value: Int) {
        bukuCounter = value
    }

    @Suppress("unused")
    @Synchronized
    fun setAnggotaCounter(value: Int) {
        anggotaCounter = value
    }

    @Suppress("unused")
    @Synchronized
    fun setPeminjamanCounter(value: Int) {
        peminjamanCounter = value
    }

    @Suppress("unused")
    @Synchronized
    fun setReservasiCounter(value: Int) {
        reservasiCounter = value
    }

    // ============================================================
    // Method untuk mendapatkan current counter (untuk save ke database)
    // ============================================================
    @Suppress("unused")
    fun getCurrentCounters(): Map<String, Int> {
        return mapOf(
            "buku" to bukuCounter,
            "anggota" to anggotaCounter,
            "peminjaman" to peminjamanCounter,
            "reservasi" to reservasiCounter
        )
    }
}