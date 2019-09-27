package de.p72b.mocklation.service.setting

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import de.p72b.mocklation.util.SecuredConstants

import ru.bullyboo.encoder.Encoder
import ru.bullyboo.encoder.builders.BuilderAES
import ru.bullyboo.encoder.methods.AES

class Setting(context: Context) : ISetting {
    companion object {
        private const val PERMISSION_LOCATION = "PERMISSION_LOCATION"
        private const val SHARED_PREFS_FILE = "omagu.settings"
        private const val LAST_POSITION_LAT = "LAST_POSITION_LAT"
        private const val LAST_POSITION_LNG = "LAST_POSITION_LNG"
        private const val ACTIVE_MOCK_LOCATION_CODE = "ACTIVE_MOCK_LOCATION_CODE"
        private const val LAST_SELECTED_LOCATION_CODE = "LAST_SELECTED_LOCATION_CODE"
        private const val PRIVACY_UPDATE_ACCEPTED = "PRIVACY_UPDATE_ACCEPTED"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)
    private var encryption: BuilderAES = Encoder.BuilderAES()
            .method(AES.Method.AES_CBC_PKCS5PADDING)
            .key(SecuredConstants.ENCRYPTION_KEY)
            .keySize(AES.Key.SIZE_128)
            .iVector(SecuredConstants.ENCRYPTION_I_VECTOR)

    override fun saveLocation(latitude: Double, longitude: Double) {
        val edit = preferences.edit()
        edit.putFloat(LAST_POSITION_LAT, latitude.toFloat())
        edit.putFloat(LAST_POSITION_LNG, longitude.toFloat())
        edit.apply()
    }

    override fun setPermissionDecision(permissionKey: String, decision: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(permissionKey, decision)
        edit.apply()
    }

    override fun getPermissionDecision(permission: String): Boolean {
        return preferences.getBoolean(resolvePermissionPreferencesKey(permission), false)
    }

    private fun resolvePermissionPreferencesKey(permission: String): String? {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> Setting.PERMISSION_LOCATION
            else -> null
        }
    }

    override fun setMockLocationItemCode(code: String?) {
        val edit = preferences.edit()
        edit.putString(ACTIVE_MOCK_LOCATION_CODE, code)
        edit.apply()
    }

    override fun saveLastPressedLocation(code: String) {
        val edit = preferences.edit()
        edit.putString(LAST_SELECTED_LOCATION_CODE, code)
        edit.apply()
    }

    override fun getLastPressedLocationCode(): String? {
        return preferences.getString(LAST_SELECTED_LOCATION_CODE, null)
    }

    override fun getMockLocationItemCode(): String? {
        return preferences.getString(ACTIVE_MOCK_LOCATION_CODE, null)
    }

    override fun isPrivacyStatementAccepted(): Boolean {
        return preferences.getBoolean(PRIVACY_UPDATE_ACCEPTED, false)
    }

    override fun acceptCurrentPrivacyStatement() {
        val edit = preferences.edit()
        edit.putBoolean(PRIVACY_UPDATE_ACCEPTED, true)
        edit.apply()
    }
}
