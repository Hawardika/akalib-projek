package repository

// ============================================================
// OOP CONCEPT: ABSTRACTION + POLYMORPHISM
// Interface sebagai kontrak untuk semua repository
// Mendefinisikan operasi CRUD tanpa implementasi detail
// ============================================================

// ============================================================
// OOP CONCEPT: GENERICS (Advanced OOP)
// Generic type <T> membuat interface reusable untuk berbagai tipe
// Contoh: Repository<Buku>, Repository<Anggota>, dll
// ============================================================
interface Repository<T> {
    // ============================================================
    // OOP CONCEPT: ABSTRACTION
    // Method signature tanpa implementation
    // Implementasi akan dilakukan di concrete class
    // ============================================================

    fun save(item: T)                          // Create
    fun findAll(): List<T>                     // Read all
    fun findById(id: String): T?               // Read one (nullable)
    fun update(id: String, item: T): Boolean   // Update
    fun delete(id: String): Boolean            // Delete
}