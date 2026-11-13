package com.example.mytablebookingpradita

object AuthValidator {
    fun validateLogin(emailOrNim: String, password: String): Pair<Boolean, String> {
        if (emailOrNim.isEmpty() || password.isEmpty()) {
            return Pair(false, "NIM/Email dan Password tidak boleh kosong!")
        }

        val isNim = emailOrNim.all { it.isDigit() }
        val isPraditaEmail = emailOrNim.endsWith("@pradita.ac.id")

        return if (isNim || isPraditaEmail) {
            Pair(true, "Login Berhasil")
        } else {
            Pair(false, "Gunakan NIM (Angka) atau Email @pradita.ac.id!")
        }
    }
}