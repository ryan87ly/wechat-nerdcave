package nerd.cave.model.api.branch

data class BranchClientInfo(
    val id: String,
    val name: String,
    val location: LocationInfo,
    val weekdayOpenHour: OpenHourInfo,
    val holidayOpenHourInfo: OpenHourInfo,
    val contactNumbers: List<String>,
    val description: String,
    val isOpen: Boolean
)