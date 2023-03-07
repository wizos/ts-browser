// package com.hinnka.tsbrowser.util;
//
// import java.io.IOException;
// import java.io.InputStream;
// import java.security.Key;
// import java.security.KeyStore;
// import java.security.KeyStoreException;
// import java.security.NoSuchAlgorithmException;
// import java.security.PrivateKey;
// import java.security.UnrecoverableKeyException;
// import java.security.cert.Certificate;
// import java.security.cert.CertificateException;
// import java.security.cert.X509Certificate;
// import java.util.Enumeration;
//
// public class KuaiNiuUtil {
//     public void get() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
//         PrivateKey privateKey = null;
//         X509Certificate[] certificates = null;
//         //读取证书
//         InputStream certificateFileStream = getClass().getResourceAsStream("fileName");
//         KeyStore keyStore = KeyStore.getInstance("PKCS12");
//         String password = "password";
//         //将证书写入keyStore
//         keyStore.load(certificateFileStream, password.toCharArray());
//
//         Enumeration<String> aliases = keyStore.aliases();
//         String alias = aliases.nextElement();
//
//         Key key = keyStore.getKey(alias, password.toCharArray());
//         if (key instanceof PrivateKey) {
//             privateKey = (PrivateKey) key;
//             Certificate cert = keyStore.getCertificate(alias);
//             certificates = new X509Certificate[1];
//             certificates[0] = (X509Certificate) cert;
//         }
//
//         certificateFileStream.close();
//     }
// }
