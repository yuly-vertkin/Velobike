package ru.sitronics.velobike.presentation.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.BonusMetroStatus
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.tools.MenuItem
import ru.sitronics.velobike.tools.callToSupport
import ru.sitronics.velobike.tools.getDateTimeStr
import java.util.Date

@Composable
fun BonusMetroScreen(
    contentPadding: PaddingValues,
    status: BonusMetroStatus,
    profile: Profile?,
    onAction: (ProfileIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(top = 16.dp)
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.bonus_metro),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Image(
            painter = painterResource(id = R.drawable.bonus_metro_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        when (status) {
            BonusMetroStatus.ACTIVATED -> BonusMetroScreenActivated(profile)
            BonusMetroStatus.NOT_ACTIVATED -> BonusMetroScreenNotActivated(onAction)
            BonusMetroStatus.UNDEFINED -> BonusMetroScreenUndefined(profile)
        }
    }

    BackPressHandler{ onAction(ProfileIntent.GoToNormal) }
}

@Composable
private fun BonusMetroScreenActivated(profile: Profile?) {
    val time = getDateStr(profile?.oldTariffEnd ?: profile?.tariffEnd)

    if (time.isNotEmpty())
        MenuItem(stringResource(R.string.bonus_validity, time), R.drawable.clock_small) {}

    TariffSection(profile, isOld = true)
    TariffSection(profile, isOld = false)
}

@Composable
private fun TariffSection(profile: Profile?, isOld: Boolean) {
    val tariff = if (isOld) profile?.oldTariff else profile?.tariff
    val numTrips = (tariff?.numberOfDisable ?: 0) - (profile?.fullNumberRental ?: 0)

    Text(
        text = stringResource(if (isOld) R.string.old_bike_tab else R.string.new_bike_tab),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )

    // TODO: temp UI
    MenuItem(stringResource(R.string.bonus_trips_left, numTrips), R.drawable.bike_small) {}
    MenuItem(stringResource(R.string.bonus_free_minutes, 30, 4), R.drawable.clock_cost) {}

    // TODO: add description
    Text(
        text = "Описание",
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun BonusMetroScreenNotActivated(onAction: (ProfileIntent) -> Unit) {
    var agree by remember { mutableStateOf(false) }

    Text(stringResource(R.string.bonus_trip_num))

    BonusCard(R.string.bonus_variant_30, R.string.bonus_variant_30_title, R.string.bonus_variant_30_desc)
    BonusCard(R.string.bonus_variant_90, R.string.bonus_variant_90_title, R.string.bonus_variant_90_desc)
    BonusCard(R.string.bonus_variant_365, R.string.bonus_variant_365_title, R.string.bonus_variant_365_desc)

    // TODO: add description
    Text(
        text = "Описание",
        modifier = Modifier.padding(vertical = 100.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Checkbox(
            checked = agree,
            onCheckedChange = { agree = it }
        )
        Text(
            text = stringResource(R.string.agree),
            fontSize = 12.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    Button(
        onClick = { onAction(ProfileIntent.BonusMetroAuth) },
        enabled = agree,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Text(text = stringResource(R.string.get_bonus))
    }
}

@Composable
private fun ColumnScope.BonusMetroScreenUndefined(profile: Profile?) {
    val context = LocalContext.current

    Text(
        text = profile?.phoneNumber.orEmpty(),
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    Text(
        text = stringResource(R.string.bonus_not_found),
        modifier = Modifier.padding(vertical = 16.dp)
    )

    // TODO: add description
    Text(
        text = "Описание",
        modifier = Modifier.padding(vertical = 16.dp)
    )

    Button(
        onClick = { goToMetro(context) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Text(text = stringResource(R.string.bonus_go_to_metro))
    }

    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(
            text = stringResource(R.string.bonus_questions),
            fontSize = 12.sp,
        )

        Text(
            text = stringResource(R.string.call_us),
            fontSize = 12.sp,
            color = Color.Blue,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { callToSupport(context) }
        )
    }
}

@Composable
private fun BonusCard(@StringRes bonusId: Int, @StringRes bonusTitleId: Int, @StringRes bonusDescId: Int) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .requiredSize(size = 56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        ) {
            Text(stringResource(bonusId))
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = stringResource(bonusTitleId),
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(bonusDescId),
                fontSize = 12.sp,
            )

            HorizontalDivider(Modifier.padding(top = 8.dp), 1.dp, Color.LightGray)
        }
    }
}

private fun goToMetro(context: Context) {
    val uri = Uri.parse(context.getString(R.string.metro_moscow_link))
    val  intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun getDateStr(time: Date?) : String =
    time?.let { getDateTimeStr(it, "dd.MM.yyyy") } ?: ""
