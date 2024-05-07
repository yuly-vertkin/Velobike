package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.BackPressHandler

@Composable
fun PortPlusDialog(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.port_plus_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Image(
            painter = painterResource(id = R.drawable.port_plus),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color.Red, CircleShape)
                .padding(all = 8.dp)
        )

        Text(
            text = stringResource(R.string.port_plus_topic_1),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.port_plus_text_1),
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = stringResource(R.string.port_plus_topic_2),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.port_plus_text_2),
            modifier = Modifier.padding(top = 12.dp)
        )
    }

    BackPressHandler(onBackPressed = onBack)
}
