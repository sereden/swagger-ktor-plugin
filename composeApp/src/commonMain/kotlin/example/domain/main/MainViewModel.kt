package example.domain.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.data.remote.dto.FeeDto
import com.example.android.data.remote.dto.IbanPayOutChannelDataDto
import com.example.android.data.remote.dto.IbanPayOutChannelDataDtoOption
import com.example.android.data.remote.dto.IbanPayOutChannelDataDtoPayOutChannelCodeOption
import com.example.android.data.remote.dto.InitMoneyTransferConfirmResponse
import com.example.android.data.remote.dto.MoneyTransferPayInChannelDto
import com.example.android.data.remote.dto.MoneyTransferPayInChannelDtoCodeOption
import com.example.android.data.remote.dto.MoneyTransferPayOutChannelDto
import com.example.android.data.remote.dto.MoneyTransferPayOutChannelDtoCodeOption
import example.model.MainScreenEvent
import example.model.MainScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent

@Factory
class MainViewModel : ViewModel(), KoinComponent {
    private val screenState = MutableStateFlow(MainScreenState())
    val screen = screenState.asStateFlow()

    init {
        observeData()
        parseSoledInterface()
    }

    private fun parseSoledInterface() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val request = getInitRequest()
        val encoded = json.encodeToString(request)
        val decoded = json.decodeFromString<InitMoneyTransferConfirmResponse>(getInitJson2())
        println(decoded)
    }

    private fun observeData() = viewModelScope.launch {

    }

    private fun getInitRequest(): InitMoneyTransferConfirmResponse {
        return InitMoneyTransferConfirmResponse(
            receiverLastName = "receiverLastName",
            payInCurrency = "payInCurrency",
            payOutChannel = MoneyTransferPayOutChannelDto(
                code = MoneyTransferPayOutChannelDtoCodeOption.Card,
                name = "payOutChannelName"
            ),
            created = "created",
            payOutCurrency = "payOutCurrency",
            receiverPhoneNumber = "receiverPhoneNumber",
            receiverLatinLastName = "receiverLatinLastName",
            confirmId = "confirmId",
            payInChannel = MoneyTransferPayInChannelDto(
                code = MoneyTransferPayInChannelDtoCodeOption.GooglePay,
                name = "payInChannelName"
            ),
            receiverLatinFirstName = "receiverLatinFirstName",
            payOutAmount = 1.0,
            receiverUserId = "receiverUserId",
            duration = 2,
            feeAmount = 3.0,
            payOutChannelData = IbanPayOutChannelDataDtoOption(
                value = IbanPayOutChannelDataDto(
                    payOutChannelCode = IbanPayOutChannelDataDtoPayOutChannelCodeOption.Iban,
                    iban = "iban",
                    "taxNumber"
                )
            ),
            exchangeRate = 4.0,
            payInAmount = 5.0,
            fee = FeeDto(2.0, currency = "USD"),
            receiverFirstName = "receiverFirstName",
        )
    }

    private fun getInitJson(): String {
        return """
            {"receiverLastName":"receiverLastName","payInCurrency":"payInCurrency","payOutChannel":{"code":"CARD","name":"payOutChannelName"},"created":"created","payOutCurrency":"payOutCurrency","receiverPhoneNumber":"receiverPhoneNumber","receiverLatinLastName":"receiverLatinLastName","confirmId":"confirmId","payInChannel":{"code":"CARD","name":"payInChannelName"},"receiverLatinFirstName":"receiverLatinFirstName","payOutAmount":1.0,"receiverUserId":"receiverUserId","duration":2,"feeAmount":3.0,"payOutChannelData":{"payOutChannelCode":"CARD"},"exchangeRate":4.0,"payInAmount":5.0,"receiverFirstName":"receiverFirstName"}
        """.trimIndent()
    }

    private fun getInitJson2(): String {
        return """
            {"receiverLastName":"receiverLastName","payInCurrency":"payInCurrency","payOutChannel":{"code":"CARD","name":"payOutChannelName"},"created":"created","payOutCurrency":"payOutCurrency","receiverPhoneNumber":"receiverPhoneNumber","receiverLatinLastName":"receiverLatinLastName","confirmId":"confirmId","payInChannel":{"code":"CARD","name":"payInChannelName"},"receiverLatinFirstName":"receiverLatinFirstName","payOutAmount":1.0,"receiverUserId":"receiverUserId","duration":2,"feeAmount":3.0,"payOutChannelData":{"payOutChannelCode":"CARD","iban":"iban","taxNumber":"taxNumber"},"exchangeRate":4.0,"payInAmount":5.0,"receiverFirstName":"receiverFirstName"}
        """.trimIndent().trimIndent()
    }

    fun onEvent(event: MainScreenEvent) {

    }
}