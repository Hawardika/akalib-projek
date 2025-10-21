package repository

// ============================================================
// OOP CONCEPT: POLYMORPHISM (Interface Implementation)
// InMemoryRepo implements Repository interface
// Menyediakan implementasi konkret dari abstract contract
// ============================================================

// ============================================================
// OOP CONCEPT: GENERICS
// Generic class <T> yang dapat bekerja dengan tipe data apapun
// Reusability: satu class untuk semua jenis data
// ============================================================
class InMemoryRepo<T>(
    // ============================================================
    // OOP CONCEPT: COMPOSITION + ENCAPSULATION
    // Menerima lambda function untuk extract ID dari object
    // Dependency injection pattern untuk fleksibilitas
    // ============================================================
    private val idExtractor: (T) -> String
) : Repository<T> {  // Implements Repository interface

    // ============================================================
    // OOP CONCEPT: ENCAPSULATION
    // Private property untuk menyimpan data
    // Data disembunyikan dari luar, hanya diakses via methods
    // Menggunakan Map untuk fast lookup O(1)
    // ============================================================
    private val data = mutableMapOf<String, T>()

    // ============================================================
    // OOP CONCEPT: POLYMORPHISM (Method Implementation)
    // Implementasi method dari interface Repository
    // Override implicit karena implement interface
    // ============================================================

    override fun save(item: T) {
        val id = idExtractor(item)  // Extract ID dari object
        data[id] = item
    }

    override fun findAll(): List<T> = data.values.toList()

    override fun findById(id: String): T? = data[id]

    override fun update(id: String, item: T): Boolean {
        return if (data.containsKey(id)) {
            data[id] = item
            true
        } else false
    }

    override fun delete(id: String): Boolean {
        return data.remove(id) != null
    }
}