package ru.sitronics.velobike.presentation.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.tools.BackPressHandler

@Composable
fun BankCardsScreen(
    cards: List<Card>,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        cards.forEach {
            CardItem(it)
        }

        if (cards.isEmpty()) {
            Text(
                text = stringResource(R.string.no_bank_cards),
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    BackPressHandler(onBackPressed = onBack)
}

@Composable
private fun CardItem(card: Card) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(getCardLogo(card.cardType)),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = card.cardNumber,
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f)
        )

        if (card.isDefault != 0) {
            Icon(
                painter = painterResource(R.drawable.card_default_checkmark),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}

@DrawableRes
fun getCardLogo(cardType: String): Int {
    return when (cardType) {
        "VISA" -> R.drawable.card_visa_logo
        "MASTERCARD" -> R.drawable.card_mastercard_logo
        "MAESTRO" -> R.drawable.card_maestro_logo
        "MIR" -> R.drawable.card_mir_logo
        "UNIONPAY" -> R.drawable.card_unionpay_logo
        else -> R.drawable.card_unknown_logo
    }
}

