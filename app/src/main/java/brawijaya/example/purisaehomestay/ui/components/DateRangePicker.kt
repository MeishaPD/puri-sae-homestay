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
    startDate: String,
    onStartDateSelected: (String) -> Unit,
    endDate: String,
    onEndDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    minStartDate: Long? = null,
    errorMessage: String? = null,
    isEditPromo: Boolean? = false
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val checkInDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)
            onStartDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    minStartDate?.let {
        checkInDatePickerDialog.datePicker.minDate = it
    }

    val checkOutDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = DateUtils.formatDate(year, month, dayOfMonth)

            if (startDate.isNotEmpty()) {
                if (!DateUtils.isValidCheckOutDate(startDate, formattedDate)) {
                    onEndDateSelected("")
                    return@DatePickerDialog
                }
            }

            onEndDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val minEndDate = DateUtils.getMillisFromDate(startDate)
    minEndDate?.let {
        checkOutDatePickerDialog.datePicker.minDate = it + 24 * 60 * 60 * 1000
    } ?: run {
        minStartDate?.let {
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
                    color = if (errorMessage != null) Color.Red else PrimaryGold.copy(alpha = 0.5f),
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
                        text = if (isEditPromo == true) "Tanggal Mulai" else "Check In",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = if (startDate.isEmpty()) "Pilih tanggal" else startDate,
                        color = if (startDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                        text = if (isEditPromo == true) "Tanggal Selesai" else "Check Out",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = if (endDate.isEmpty()) "" else endDate,
                        color = if (endDate.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            if (startDate.isEmpty()) {
                                checkInDatePickerDialog.show()
                            } else {
                                checkOutDatePickerDialog.show()
                            }
                        }
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

