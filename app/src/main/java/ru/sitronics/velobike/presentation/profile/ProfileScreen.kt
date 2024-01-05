package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import ru.sitronics.velobike.domain.profile.Profile

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profileUiState by profileViewModel.profileUiState.collectAsStateWithLifecycle()
    val profile = if (profileUiState is ProfileUiState.Normal) (profileUiState as ProfileUiState.Normal).profile
                  else Profile.empty

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)) {
        Text(
            text = context.getString(R.string.profile_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
