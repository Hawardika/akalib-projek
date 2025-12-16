package model

import java.util.ArrayDeque

// ============================================================
// OOP CONCEPT: ENCAPSULATION + COMPOSITION
// Class untuk mengelola antrian reservasi buku
// Menggunakan ArrayDeque (Queue data structure) untuk FIFO
// ============================================================
data class Reservasi(
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Properties untuk identifikasi reservasi
    // ============================================================
    val id: String,
    val bukuId: String,

    // ============================================================
    // OOP CONCEPT: COMPOSITION
    // Reservasi "has-a" ArrayDeque (Queue)
    // Menggunakan data structure untuk manage antrian
    // Private by default, hanya diakses via methods
    // ============================================================
    val antrian: ArrayDeque<String> = ArrayDeque()
) {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk menambahkan anggota ke antrian
    // Business logic: cek duplikasi sebelum menambah
    // ============================================================
    fun daftar(anggotaId: String) {
        if (!antrian.contains(anggotaId)) {
            antrian.addLast(anggotaId)  // Tambah di belakang (FIFO)
            println(" Anggota $anggotaId berhasil didaftarkan ke antrian buku $bukuId.")
        } else {
            println(" Anggota $anggotaId sudah ada dalam antrian buku $bukuId.")
        }
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk melihat anggota berikutnya (tanpa menghapus)
    // Peek operation
    // ============================================================
    fun next(): String? {
        val berikutnya: String? = antrian.firstOrNull()
        if (berikutnya != null) {
            println(" Anggota berikutnya dalam antrian buku $bukuId adalah: $berikutnya")
        } else {
            println(" Antrian buku $bukuId kosong.")
        }
        return berikutnya
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk mengambil dan menghapus anggota pertama
    // Dequeue operation (FIFO)
    // ============================================================
    fun ambilBerikutnya(): String? {
        if (antrian.isEmpty()) {
            println(" Tidak ada anggota yang bisa diambil dari antrian buku $bukuId.")
            return null
        } else {
            val diambil: String = antrian.removeFirst()
            println(" Anggota $diambil telah diambil dari antrian buku $bukuId.")
            return diambil
        }
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk mengecek status antrian
    // ============================================================
    fun kosong(): Boolean {
        val statusKosong: Boolean = antrian.isEmpty()
        println(" Status antrian buku $bukuId kosong: $statusKosong")
        return statusKosong
    }

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk menghitung jumlah anggota dalam antrian
    // ============================================================
    fun jumlah(): Int {
        val total: Int = antrian.size
        println(" Jumlah anggota dalam antrian buku $bukuId: $total")
        return total
    }
}