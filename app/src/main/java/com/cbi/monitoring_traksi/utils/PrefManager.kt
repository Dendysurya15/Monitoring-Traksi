package com.cbi.monitoring_traksi.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager(_context: Context) {
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    var privateMode = 0

    var locNotifWl: String?
        get() = pref.getString(LOC_NOTIF_TAG, "")
        set(loc_notif) {
            editor.putString(LOC_NOTIF_TAG, loc_notif)
            editor.commit()
        }

    var setNotifWl: String?
        get() = pref.getString(NOTIF_TAG, "true;true;true")
        set(notifWl) {
            editor.putString(NOTIF_TAG, notifWl)
            editor.commit()
        }

    var isFromWidget: Boolean
        get() = pref.getBoolean(WIDGET_TAG, false)
        set(isFromWidget) {
            editor.putBoolean(WIDGET_TAG, isFromWidget)
            editor.commit()
        }

    var remember: Boolean
        get() = pref.getBoolean(REMEMBERME, false)
        set(rememberMe) {
            editor.putBoolean(REMEMBERME, rememberMe)
            editor.commit()
        }

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    var versionQC: Int
        get() = pref.getInt(version_tagQC, 0)
        set(versionQcCount) {
            editor.putInt(version_tagQC, versionQcCount)
            editor.commit()
        }

    var version: Int
        get() = pref.getInt(version_tag, 0)
        set(versionCount) {
            editor.putInt(version_tag, versionCount)
            editor.commit()
        }

    var idWl: Int
        get() = pref.getInt("id_wl", 0)
        set(idWlCount) {
            editor.putInt("id_wl", idWlCount)
            editor.commit()
        }

    var idReg: Int
        get() = pref.getInt("id_reg", 0)
        set(idRegCount) {
            editor.putInt("id_reg", idRegCount)
            editor.commit()
        }

    var countWl: Int
        get() = pref.getInt(COUNT_WL, 0)
        set(countWl) {
            editor.putInt(COUNT_WL, countWl)
            editor.commit()
        }

    var reg: String?
        get() = pref.getString(reg_tag, "")
        set(regCount) {
            editor.putString(reg_tag, regCount)
            editor.commit()
        }

    var lastUpdate: String?
        get() = pref.getString("update", "blm update")
        set(update) {
            editor.putString("update", update)
            editor.commit()
        }

    var dataReg: String?
        get() = pref.getString("data_reg", "")
        set(dataRegCount) {
            editor.putString("data_reg", dataRegCount)
            editor.commit()
        }

    var login: Int
        get() = pref.getInt(LOGIN, 0)
        set(isLogged) {
            editor.putInt(LOGIN, isLogged)
            editor.commit()
        }

    var session: Boolean
        get() = pref.getBoolean(SESSION, false)
        set(sessionActive) {
            editor.putBoolean(SESSION, sessionActive)
            editor.commit()
        }

    var name: String?
        get() = pref.getString(NAME, "")
        set(sureName) {
            editor.putString(NAME, sureName)
            editor.commit()
        }

    var userid: String?
        get() = pref.getString(USERID, "")
        set(userId) {
            editor.putString(USERID, userId)
            editor.commit()
        }

    var departemen: String?
        get() = pref.getString(DEPARTEMEN, "")
        set(dept) {
            editor.putString(DEPARTEMEN, dept)
            editor.commit()
        }

    var lokasi_kerja: String?
        get() = pref.getString(LOKASI, "")
        set(lokker) {
            editor.putString(LOKASI, lokker)
            editor.commit()
        }

    var jabatan: String?
        get() = pref.getString(JABATAN, "")
        set(jabtn) {
            editor.putString(JABATAN, jabtn)
            editor.commit()
        }

    var nohp: String?
        get() = pref.getString(NOHP, "")
        set(hp) {
            editor.putString(NOHP, hp)
            editor.commit()
        }

    var email: String?
        get() = pref.getString(EMAIL, "")
        set(mail) {
            editor.putString(EMAIL, mail)
            editor.commit()
        }

    var password: String?
        get() = pref.getString(PASSWORD, "")
        set(pass) {
            editor.putString(PASSWORD, pass)
            editor.commit()
        }

    var lokasi: String?
        get() = pref.getString(LOKASI, "")
        set(lokasi) {
            editor.putString(LOKASI, lokasi)
            editor.commit()
        }

    var akses: String?
        get() = pref.getString(AKSES, "")
        set(akses) {
            editor.putString(AKSES, akses)
            editor.commit()
        }

    var estateForm: String?
        get() = pref.getString(ESTATE_FORM, "")
        set(estateForm) {
            editor.putString(ESTATE_FORM, estateForm)
            editor.commit()
        }

    var afdeling: String?
        get() = pref.getString(AFDELING, "")
        set(afdeling) {
            editor.putString(AFDELING, afdeling)
            editor.commit()
        }

    var hexMaps: String?
        get() = pref.getString("hexMaps", "")
        set(hexMaps) {
            editor.putString("hexMaps", hexMaps)
            editor.commit()
        }

    var hexDataWl: String?
        get() = pref.getString("hexDataWl", "")
        set(hexDataWl) {
            editor.putString("hexDataWl", hexDataWl)
            editor.commit()
        }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "ssms_water_management"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val LOGIN = "Login"
        private const val SESSION = "Session"


        const val REMEMBERME = "remember_me"
        const val LOC_NOTIF_TAG = "loc_notif_act"
        const val NOTIF_TAG = "notif_act"
        const val WIDGET_TAG = "widget_act"
        const val version_tagQC = "version_qc"
        const val version_tag = "version"
        const val USERID = "user_id"
        const val NAME = "nama_lengkap"
        const val DEPARTEMEN = "departemen"
        const val JABATAN = "jabatan"
        const val NOHP = "no_hp"
        const val EMAIL = "email"
        const val LOKASI = "lokasi_kerja"
        const val AKSES = "akses_level"
        const val PASSWORD = "password"
        const val ESTATE_FORM = "estate_form"
        const val COUNT_WL = "count_wl"
        const val reg_tag = "reg"

        //DATA BLOK
        const val AFDELING = "afdeling"

    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
    }
}