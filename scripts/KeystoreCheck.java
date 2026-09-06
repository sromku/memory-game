import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Inspects an Android signing keystore and tests candidate passwords without ever printing them.
 *
 *   java scripts/KeystoreCheck.java <keystore>                      list aliases and certificates (no password)
 *   java scripts/KeystoreCheck.java <keystore> --try <file>         try each line of <file> as store password,
 *                                                                   then as key password for every alias
 *   java scripts/KeystoreCheck.java <keystore> --props <gradle.properties>
 *                                                                   check MEMORY_GAME_STORE_PASSWORD / KEY_ALIAS /
 *                                                                   KEY_PASSWORD from a properties file
 */
public class KeystoreCheck {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) usage();
        File file = new File(args[0]);
        if (!file.isFile()) fail("No such keystore: " + file);

        listUnauthenticated(file);

        if (args.length == 3 && args[1].equals("--try")) {
            List<String> candidates = new ArrayList<>();
            for (String line : Files.readAllLines(Paths.get(args[2]))) if (!line.isEmpty()) candidates.add(line);
            System.out.printf("%n== Trying %d candidate password(s) from %s%n", candidates.size(), args[2]);
            tryCandidates(file, candidates);
        } else if (args.length == 3 && args[1].equals("--props")) {
            Properties p = new Properties();
            try (Reader r = new FileReader(args[2])) { p.load(r); }
            String store = p.getProperty("MEMORY_GAME_STORE_PASSWORD", "");
            String alias = p.getProperty("MEMORY_GAME_KEY_ALIAS", "");
            String key = p.getProperty("MEMORY_GAME_KEY_PASSWORD", "");
            if (store.isEmpty() || alias.isEmpty()) fail("MEMORY_GAME_STORE_PASSWORD and MEMORY_GAME_KEY_ALIAS must be set in " + args[2]);
            System.out.println("\n== Checking the MEMORY_GAME_* properties in " + args[2]);
            KeyStore ks = open(file, store.toCharArray());
            if (ks == null) fail("FAIL  store password is wrong");
            System.out.println("PASS  store password opens the keystore");
            if (!ks.containsAlias(alias)) fail("FAIL  no alias '" + alias + "' (aliases: " + Collections.list(ks.aliases()) + ")");
            System.out.println("PASS  alias '" + alias + "' exists");
            if (tryKey(ks, alias, (key.isEmpty() ? store : key).toCharArray())) System.out.println("PASS  key password unlocks the private key");
            else fail("FAIL  key password is wrong for alias '" + alias + "'");
            System.out.println("Ready to sign: run scripts/verify-release.sh");
        } else if (args.length != 1) usage();
    }

    static void listUnauthenticated(File file) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream in = new FileInputStream(file)) { ks.load(in, null); }
        System.out.println("== " + file + " (" + ks.getType() + ", integrity not checked)");
        for (String alias : Collections.list(ks.aliases())) {
            Certificate cert = ks.getCertificate(alias);
            System.out.println("alias: " + alias + "  created: " + ks.getCreationDate(alias)
                    + "  type: " + (ks.isKeyEntry(alias) ? "private key" : "certificate"));
            if (cert instanceof X509Certificate) {
                X509Certificate x = (X509Certificate) cert;
                System.out.println("  owner:   " + x.getSubjectX500Principal());
                System.out.println("  valid:   " + x.getNotBefore() + " -> " + x.getNotAfter());
                System.out.println("  SHA-256: " + fingerprint(cert, "SHA-256"));
                System.out.println("  SHA-1:   " + fingerprint(cert, "SHA-1"));
            }
        }
    }

    static void tryCandidates(File file, List<String> candidates) throws Exception {
        int storeIndex = -1;
        KeyStore ks = null;
        for (int i = 0; i < candidates.size(); i++) {
            ks = open(file, candidates.get(i).toCharArray());
            if (ks != null) { storeIndex = i; break; }
        }
        if (ks == null) { System.out.println("FAIL  none of the candidates is the store password"); return; }
        System.out.println("PASS  store password = candidate #" + (storeIndex + 1) + " (line " + (storeIndex + 1) + " of the file)");
        for (String alias : Collections.list(ks.aliases())) {
            if (!ks.isKeyEntry(alias)) continue;
            int keyIndex = -1;
            for (int i = 0; i < candidates.size(); i++) if (tryKey(ks, alias, candidates.get(i).toCharArray())) { keyIndex = i; break; }
            if (keyIndex >= 0) System.out.println("PASS  key password for '" + alias + "' = candidate #" + (keyIndex + 1));
            else System.out.println("FAIL  none of the candidates unlocks the key '" + alias + "'");
        }
    }

    static KeyStore open(File file, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream in = new FileInputStream(file)) { ks.load(in, password); return ks; }
        catch (IOException e) { return null; }
    }

    static boolean tryKey(KeyStore ks, String alias, char[] password) {
        try { return ks.getKey(alias, password) != null; } catch (GeneralSecurityException e) { return false; }
    }

    static String fingerprint(Certificate cert, String algorithm) throws Exception {
        byte[] digest = MessageDigest.getInstance(algorithm).digest(cert.getEncoded());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) sb.append(String.format("%s%02X", i == 0 ? "" : ":", digest[i]));
        return sb.toString();
    }

    static void usage() { fail("usage: KeystoreCheck <keystore> [--try <candidates-file> | --props <gradle.properties>]"); }
    static void fail(String message) { System.out.println(message); System.exit(1); }
}
