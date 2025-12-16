package service

// ============================================================
// OOP CONCEPT: STRATEGY PATTERN
// Interface untuk berbagai strategi perhitungan denda
// Memungkinkan algoritma perhitungan denda diubah saat runtime
// ============================================================
interface StrategiDenda {
    fun hitung(hariTerlambat: Long): Int
}

// ============================================================
// OOP CONCEPT: STRATEGY PATTERN - Concrete Strategy 1
// Strategi denda standar: Rp 1000 per hari
// ============================================================
class DendaStandar : StrategiDenda {
    override fun hitung(hariTerlambat: Long): Int {
        return (hariTerlambat * 1000).toInt()
    }
}

// ============================================================
// OOP CONCEPT: STRATEGY PATTERN - Concrete Strategy 2
// Strategi denda progresif: semakin lama semakin mahal
// 1-3 hari: Rp 1000/hari
// 4-7 hari: Rp 2000/hari
// >7 hari: Rp 3000/hari
// ============================================================
class DendaProgresif : StrategiDenda {
    override fun hitung(hariTerlambat: Long): Int {
        return when {
            hariTerlambat <= 3 -> (hariTerlambat * 1000).toInt()
            hariTerlambat <= 7 -> (3 * 1000 + (hariTerlambat - 3) * 2000).toInt()
            else -> (3 * 1000 + 4 * 2000 + (hariTerlambat - 7) * 3000).toInt()
        }
    }
}

// ============================================================
// OOP CONCEPT: STRATEGY PATTERN - Concrete Strategy 3
// Strategi denda weekend: tidak kena denda di akhir pekan
// Mengurangi hari weekend dari perhitungan
// ============================================================
class DendaWeekend : StrategiDenda {
    override fun hitung(hariTerlambat: Long): Int {
        // Asumsi: ~2/7 hari adalah weekend
        val hariKerja = (hariTerlambat * 5 / 7)
        return (hariKerja * 1000).toInt()
    }
}