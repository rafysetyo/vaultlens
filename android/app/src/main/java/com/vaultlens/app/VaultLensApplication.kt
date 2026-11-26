package com.vaultlens.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.initialize

class VaultLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)

        // Configure Firebase App Check (Debug Provider for testing/judging, Play Integrity for release)
        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        // Enable Cloud Firestore Offline Persistence so sanitized summaries and offline queues survive disconnects
        val settings = firestoreSettings {
            setLocalCacheSettings(
                persistentCacheSettings {
                    setSizeBytes(100L * 1024L * 1024L)
                }
            )
        }
        Firebase.firestore.firestoreSettings = settings
    }
}
