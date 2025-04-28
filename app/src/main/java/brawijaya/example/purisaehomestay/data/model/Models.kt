package brawijaya.example.purisaehomestay.data.model

data class UserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "Penyewa",
    val photoUrl: String? = null
)
