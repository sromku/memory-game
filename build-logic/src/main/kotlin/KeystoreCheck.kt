import java.io.File
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.X509Certificate

/** Inspects an Android signing keystore and tests passwords without ever printing them. */
object KeystoreCheck {

    /** Aliases and certificates; a JKS file lists these without its password (integrity unchecked). */
    fun describe(keystore: File): String = buildString {
        val ks = KeyStore.getInstance("JKS").apply { keystore.inputStream().use { load(it, null) } }
        appendLine("== $keystore (${ks.type}, integrity not checked)")
        for (alias in ks.aliases().toList()) {
            val kind = if (ks.isKeyEntry(alias)) "private key" else "certificate"
            appendLine("alias: $alias  created: ${ks.getCreationDate(alias)}  type: $kind")
            (ks.getCertificate(alias) as? X509Certificate)?.let { cert ->
                appendLine("  owner:   ${cert.subjectX500Principal}")
                appendLine("  valid:   ${cert.notBefore} -> ${cert.notAfter}")
                appendLine("  SHA-256: ${fingerprint(cert.encoded, "SHA-256")}")
                appendLine("  SHA-1:   ${fingerprint(cert.encoded, "SHA-1")}")
            }
        }
    }

    /** Tries each candidate as the store password, then as the key password of every key. Reports line numbers only. */
    fun tryCandidates(keystore: File, candidates: List<String>): String = buildString {
        appendLine("== Trying ${candidates.size} candidate password(s)")
        val storeIndex = candidates.indexOfFirst { open(keystore, it) != null }
        if (storeIndex < 0) return appendLine("FAIL  none of the candidates is the store password").toString()
        appendLine("PASS  store password = candidate #${storeIndex + 1} (line ${storeIndex + 1} of the file)")
        val ks = open(keystore, candidates[storeIndex])!!
        for (alias in ks.aliases().toList().filter(ks::isKeyEntry)) {
            val keyIndex = candidates.indexOfFirst { unlocks(ks, alias, it) }
            if (keyIndex >= 0) appendLine("PASS  key password for '$alias' = candidate #${keyIndex + 1}")
            else appendLine("FAIL  none of the candidates unlocks the key '$alias'")
        }
    }

    /** Verifies a configured store password, alias and key password. Returns null when everything passes. */
    fun verify(keystore: File, storePassword: String, alias: String, keyPassword: String): String? {
        val ks = open(keystore, storePassword) ?: return "FAIL  store password is wrong"
        if (!ks.containsAlias(alias)) return "FAIL  no alias '$alias' (aliases: ${ks.aliases().toList()})"
        if (!unlocks(ks, alias, keyPassword)) return "FAIL  key password is wrong for alias '$alias'"
        return null
    }

    private fun open(keystore: File, password: String): KeyStore? = try {
        KeyStore.getInstance("JKS").apply { keystore.inputStream().use { load(it, password.toCharArray()) } }
    } catch (_: java.io.IOException) {
        null
    }

    private fun unlocks(ks: KeyStore, alias: String, password: String) = try {
        ks.getKey(alias, password.toCharArray()) != null
    } catch (_: GeneralSecurityException) {
        false
    }

    private fun fingerprint(encoded: ByteArray, algorithm: String) =
        MessageDigest.getInstance(algorithm).digest(encoded).joinToString(":") { "%02X".format(it) }
}
