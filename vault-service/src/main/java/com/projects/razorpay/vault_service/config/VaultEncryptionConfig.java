package com.projects.razorpay.vault_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class VaultEncryptionConfig {

    @Value("${vault.master-key}")
    private String masterKey;

    public static BytesEncryptor panEncrypter(byte[] dek) {
        SecretKeySpec dekKey = new SecretKeySpec(dek, "AES");
        return new AesBytesEncryptor(dekKey, KeyGenerators.secureRandom(12),
                                     AesBytesEncryptor.CipherAlgorithm.GCM);
    }

//    @Bean
//    public BytesEncryptor dekEncrypter() {
//        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
//        SecretKeySpec masterDecKey = new SecretKeySpec(masterKeyBytes, "AES");
//        return new AesBytesEncryptor(masterDecKey, KeyGenerators.secureRandom(12),
//                                     AesBytesEncryptor.CipherAlgorithm.GCM);
//    }

}
