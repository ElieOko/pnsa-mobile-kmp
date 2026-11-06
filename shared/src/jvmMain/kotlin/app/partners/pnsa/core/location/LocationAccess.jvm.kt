package app.partners.pnsa.core.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.partners.pnsa.core.ui.theme.RepereInk
import app.partners.pnsa.core.ui.theme.RepereMuted
import app.partners.pnsa.core.ui.theme.ReperePurple
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private val locationChoice = File(System.getProperty("user.home"), ".cache/repere-jeunes/location-granted")
private val locationGrantedState = mutableStateOf(locationChoice.isFile)
private val locationPromptHandled = mutableStateOf(locationGrantedState.value)
private var locationDialogOwner: Any? = null

@Composable
actual fun rememberLocationGranted(): Boolean {
    val granted by locationGrantedState
    val handled by locationPromptHandled
    val owner = remember { Any() }
    if (!granted && !handled && locationDialogOwner == null) {
        locationDialogOwner = owner
    }
    if (!granted && !handled && locationDialogOwner == owner) {
        Dialog(onDismissRequest = { locationPromptHandled.value = true }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        "Activer la localisation",
                        color = RepereInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Repère Jeunes a besoin de ta position pour montrer les services près de toi et proposer l’itinéraire.",
                        color = RepereMuted,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { locationPromptHandled.value = true }) {
                            Text("Plus tard", color = RepereMuted)
                        }
                        Button(
                            onClick = {
                                locationChoice.parentFile?.mkdirs()
                                runCatching { locationChoice.writeText("granted") }
                                locationGrantedState.value = true
                                locationPromptHandled.value = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ReperePurple, contentColor = Color.White),
                        ) {
                            Text("Autoriser", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    return granted
}

actual fun locationUpdates(): Flow<LatLngPoint> = emptyFlow()
