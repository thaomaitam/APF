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
    onCancel: () -> Unit,
    onDelete: () -> Unit
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load existing profile if editing
    LaunchedEffect(profileId) {
        if (profileId != null) {
            val profile = profileManager.getProfileById(profileId)
            profile?.let {
                profileName = it.name
                it.deviceInfo.let { deviceInfo ->
                    imei1 = deviceInfo.imei1
                    imei2 = deviceInfo.imei2
                    androidId = deviceInfo.androidId
                    buildFingerprint = deviceInfo.buildFingerprint
                    buildDevice = deviceInfo.buildDevice
                    wifiMac = deviceInfo.wifiMac
                    wifiSsid = deviceInfo.wifiSsid
                    wifiBssid = deviceInfo.wifiBssid
                    bluetoothMac = deviceInfo.bluetoothMac
                    buildProduct = deviceInfo.buildProduct
                    simSerial = deviceInfo.simSerial
                    mobileNumber = deviceInfo.mobileNumber
                    simOperator = deviceInfo.simOperator
                    adsId = deviceInfo.adsId
                    mediaDrmId = deviceInfo.mediaDrmId
                    gsfId = deviceInfo.gsfId
                    buildBrand = deviceInfo.buildBrand
                    buildHardware = deviceInfo.buildHardware
                    buildBoard = deviceInfo.buildBoard
                    buildId = deviceInfo.buildId
                    buildDisplay = deviceInfo.buildDisplay
                    buildType = deviceInfo.buildType
                    buildTags = deviceInfo.buildTags
                    buildVersionRelease = deviceInfo.buildVersionRelease
                    buildVersionIncremental = deviceInfo.buildVersionIncremental
                    buildVersionCodename = deviceInfo.buildVersionCodename
                    buildVersionSecurityPatch = deviceInfo.buildVersionSecurityPatch
                    simSubscriberId = deviceInfo.simSubscriberId
                    simCountry = deviceInfo.simCountry
                    simMnc = deviceInfo.simMnc
                }
            }
        }
    }

    // Validation states
    val isProfileNameValid = profileName.isNotBlank()
    val isImei1Valid = imei1.isBlank() || imei1.matches(Regex("\\d{15}"))
    val isImei2Valid = imei2.isBlank() || imei2.matches(Regex("\\d{15}"))
    val isAndroidIdValid = androidId.isBlank() || androidId.matches(Regex("[0-9a-f]{16}"))
    val isWifiMacValid = wifiMac.isBlank() || wifiMac.matches(Regex("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"))
    val isWifiBssidValid = wifiBssid.isBlank() || wifiBssid.matches(Regex("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"))
    val isBluetoothMacValid = bluetoothMac.isBlank() || bluetoothMac.matches(Regex("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"))
    val isSimSerialValid = simSerial.isBlank() || simSerial.matches(Regex("\\d{19,20}"))
    val isMobileNumberValid = mobileNumber.isBlank() || mobileNumber.matches(Regex("\\+?\\d{10,15}"))
    val isAdsIdValid = adsId.isBlank() || adsId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
    val isGsfIdValid = gsfId.isBlank() || gsfId.matches(Regex("[0-9a-f]{16}"))
    val isBuildTypeValid = buildType.isBlank() || buildType.matches(Regex("user|userdebug|eng"))
    val isSimSubscriberIdValid = simSubscriberId.isBlank() || simSubscriberId.matches(Regex("\\d{15,20}"))
    val isSimCountryValid = simCountry.isBlank() || simCountry.matches(Regex("[a-zA-Z]{2}"))
    val isSimMncValid = simMnc.isBlank() || simMnc.matches(Regex("\\d{5,6}|\\d{2,3}"))

    val isFormValid = isProfileNameValid && isImei1Valid && isImei2Valid && isAndroidIdValid &&
            isWifiMacValid && isWifiBssidValid && isBluetoothMacValid &&
            isSimSerialValid && isMobileNumberValid && isAdsIdValid &&
            isGsfIdValid && isBuildTypeValid && isSimSubscriberIdValid &&
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

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // --- Input Fields ---
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

        OutlinedTextField(value = buildFingerprint, onValueChange = { buildFingerprint = it }, label = { Text("Build Fingerprint") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildDevice, onValueChange = { buildDevice = it }, label = { Text("Build Device") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildProduct, onValueChange = { buildProduct = it }, label = { Text("Build Product") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildBrand, onValueChange = { buildBrand = it }, label = { Text("Build Brand") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildHardware, onValueChange = { buildHardware = it }, label = { Text("Build Hardware") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildBoard, onValueChange = { buildBoard = it }, label = { Text("Build Board") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildId, onValueChange = { buildId = it }, label = { Text("Build ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildDisplay, onValueChange = { buildDisplay = it }, label = { Text("Build Display") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildTags, onValueChange = { buildTags = it }, label = { Text("Build Tags") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildType, onValueChange = { buildType = it }, label = { Text("Build Type") }, modifier = Modifier.fillMaxWidth(), isError = !isBuildTypeValid, supportingText = { if (!isBuildTypeValid) Text("user, userdebug, or eng") })
        OutlinedTextField(value = buildVersionRelease, onValueChange = { buildVersionRelease = it }, label = { Text("Build Version Release") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildVersionIncremental, onValueChange = { buildVersionIncremental = it }, label = { Text("Build Version Incremental") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildVersionCodename, onValueChange = { buildVersionCodename = it }, label = { Text("Build Version Codename") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = buildVersionSecurityPatch, onValueChange = { buildVersionSecurityPatch = it }, label = { Text("Build Version Security Patch") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = wifiMac, onValueChange = { wifiMac = it }, label = { Text("Wi-Fi MAC") }, modifier = Modifier.fillMaxWidth(), isError = !isWifiMacValid, supportingText = { if (!isWifiMacValid) Text("XX:XX:XX:XX:XX:XX") })
        OutlinedTextField(value = wifiSsid, onValueChange = { wifiSsid = it }, label = { Text("Wi-Fi SSID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = wifiBssid, onValueChange = { wifiBssid = it }, label = { Text("Wi-Fi BSSID") }, modifier = Modifier.fillMaxWidth(), isError = !isWifiBssidValid, supportingText = { if (!isWifiBssidValid) Text("XX:XX:XX:XX:XX:XX") })
        OutlinedTextField(value = bluetoothMac, onValueChange = { bluetoothMac = it }, label = { Text("Bluetooth MAC") }, modifier = Modifier.fillMaxWidth(), isError = !isBluetoothMacValid, supportingText = { if (!isBluetoothMacValid) Text("XX:XX:XX:XX:XX:XX") })

        OutlinedTextField(value = simSerial, onValueChange = { simSerial = it }, label = { Text("SIM Serial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = !isSimSerialValid, supportingText = { if (!isSimSerialValid) Text("19-20 digits") })
        OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("Mobile Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), isError = !isMobileNumberValid, supportingText = { if (!isMobileNumberValid) Text("10-15 digits, optional +") })
        OutlinedTextField(value = simOperator, onValueChange = { simOperator = it }, label = { Text("SIM Operator Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = simSubscriberId, onValueChange = { simSubscriberId = it }, label = { Text("SIM Subscriber ID (IMSI)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = !isSimSubscriberIdValid, supportingText = { if (!isSimSubscriberIdValid) Text("15-20 digits") })
        OutlinedTextField(value = simCountry, onValueChange = { simCountry = it }, label = { Text("SIM Country ISO") }, modifier = Modifier.fillMaxWidth(), isError = !isSimCountryValid, supportingText = { if (!isSimCountryValid) Text("2-letter ISO code") })
        OutlinedTextField(value = simMnc, onValueChange = { simMnc = it }, label = { Text("SIM MCC+MNC or MNC") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = !isSimMncValid, supportingText = { if (!isSimMncValid) Text("5-6 digits (MCC+MNC) or 2-3 (MNC)") })

        OutlinedTextField(value = adsId, onValueChange = { adsId = it }, label = { Text("Advertising ID") }, modifier = Modifier.fillMaxWidth(), isError = !isAdsIdValid, supportingText = { if (!isAdsIdValid) Text("Valid UUID format") })
        OutlinedTextField(value = mediaDrmId, onValueChange = { mediaDrmId = it }, label = { Text("Media DRM ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gsfId, onValueChange = { gsfId = it }, label = { Text("GSF ID") }, modifier = Modifier.fillMaxWidth(), isError = !isGsfIdValid, supportingText = { if (!isGsfIdValid) Text("16 hex characters") })

        // --- Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isFormValid) {
                        val deviceInfo = DeviceInfo.newBuilder()
                            .setImei1(imei1)
                            .setImei2(imei2)
                            .setAndroidId(androidId)
                            .setBuildFingerprint(buildFingerprint)
                            .setBuildDevice(buildDevice)
                            .setWifiMac(wifiMac)
                            .setWifiSsid(wifiSsid)
                            .setWifiBssid(wifiBssid)
                            .setBluetoothMac(bluetoothMac)
                            .setBuildProduct(buildProduct)
                            .setSimSerial(simSerial)
                            .setMobileNumber(mobileNumber)
                            .setSimOperator(simOperator)
                            .setAdsId(adsId)
                            .setMediaDrmId(mediaDrmId)
                            .setGsfId(gsfId)
                            .setBuildBrand(buildBrand)
                            .setBuildHardware(buildHardware)
                            .setBuildBoard(buildBoard)
                            .setBuildId(buildId)
                            .setBuildDisplay(buildDisplay)
                            .setBuildType(buildType)
                            .setBuildTags(buildTags)
                            .setBuildVersionRelease(buildVersionRelease)
                            .setBuildVersionIncremental(buildVersionIncremental)
                            .setBuildVersionCodename(buildVersionCodename)
                            .setBuildVersionSecurityPatch(buildVersionSecurityPatch)
                            .setSimSubscriberId(simSubscriberId)
                            .setSimCountry(simCountry)
                            .setSimMnc(simMnc)
                            .build()

                        val profileBuilder = Profile.newBuilder()
                            .setName(profileName)
                            .setDeviceInfo(deviceInfo)

                        try {
                            if (profileId == null) {
                                // Add new profile (ID will be generated by ProfileManager)
                                profileManager.addProfile(profileBuilder.build())
                                Logger.log("ProfileEditorScreen: Added new profile: $profileName")
                            } else {
                                // Update existing profile
                                val updatedProfile = profileBuilder.setId(profileId).build()
                                profileManager.updateProfile(updatedProfile)
                                Logger.log("ProfileEditorScreen: Updated profile: $profileName")
                            }
                            onSave()
                        } catch (e: Exception) {
                            errorMessage = "Failed to save profile: ${e.message}"
                            Logger.error("ProfileEditorScreen: Failed to save profile", e)
                        }
                    } else {
                        errorMessage = "Please fix the errors before saving."
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
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
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
                text = { Text("Are you sure you want to delete this profile? This action cannot be undone and will unmap it from all apps.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                profileManager.deleteProfile(profileId!!)
                                Logger.log("ProfileEditorScreen: Deleted profile: $profileId")
                                showDeleteDialog = false
                                onDelete() // Navigate back after deletion
                            } catch (e: Exception) {
                                errorMessage = "Failed to delete profile: ${e.message}"
                                Logger.error("ProfileEditorScreen: Failed to delete profile", e)
                                showDeleteDialog = false
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
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