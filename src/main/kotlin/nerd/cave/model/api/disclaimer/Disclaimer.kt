package nerd.cave.model.api.disclaimer

data class DisclaimerSignature (
    val memberId: String,
    val legalName: String,
    val contactNumber: String,
    val emergentContactNumber: String
)