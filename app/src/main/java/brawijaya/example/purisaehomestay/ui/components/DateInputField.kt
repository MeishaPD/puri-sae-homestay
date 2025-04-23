package brawijaya.example.purisaehomestay.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import java.util.Calendar
import java.util.Locale

@Composable
fun DateInputField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDate: Long? = null,
    errorText: String? = null
) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale("id", "ID"), "%02d/%02d/%d", dayOfMonth, month + 1, year)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    minDate?.let {
        datePickerDialog.datePicker.minDate = it
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            label = { Text(label) },
            onValueChange = {  },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { datePickerDialog.show() }),
            placeholder = {
                Text(
                    text = if (label.contains("Out") && value.isEmpty()) "Opsional" else value,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = PrimaryGold,
                    modifier = Modifier.clickable { datePickerDialog.show() }
                )
            },
            isError = errorText != null,
            supportingText = {
                if (errorText != null) {
                    Text(
                        text = errorText,
                        color = Color.Red
                    )
                }
            }
        )
    }
}