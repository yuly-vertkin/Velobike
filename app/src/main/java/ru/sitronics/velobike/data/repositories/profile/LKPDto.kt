package ru.sitronics.velobike.data.repositories.profile

import com.google.gson.annotations.SerializedName
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.profile.MetroPasswordParameters

data class OtpAuth(
    @SerializedName("password_parameters")
    val passwordParameters: MetroPasswordParameters
) : ResponseDto<MetroPasswordParameters> {
    override fun toModel(): MetroPasswordParameters = passwordParameters
}
