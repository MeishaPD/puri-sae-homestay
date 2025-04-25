package brawijaya.example.purisaehomestay.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.DateUtils
import java.util.Calendar

@Composable
fun DateRangePicker(
    checkInDate: String,
    onCheckInDateSelected: (String) -> Unit,
    checkOutDate: String,
    onCheckOutDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    minCheckInDate: Long? = null,
    checkOutError: String? = null
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val checkInDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)
            onCheckInDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    minCheckInDate?.let {
        checkInDatePickerDialog.datePicker.minDate = it
    }

    val checkOutDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)

            if (checkInDate.isNotEmpty()) {
                if (!DateUtils.isValidCheckOutDate(checkInDate, formattedDate)) {
                    onCheckOutDateSelected("")
                    return@DatePickerDialog
                }
            }

            onCheckOutDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val minCheckOutDate = DateUtils.getMillisFromDate(checkInDate)
    minCheckOutDate?.let {
        checkOutDatePickerDialog.datePicker.minDate = it + 24 * 60 * 60 * 1000
    } ?: run {
        minCheckInDate?.let {
            checkOutDatePickerDialog.datePicker.minDate = it
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = if (checkOutError != null) Color.Red else PrimaryGold.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { checkInDatePickerDialog.show() }
                ) {
                    Text(
                        text = "Check In",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = if (checkInDate.isEmpty()) "Pilih tanggal" else checkInDate,
                        color = if (checkInDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = PrimaryGold
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { checkOutDatePickerDialog.show() }
                ) {
                    Text(
                        text = "Check Out",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = if (checkOutDate.isEmpty()) "Opsional" else checkOutDate,
                        color = if (checkOutDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date Range",
                    tint = PrimaryGold,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            if (checkInDate.isEmpty()) {
                                checkInDatePickerDialog.show()
                            } else {
                                checkOutDatePickerDialog.show()
                            }
                        }
                )
            }
        }

        if (checkOutError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = checkOutError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

