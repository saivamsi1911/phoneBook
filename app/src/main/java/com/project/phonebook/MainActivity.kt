package com.project.phonebook

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.phonebook.model.CallLogItem
import com.project.phonebook.screens.HomeScreen
import com.project.phonebook.ui.theme.PhoneBookTheme
import com.project.phonebook.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PhoneBookTheme {
                val vm = hiltViewModel<MainViewModel>()
                val context = LocalContext.current as ComponentActivity
                val isGranted = remember {
                    mutableStateOf(
                        (ActivityCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_CALL_LOG
                        ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED)
                    )
                }


                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { granted ->
                    if (!granted.values.contains(false)) isGranted.value = true
                }

                LaunchedEffect(Unit) {  // Launch permissions only once when the Composable enters composition
                    if (!isGranted.value) {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.READ_CALL_LOG,
                                android.Manifest.permission.READ_CONTACTS
                            )
                        )
                    }
                }


                if (isGranted.value) {
                    HomeScreen(vm) {
                        println("@@@ call callback -> $it")
                        makePhoneCall(context, it)
                    }
                }
            }
        }
    }
}

fun makePhoneCall(context: Context, phoneNumber: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    ContextCompat.startActivity(context, dialIntent, null)
}

