package brawijaya.example.purisaehomestay.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.createImageUri
import brawijaya.example.purisaehomestay.utils.hasPermissions
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImageUploader(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageUrls: List<String> = emptyList(),
    placeHolderResId: Int? = null,
    onImageUrlChanged: (Uri) -> Unit = {},
    onImageUrlsChanged: (List<Uri>) -> Unit = {},
    onImageRemoved: (String) -> Unit = {},
    isUploading: Boolean = false,
    isMultiple: Boolean = false,
) {
    val maxImages = 4
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val permissionsGranted = remember {
        hasPermissions(
            context,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        )
    }

    if (isMultiple) {
        MultipleImageUploader(
            modifier = modifier,
            imageUrls = imageUrls,
            onImageUrlsChanged = onImageUrlsChanged,
            onImageRemoved = onImageRemoved,
            isUploading = isUploading,
            maxImages = maxImages,
            permissionsGranted = permissionsGranted,
            showImagePickerDialog = showImagePickerDialog,
            onShowImagePickerDialog = { showImagePickerDialog = it }
        )
    } else {
        SingleImageUploader(
            modifier = modifier,
            imageUrl = imageUrl,
            placeHolderResId = placeHolderResId,
            onImageUrlChanged = onImageUrlChanged,
            isUploading = isUploading,
            permissionsGranted = permissionsGranted,
            showImagePickerDialog = showImagePickerDialog,
            onShowImagePickerDialog = { showImagePickerDialog = it }
        )
    }

    if (showImagePickerDialog) {
        ImagePickerDialog(
            isMultiple = isMultiple,
            maxImages = maxImages,
            currentImageCount = if (isMultiple) imageUrls.size else (if (imageUrl != null) 1 else 0),
            onDismiss = {
                showImagePickerDialog = false
            },
            onImageSelected = { uri ->
                if (isMultiple) {
                    onImageUrlsChanged(listOf(uri))
                } else {
                    onImageUrlChanged(uri)
                }
                showImagePickerDialog = false
            },
            onImagesSelected = { uris ->
                onImageUrlsChanged(uris)
                showImagePickerDialog = false
            }
        )
    }
}

@Composable
private fun SingleImageUploader(
    modifier: Modifier,
    imageUrl: String?,
    placeHolderResId: Int?,
    onImageUrlChanged: (Uri) -> Unit,
    isUploading: Boolean,
    permissionsGranted: Boolean,
    showImagePickerDialog: Boolean,
    onShowImagePickerDialog: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, PrimaryGold, RoundedCornerShape(8.dp))
            .clickable {
                if (permissionsGranted) {
                    onShowImagePickerDialog(true)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            CircularProgressIndicator(color = PrimaryGold)
        } else if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Uploaded Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (placeHolderResId != null) {
            Image(
                painter = painterResource(id = placeHolderResId),
                contentDescription = "Placeholder Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add Image",
                    tint = PrimaryGold,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tambah Gambar",
                    color = PrimaryGold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MultipleImageUploader(
    modifier: Modifier,
    imageUrls: List<String>,
    onImageUrlsChanged: (List<Uri>) -> Unit,
    onImageRemoved: (String) -> Unit,
    isUploading: Boolean,
    maxImages: Int,
    permissionsGranted: Boolean,
    showImagePickerDialog: Boolean,
    onShowImagePickerDialog: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(imageUrls) { imageUrl ->
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(1f)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Uploaded Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { onImageRemoved(imageUrl) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .scale(0.75f)
                            .background(
                                PrimaryGold.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Remove Image",
                            tint = Color.White
                        )
                    }
                }
            }

            item {
                if (imageUrls.size < maxImages) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryGold)
                            .clickable {
                                if (permissionsGranted) {
                                    onShowImagePickerDialog(true)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePickerDialog(
    isMultiple: Boolean = false,
    maxImages: Int = 3,
    currentImageCount: Int = 0,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit = {},
    onImagesSelected: (List<Uri>) -> Unit = {}
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val remainingSlots = maxImages - currentImageCount

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = if (isMultiple) {
            ActivityResultContracts.GetMultipleContents()
        } else {
            ActivityResultContracts.GetContent()
        }
    ) { result ->
        if (isMultiple) {
            val uris = result as? List<Uri>
            if (!uris.isNullOrEmpty()) {
                val limitedUris = uris.take(remainingSlots)
                onImagesSelected(limitedUris)
            }
        } else {
            val uri = result as? Uri
            if (uri != null) {
                onImageSelected(uri)
            }
        }
        onDismiss()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUri != null) {
            if (isMultiple) {
                onImagesSelected(listOf(cameraUri!!))
            } else {
                onImageSelected(cameraUri!!)
            }
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih Sumber Gambar",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        cameraUri = createImageUri(context)
                        cameraLauncher.launch(cameraUri!!)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("Kamera")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text(
                        text = "Galeri"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = PrimaryGold
                    )
                ) {
                    Text("Batal")
                }
            }
        }
    }
}