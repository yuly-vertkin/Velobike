package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.ui.theme.LoadingBackgroundColor

@Composable
fun BonusMetroDialog(profile: Profile?, onAction: (ProfileIntent) -> Unit) {
    val code by remember { mutableStateOf(MutableList(CODE_DIGIT_NUM) { "" })}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoadingBackgroundColor)
            .clickable { },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = (-100).dp)
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            Text(
                text = stringResource(R.string.link_account),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = stringResource(R.string.sms_sent, profile?.phoneNumber.orEmpty()),
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                code.forEachIndexed { i, _ ->
                    DigitField { value ->
                        code[i] = value
                        if (code.count { it.isNotEmpty() } == CODE_DIGIT_NUM)
                            onAction(ProfileIntent.BonusMetroToken(code.joinToString("")))
                    }
                }
            }

            Button(
                onClick = { onAction(ProfileIntent.BonusMetroAuth) },
//                enabled = agree,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Text(text = stringResource(R.string.send_sms_again))
            }

        }
    }

    BackPressHandler{ onAction(ProfileIntent.BonusMetroAuth) }
}

@Composable
fun DigitField(onChange: (String) -> Unit) {
    var digit by remember { mutableStateOf("") }

    OutlinedTextField(
        value = digit,
        onValueChange = {
            if (it.isEmpty() || it.length == 1 && it[0].isDigit()) {
                digit = it
                onChange(it)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(45.dp)
    )
}

private const val CODE_DIGIT_NUM = 6