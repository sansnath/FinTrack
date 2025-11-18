package com.example.fintrack.utils
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(value).replace("Rp", "Rp ").replace(",00", "")
}