package auth

// ============================================================
// OOP CONCEPT: ENUM untuk role
// Encapsulation untuk jenis-jenis user
// ============================================================
enum class Role(val permissions: Set<Permission>) {
    ADMIN(setOf(
        Permission.TAMBAH_BUKU,
        Permission.HAPUS_BUKU,
        Permission.TAMBAH_ANGGOTA,
        Permission.HAPUS_ANGGOTA,
        Permission.PINJAM_BUKU,
        Permission.KEMBALI_BUKU,
        Permission.RESERVASI,
        Permission.LIHAT_LAPORAN,
        Permission.EKSPOR_CSV
    )),
    PUSTAKAWAN(setOf(
        Permission.PINJAM_BUKU,
        Permission.KEMBALI_BUKU,
        Permission.RESERVASI,
        Permission.LIHAT_LAPORAN
    ))
}

// ============================================================
// OOP CONCEPT: ENUM untuk permission
// Encapsulation untuk hak akses
// ============================================================
enum class Permission {
    TAMBAH_BUKU,
    HAPUS_BUKU,
    TAMBAH_ANGGOTA,
    HAPUS_ANGGOTA,
    PINJAM_BUKU,
    KEMBALI_BUKU,
    RESERVASI,
    LIHAT_LAPORAN,
    EKSPOR_CSV
}

// ============================================================
// OOP CONCEPT: DATA CLASS
// Model untuk user dengan encapsulation
// ============================================================
data class User(
    val username: String,
    val password: String,  // NOTE: Di production harus di-hash!
    val role: Role,
    val nama: String
) {
    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Method untuk check permission
    // ============================================================
    fun hasPermission(permission: Permission): Boolean {
        return role.permissions.contains(permission)
    }
}

// ============================================================
// OOP CONCEPT: SINGLETON PATTERN
// Object untuk manage authentication state
// ============================================================
object AuthManager {

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Private property untuk current user
    // ============================================================
    private var currentUser: User? = null

    // ============================================================
    // Hard-coded users (untuk demo)
    // Di production: load dari database/file
    // ============================================================
    private val users = mutableMapOf(
        "admin" to User("admin", "admin123", Role.ADMIN, "Administrator"),
        "pustakawan" to User("pustakawan", "pustaka123", Role.PUSTAKAWAN, "Petugas Pustaka")
    )

    // ============================================================
    // Method untuk login
    // ============================================================
    fun login(username: String, password: String): Boolean {
        val user = users[username]

        return if (user != null && user.password == password) {
            currentUser = user
            println("\n Login berhasil!")
            println(" Selamat datang, ${user.nama} (${user.role})")
            true
        } else {
            println("\n Username atau password salah!")
            false
        }
    }

    // ============================================================
    // Method untuk logout
    // ============================================================
    fun logout() {
        if (currentUser != null) {
            println("\n ${currentUser?.nama} telah logout.")
            currentUser = null
        }
    }

    // ============================================================
    // Method untuk check apakah user sudah login
    // ============================================================
    fun isLoggedIn(): Boolean = currentUser != null

    // ============================================================
    // Method untuk get current user
    // ============================================================
    fun getCurrentUser(): User? = currentUser

    // ============================================================
    // Method untuk check permission
    // ============================================================
    fun hasPermission(permission: Permission): Boolean {
        return currentUser?.hasPermission(permission) ?: false
    }

    // ============================================================
    // Method untuk tambah user baru (hanya admin)
    // Utility method untuk future feature
    // ============================================================
    @Suppress("unused")
    fun registerUser(username: String, password: String, role: Role, nama: String): Boolean {
        if (!hasPermission(Permission.TAMBAH_ANGGOTA)) {
            println(" Anda tidak memiliki izin untuk menambah user!")
            return false
        }

        if (users.containsKey(username)) {
            println(" Username sudah digunakan!")
            return false
        }

        users[username] = User(username, password, role, nama)
        println(" User '$username' berhasil ditambahkan!")
        return true
    }

    // ============================================================
    // Method untuk mendapatkan info user saat ini
    // ============================================================
    fun getCurrentUserInfo(): String {
        val user = currentUser ?: return "Belum login"
        return "${user.nama} (${user.role})"
    }
}