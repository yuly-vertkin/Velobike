package ru.sitronics.velobike.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailDialog(parking: Parking, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val freePlaces = parking.freeNonElectricSlots + parking.freeElectricSlots + parking.freeOmniSlots

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .background(HeaderBackgroundColor)
                .padding(all = 12.dp)
        ) {
            Text(
                text = "№ ${parking.id}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = parking.address,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = freePlaces.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(text = context.getString(R.string.free_places))
        }
    }
}
