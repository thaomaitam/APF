package com.KTA.devicespoof.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.KTA.devicespoof.profile.Profile
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.utils.Logger

@Composable
fun ProfileEditorScreen(
    profileManager: ProfileManager,
    profileId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var profileName by remember { mutableStateOf("") }
    var imei1 by remember { mutableStateOf("") }
    var imei2 by remember { mutableStateOf("") }
    var androidId by remember { mutableStateOf("") }
    var buildFingerprint by remember { mutableStateOf("") }
    var buildDevice by remember { mutableStateOf("") }
    var wifiMac by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiBssid by remember { mutableStateOf("") }
    var bluetoothMac by remember { mutableStateOf("") }
    var buildProduct by remember { mutableStateOf("") }
    var simSerial by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var simOperator by remember { mutableStateOf("") }
    var adsId by remember { mutableStateOf("") }
    var mediaDrmId by remember { mutableStateOf("") }
    var gsfId by remember { mutableStateOf("") }
    var buildBrand by remember { mutableStateOf("") }
    var buildHardware by remember { mutableStateOf("") }
    var buildBoard by remember { mutableStateOf("") }
    var buildId by remember { mutableStateOf("") }
    var buildDisplay by remember { mutableStateOf("") }
    var buildType by remember { mutableStateOf("") }
    var buildTags by remember { mutableStateOf("") }
    var buildVersionRelease by remember { mutableStateOf("") }
    var buildVersionIncremental by remember { mutableStateOf("") }
    var buildVersionCodename by remember { mutableStateOf("") }
    var buildVersionSecurityPatch by remember { mutableStateOf("") }
    var simSubscriberId by remember { mutableStateOf("") }
    var simCountry by remember { mutableStateOf("") }
    var simMnc by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load existing profile if editing
    LaunchedEffect(profileId) {
        if (profileId != null) {
            val profile = profileManager.getProfileById(profileId)
            profile?.let {
                profileName = it.name
                it.deviceInfo?.let { deviceInfo ->
                    imei1 = deviceInfo.imei1 ?: ""
                    imei2 = deviceInfo.imei2 ?: ""
                    androidId = deviceInfo.androidId ?: ""
                    buildFingerprint = deviceInfo.buildFingerprint ?: ""
                    buildDevice = deviceInfo.buildDevice ?: ""
                    wifiMac = deviceInfo.wifiMac ?: ""
                    wifiSsid = deviceInfo.wifiSsid ?: ""
                    wifiBssid = deviceInfo.wifiBssid ?: ""
                    bluetoothMac = deviceInfo.bluetoothMac ?: ""
                    buildProduct = deviceInfo.buildProduct ?: ""
                    simSerial = deviceInfo.simSerial ?: ""
                    mobileNumber = deviceInfo.mobileNumber ?: ""
                    simOperator = deviceInfo.simOperator ?: ""
                    adsId = deviceInfo.adsId ?: ""
                    mediaDrmId = deviceInfo.mediaDrmId ?: ""
                    gsfId = deviceInfo.gsfId ?: ""
                    buildBrand = deviceInfo.buildBrand ?: ""
                    buildHardware = deviceInfo.buildHardware ?: ""
                    buildBoard = deviceInfo.buildBoard ?: ""
                    buildId = deviceInfo.buildId ?: ""
                    buildDisplay = deviceInfo.buildDisplay ?: ""
                    buildType = deviceInfo.buildType ?: ""
                    buildTags = deviceInfo.buildTags ?: ""
                    buildVersionRelease = deviceInfo.buildVersionRelease ?: ""
                    buildVersionIncremental = deviceInfo.buildVersionIncremental ?: ""
                    buildVersionCodename = deviceInfo.buildVersionCodename ?: ""
                    buildVersionSecurityPatch = deviceInfo.buildVersionSecurityPatch ?: ""
                    simSubscriberId = deviceInfo.simSubscriberId ?: ""
                    simCountry = deviceInfo.simCountry ?: ""
                    simMnc = deviceInfo.simMnc ?: ""
                }
            }
        }
    }

    // Validation states
    val isProfileNameValid = profileName.isNotBlank()
    val isImei1Valid = imei1.isBlank() || imei1.matches(Regex("\\d{15}"))
    val isImei2Valid = imei2.isBlank() || imei2.matches(Regex("\\d{15}"))
    val isAndroidIdValid = androidId.isBlank() || androidId.matches(Regex("[0-9a-f]{16}"))
    val isWifiMacValid = wifiMac.isBlank() || wifiMac.matches(Regex("[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}"))
    val isWifiBssidValid = wifiBssid.isBlank() || wifiBssid.matches(Regex("[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}"))
    val isBluetoothMacValid = bluetoothMac.isBlank() || bluetoothMac.matches(Regex("[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}"))
    val isSimSerialValid = simSerial.isBlank() || simSerial.matches(Regex("\\d{19,20}"))
    val isMobileNumberValid = mobileNumber.isBlank() || mobileNumber.matches(Regex("\\+?\\d{10,15}"))
    val isAdsIdValid = adsId.isBlank() || adsId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
    val isGsfIdValid = gsfId.isBlank() || gsfId.matches(Regex("[0-9a-f]{16}"))
    val isBuildTypeValid = buildType.isBlank() || buildType.matches(Regex("user|userdebug|eng"))
    val isBuildVersionReleaseValid = buildVersionRelease.isBlank() || buildVersionRelease.matches(Regex("\\d+\\.\\d+\\.?\\d*"))
    val isBuildVersionSecurityPatchValid = buildVersionSecurityPatch.isBlank() || buildVersionSecurityPatch.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    val isSimSubscriberIdValid = simSubscriberId.isBlank() || simSubscriberId.matches(Regex("\\d{15,20}"))
    val isSimCountryValid = simCountry.isBlank() || simCountry.matches(Regex("[a-zA-Z]{2}"))
    val isSimMncValid = simMnc.isBlank() || simMnc.matches(Regex("\\d{5,6}|\\d{2,3}"))

    val isFormValid = isProfileNameValid &&
            isImei1Valid && isImei2Valid && isAndroidIdValid &&
            isWifiMacValid && isWifiBssidValid && isBluetoothMacValid &&
            isSimSerialValid && isMobileNumberValid && isAdsIdValid &&
            isGsfIdValid && isBuildTypeValid && isBuildVersionReleaseValid &&
            isBuildVersionSecurityPatchValid && isSimSubscriberIdValid &&
            isSimCountryValid && isSimMncValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (profileId == null) "Create Profile" else "Edit Profile",
            style = MaterialTheme.typography.headlineSmall
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = profileName,
            onValueChange = { profileName = it },
            label = { Text("Profile Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isProfileNameValid,
            supportingText = { if (!isProfileNameValid) Text("Profile name is required") }
        )

        OutlinedTextField(
            value = imei1,
            onValueChange = { imei1 = it },
            label = { Text("IMEI 1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = !isImei1Valid,
            supportingText = { if (!isImei1Valid) Text("Must be 15 digits") }
        )

        OutlinedTextField(
            value = imei2,
            onValueChange = { imei2 = it },
            label = { Text("IMEI 2") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = !isImei2Valid,
            supportingText = { if (!isImei2Valid) Text("Must be 15 digits") }
        )

        OutlinedTextField(
            value = androidId,
            onValueChange = { androidId = it },
            label = { Text("Android ID") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isAndroidIdValid,
            supportingText = { if (!isAndroidIdValid) Text("Must be 16 hex characters") }
        )

        OutlinedTextField(
            value = buildFingerprint,
            onValueChange = { buildFingerprint = it },
            label = { Text("Build Fingerprint") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildDevice,
            onValueChange = { buildDevice = it },
            label = { Text("Build Device") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = wifiMac,
            onValueChange = { wifiMac = it },
            label = { Text("Wi-Fi MAC") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isWifiMacValid,
            supportingText = { if (!isWifiMacValid) Text("Must be XX:XX:XX:XX:XX:XX") }
        )

        OutlinedTextField(
            value = wifiSsid,
            onValueChange = { wifiSsid = it },
            label = { Text("Wi-Fi SSID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = wifiBssid,
            onValueChange = { wifiBssid = it },
            label = { Text("Wi-Fi BSSID") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isWifiBssidValid,
            supportingText = { if (!isWifiBssidValid) Text("Must be XX:XX:XX:XX:XX:XX") }
        )

        OutlinedTextField(
            value = bluetoothMac,
            onValueChange = { bluetoothMac = it },
            label = { Text("Bluetooth MAC") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isBluetoothMacValid,
            supportingText = { if (!isBluetoothMacValid) Text("Must be XX:XX:XX:XX:XX:XX") }
        )

        OutlinedTextField(
            value = buildProduct,
            onValueChange = { buildProduct = it },
            label = { Text("Build Product") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = simSerial,
            onValueChange = { simSerial = it },
            label = { Text("SIM Serial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = !isSimSerialValid,
            supportingText = { if (!isSimSerialValid) Text("Must be 19-20 digits") }
        )

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = !isMobileNumberValid,
            supportingText = { if (!isMobileNumberValid) Text("Must be 10-15 digits, optional +") }
        )

        OutlinedTextField(
            value = simOperator,
            onValueChange = { simOperator = it },
            label = { Text("SIM Operator Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = adsId,
            onValueChange = { adsId = it },
            label = { Text("Advertising ID") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isAdsIdValid,
            supportingText = { if (!isAdsIdValid) Text("Must be a valid UUID") }
        )

        OutlinedTextField(
            value = mediaDrmId,
            onValueChange = { mediaDrmId = it },
            label = { Text("Media DRM ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gsfId,
            onValueChange = { gsfId = it },
            label = { Text("GSF ID") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isGsfIdValid,
            supportingText = { if (!isGsfIdValid) Text("Must be 16 hex characters") }
        )

        OutlinedTextField(
            value = buildBrand,
            onValueChange = { buildBrand = it },
            label = { Text("Build Brand") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildHardware,
            onValueChange = { buildHardware = it },
            label = { Text("Build Hardware") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildBoard,
            onValueChange = { buildBoard = it },
            label = { Text("Build Board") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildId,
            onValueChange = { buildId = it },
            label = { Text("Build ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildDisplay,
            onValueChange = { buildDisplay = it },
            label = { Text("Build Display") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildType,
            onValueChange = { buildType = it },
            label = { Text("Build Type") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isBuildTypeValid,
            supportingText = { if (!isBuildTypeValid) Text("Must be user, userdebug, or eng") }
        )

        OutlinedTextField(
            value = buildTags,
            onValueChange = { buildTags = it },
            label = { Text("Build Tags") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildVersionRelease,
            onValueChange = { buildVersionRelease = it },
            label = { Text("Build Version Release") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isBuildVersionReleaseValid,
            supportingText = { if (!isBuildVersionReleaseValid) Text("Must be X.Y or X.Y.Z") }
        )

        OutlinedTextField(
            value = buildVersionIncremental,
            onValueChange = { buildVersionIncremental = it },
            label = { Text("Build Version Incremental") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildVersionCodename,
            onValueChange = { buildVersionCodename = it },
            label = { Text("Build Version Codename") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = buildVersionSecurityPatch,
            onValueChange = { buildVersionSecurityPatch = it },
            label = { Text("Build Version Security Patch") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isBuildVersionSecurityPatchValid,
            supportingText = { if (!isBuildVersionSecurityPatchValid) Text("Must be YYYY-MM-DD") }
        )

        OutlinedTextField(
            value = simSubscriberId,
            onValueChange = { simSubscriberId = it },
            label = { Text("SIM Subscriber ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = !isSimSubscriberIdValid,
            supportingText = { if (!isSimSubscriberIdValid) Text("Must be 15-20 digits") }
        )

        OutlinedTextField(
            value = simCountry,
            onValueChange = { simCountry = it },
            label = { Text("SIM Country ISO") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isSimCountryValid,
            supportingText = { if (!isSimCountryValid) Text("Must be 2-letter ISO code") }
        )

        OutlinedTextField(
            value = simMnc,
            onValueChange = { simMnc = it },
            label = { Text("SIM MCC+MNC or MNC") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = !isSimMncValid,
            supportingText = { if (!isSimMncValid) Text("Must be 5-6 digits (MCC+MNC) or 2-3 digits (MNC)") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    when {
                        !isProfileNameValid -> errorMessage = "Profile name is required"
                        !isImei1Valid -> errorMessage = "IMEI 1 must be 15 digits"
                        !isImei2Valid -> errorMessage = "IMEI 2 must be 15 digits"
                        !isAndroidIdValid -> errorMessage = "Android ID must be 16 hexadecimal characters"
                        !isWifiMacValid -> errorMessage = "Wi-Fi MAC must be in format XX:XX:XX:XX:XX:XX"
                        !isWifiBssidValid -> errorMessage = "Wi-Fi BSSID must be in format XX:XX:XX:XX:XX:XX"
                        !isBluetoothMacValid -> errorMessage = "Bluetooth MAC must be in format XX:XX:XX:XX:XX:XX"
                        !isSimSerialValid -> errorMessage = "SIM Serial must be 19-20 digits"
                        !isMobileNumberValid -> errorMessage = "Mobile Number must be 10-15 digits, optionally starting with +"
                        !isAdsIdValid -> errorMessage = "Advertising ID must be a valid UUID"
                        !isGsfIdValid -> errorMessage = "GSF ID must be 16 hexadecimal characters"
                        !isBuildTypeValid -> errorMessage = "Build Type must be user, userdebug, or eng"
                        !isBuildVersionReleaseValid -> errorMessage = "Build Version Release must be in format X.Y or X.Y.Z"
                        !isBuildVersionSecurityPatchValid -> errorMessage = "Security Patch must be in format YYYY-MM-DD"
                        !isSimSubscriberIdValid -> errorMessage = "SIM Subscriber ID must be 15-20 digits"
                        !isSimCountryValid -> errorMessage = "SIM Country ISO must be a 2-letter code"
                        !isSimMncValid -> errorMessage = "SIM MCC+MNC must be 5-6 digits or MNC must be 2-3 digits"
                        else -> {
                            val deviceInfo = DeviceInfo.newBuilder()
                                .setImei1(imei1.takeIf { it.isNotBlank() })
                                .setImei2(imei2.takeIf { it.isNotBlank() })
                                .setAndroidId(androidId.takeIf { it.isNotBlank() })
                                .setBuildFingerprint(buildFingerprint.takeIf { it.isNotBlank() })
                                .setBuildDevice(buildDevice.takeIf { it.isNotBlank() })
                                .setWifiMac(wifiMac.takeIf { it.isNotBlank() })
                                .setWifiSsid(wifiSsid.takeIf { it.isNotBlank() })
                                .setWifiBssid(wifiBssid.takeIf { it.isNotBlank() })
                                .setBluetoothMac(bluetoothMac.takeIf { it.isNotBlank() })
                                .setBuildProduct(buildProduct.takeIf { it.isNotBlank() })
                                .setSimSerial(simSerial.takeIf { it.isNotBlank() })
                                .setMobileNumber(mobileNumber.takeIf { it.isNotBlank() })
                                .setSimOperator(simOperator.takeIf { it.isNotBlank() })
                                .setAdsId(adsId.takeIf { it.isNotBlank() })
                                .setMediaDrmId(mediaDrmId.takeIf { it.isNotBlank() })
                                .setGsfId(gsfId.takeIf { it.isNotBlank() })
                                .setBuildBrand(buildBrand.takeIf { it.isNotBlank() })
                                .setBuildHardware(buildHardware.takeIf { it.isNotBlank() })
                                .setBuildBoard(buildBoard.takeIf { it.isNotBlank() })
                                .setBuildId(buildId.takeIf { it.isNotBlank() })
                                .setBuildDisplay(buildDisplay.takeIf { it.isNotBlank() })
                                .setBuildType(buildType.takeIf { it.isNotBlank() })
                                .setBuildTags(buildTags.takeIf { it.isNotBlank() })
                                .setBuildVersionRelease(buildVersionRelease.takeIf { it.isNotBlank() })
                                .setBuildVersionIncremental(buildVersionIncremental.takeIf { it.isNotBlank() })
                                .setBuildVersionCodename(buildVersionCodename.takeIf { it.isNotBlank() })
                                .setBuildVersionSecurityPatch(buildVersionSecurityPatch.takeIf { it.isNotBlank() })
                                .setSimSubscriberId(simSubscriberId.takeIf { it.isNotBlank() })
                                .setSimCountry(simCountry.takeIf { it.isNotBlank() })
                                .setSimMnc(simMnc.takeIf { it.isNotBlank() })
                                .build()

                            val profile = Profile.newBuilder()
                                .setName(profileName)
                                .setDeviceInfo(deviceInfo)
                                .build()

                            try {
                                if (profileId == null) {
                                    profileManager.addProfile(profile)
                                    Logger.log("ProfileEditorScreen: Added new profile: ${profile.name}")
                                } else {
                                    profileManager.updateProfile(profileId, profile)
                                    Logger.log("ProfileEditorScreen: Updated profile: ${profile.name}")
                                }
                                onSave()
                            } catch (e: Exception) {
                                errorMessage = "Failed to save profile: ${e.message}"
                                Logger.error("ProfileEditorScreen: Failed to save profile", e)
                            }
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            if (profileId != null) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Profile") },
                text = { Text("Are you sure you want to delete this profile? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                profileManager.deleteProfile(profileId!!)
                                Logger.log("ProfileEditorScreen: Deleted profile: $profileId")
                                showDeleteDialog = false
                                onCancel()
                            } catch (e: Exception) {
                                errorMessage = "Failed to delete profile: ${e.message}"
                                Logger.error("ProfileEditorScreen: Failed to delete profile", e)
                                showDeleteDialog = false
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}