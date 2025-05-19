package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.ui.screens.order.components.PackageCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold

@Composable
fun PackageCardWithActions(
    paket: Paket,
    isSelected: Boolean,
    onSelect: (Paket) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column {
        PackageCard(
            paket = paket,
            isSelected = isSelected,
            onSelect = { onSelect(paket) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onEdit,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryGold
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus")
            }
        }
    }
}