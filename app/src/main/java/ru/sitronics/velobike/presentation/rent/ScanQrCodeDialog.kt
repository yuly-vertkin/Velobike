package ru.sitronics.velobike.presentation.rent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.isCameraPermissionGranted
import ru.sitronics.velobike.tools.rememberCameraPermissionLauncher
import ru.sitronics.velobike.tools.runWithCamera

@Composable
fun ScanQrCodeDialog(
    onCancel: () -> Unit,
    onAction: (String) -> Unit,
) {
    val context = LocalContext.current
    val cameraPermissionLauncher = rememberCameraPermissionLauncher()
    val qrCodeScannerView = remember {
        LayoutInflater.from(context).inflate(R.layout.qr_code_scanner, null)
    }
    val codeScanner = remember {
        val scannerView = qrCodeScannerView.findViewById<CodeScannerView>(R.id.scanner_view)
        CodeScanner(context, scannerView)
    }

    AndroidView({
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat, ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback { result ->
            val parts = result.text.split("{", "}")
            val bikeNumber = if (parts.isNotEmpty()) parts.last { it.isNotEmpty() } else ""
            onAction(bikeNumber)
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            Logg.d("!!!! Camera initialization error: ${it.message}")
            onCancel()
        }

        val bikeNumber = qrCodeScannerView.findViewById<EditText>(R.id.bike_number)
        bikeNumber.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(context, bikeNumber)
                onAction(view.text.toString())
                true
            } else false
        }

        qrCodeScannerView.findViewById<ImageView>(R.id.next_btn).setOnClickListener {
            hideKeyboard(context, bikeNumber)
            onAction(bikeNumber.text.toString())
        }

//        scannerView.setOnClickListener {
//            codeScanner.startPreview()
//        }

        qrCodeScannerView
    })

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = qrCodeScannerView) {
        // Make qrCodeScannerView follow the current lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    cameraPermissionLauncher.runWithCamera(context) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    updateCameraView(qrCodeScannerView, isCameraPermissionGranted(context))
                    if (isCameraPermissionGranted(context)) {
                        codeScanner.startPreview()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    codeScanner.releaseResources()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    BackPressHandler(onBackPressed = onCancel)
}

private fun updateCameraView(qrCodeScannerView: View, isCameraAvailable: Boolean) {
    qrCodeScannerView.findViewById<CodeScannerView>(R.id.scanner_view).visibility = if (isCameraAvailable) View.VISIBLE else View.INVISIBLE
    qrCodeScannerView.findViewById<TextView>(R.id.error_no_permission).isVisible = !isCameraAvailable
    qrCodeScannerView.findViewById<TextView>(R.id.scan_text).isVisible = isCameraAvailable
}

private fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}