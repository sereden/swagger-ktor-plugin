package com.test

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class StringEnumResponsePropertyNameOption(
    public val rawValue: String,
) {
    @SerialName("FIRST")
    First("FIRST"),
    @SerialName("SECOND")
    Second("SECOND"),
    @SerialName("THIRD")
    Third("THIRD"),
    ;
}
package com.test

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class StringEnumResponse(
    @SerialName("propertyName")
    public val propertyName: StringEnumResponsePropertyNameOption,
)