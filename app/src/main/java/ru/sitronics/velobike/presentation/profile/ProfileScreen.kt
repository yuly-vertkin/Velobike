package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.presentation.SimpleDialog
import ru.sitronics.velobike.tools.MenuItem
import ru.sitronics.velobike.tools.formatDateTimeStr

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val profileUiState by profileViewModel.profileUiState.collectAsStateWithLifecycle()
    val onAction: (ProfileIntent) -> Unit = { intent -> profileViewModel.handleIntent(intent) }

    when(profileUiState) {
        is ProfileUiState.Normal -> {
            val profile = (profileUiState as? ProfileUiState.Normal)?.profile ?: Profile()
            ProfileScreenInt(contentPadding, profile, onAction)
        }
        is ProfileUiState.Tariffs -> {
            val tariffs = (profileUiState as? ProfileUiState.Tariffs)?.tariffs ?: emptyList()
            TariffsScreen(tariffs) {
                onAction(ProfileIntent.GetTariff(it, true))
            }
        }
        is ProfileUiState.TariffDetail -> {
            val uiState = profileUiState as ProfileUiState.TariffDetail
            TariffDetail(uiState.tariff, uiState.canBuy) {
                onAction(ProfileIntent.BuyTariff(it, uiState.canBuy))
            }
        }
        is ProfileUiState.Cards -> {
            val cards = (profileUiState as? ProfileUiState.Cards)?.cards ?: emptyList()
            BankCardsScreen(cards) {
                onAction(ProfileIntent.CloseMessage)
            }
        }
        is ProfileUiState.ShowMessage -> {
            val uiState = profileUiState as ProfileUiState.ShowMessage
            ShowMessageDialog(uiState.text) {
                onAction(ProfileIntent.CloseMessage)
            }
        }
    }
}

@Composable
fun ProfileScreenInt(
    contentPadding: PaddingValues,
    profile: Profile,
    onAction: (ProfileIntent) -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(horizontal = 16.dp)
    ) {
        Text(
            text = context.getString(R.string.profile_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )
        Row {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
            )
            Text(
                text = context.getString(R.string.profile_login, profile.login),
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = context.getString(R.string.profile_password, "...."),
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = "${profile.firstName} ${profile.lastName}",
            modifier = Modifier.padding(top = 16.dp)
        )

        TariffSection(profile, isOld = true, onAction)
        TariffSection(profile, isOld = false, onAction)

        MenuItem(R.string.my_bank_cards, R.drawable.card) { onAction(ProfileIntent.GetCards) }
    }
}

@Composable
private fun TariffSection(profile: Profile, isOld: Boolean, onAction: (ProfileIntent) -> Unit) {
    val context = LocalContext.current

    Text(
        text = context.getString(if (isOld) R.string.old_bike_tab else R.string.new_bike_tab),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val isTariff = isOld && profile.tariffIdOld.isNotEmpty() || !isOld && profile.tariffId.isNotEmpty()

        if (isTariff) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(R.string.tariff_name, if (isOld) profile.tariffNameOld else profile.tariffName),
                    fontSize = 14.sp,
                )

                val cost = if (isOld) profile.tariffOld?.cost?.toInt() ?: 0
                           else profile.tariff?.cost?.toInt() ?: 0
                Text(
                    text = "$cost ₽",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                )

                Text(
                    text = context.getString(R.string.tariff_end, getDateStr(
                        if (isOld) profile.tariffEndOld else profile.tariffEnd
                    )),
                    fontSize = 14.sp,
                )
            }
        } else {
            Text(
                text = context.getString(R.string.no_tariff),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Button(onClick = {
            val intent = if (isOld && profile.tariffIdOld.isNotEmpty()) ProfileIntent.GetTariff(profile.tariffOld, false)
                    else if (!isOld && profile.tariffId.isNotEmpty()) ProfileIntent.GetTariff(profile.tariff, false)
                    else ProfileIntent.GetTariffs
            onAction(intent)
        }) {
            val textId = if (isOld && profile.tariffIdOld.isNotEmpty() ||
                             !isOld && profile.tariffId.isNotEmpty()) R.string.tariff
                         else R.string.buy_tariff
            Text(text = context.getString(textId))
        }
    }

    HorizontalDivider(Modifier.padding(top = 8.dp), 1.dp, Color.LightGray)
}

@Composable
fun ShowMessageDialog(msg: String?, onClick: () -> Unit) {
    val context = LocalContext.current

    SimpleDialog(
        onDismissRequest = { onClick() },
        onConfirmation = { onClick() },
        dialogTitle = context.getString(R.string.warning),
        dialogText = msg ?: context.getString(R.string.warning),
        icon = Icons.Default.Warning
    )
}

private fun getDateStr(time: String?) : String =
    time?.let { formatDateTimeStr(it, "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy") } ?: ""
