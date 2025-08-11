package org.example.springsecurity.另一个网页的身份生成代码;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class Ed25519Example {
    static {
        // 注册Bouncy Castle安全提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    // 生成Ed25519密钥对
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        // 使用Java标准API生成Ed25519密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC");
        return keyPairGenerator.generateKeyPair();
    }

    // 手动方式生成密钥对（使用Bouncy Castle底层API）
    public static KeyPair generateKeyPairManual() {
        Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
        keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

        Ed25519PrivateKeyParameters privateKey = (Ed25519PrivateKeyParameters) keyPair.getPrivate();
        Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) keyPair.getPublic();

        try {
            // 转换为Java标准Key对象
            PrivateKey javaPrivateKey = KeyFactory.getInstance("Ed25519", "BC")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKey.getEncoded()));

            PublicKey javaPublicKey = KeyFactory.getInstance("Ed25519", "BC")
                    .generatePublic(new X509EncodedKeySpec(publicKey.getEncoded()));

            return new KeyPair(javaPublicKey, javaPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException("密钥转换失败", e);
        }
    }

    // 签名示例
    public static byte[] sign(byte[] message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("Ed25519", "BC");
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign();
    }

    // 验证签名示例
    public static boolean verify(byte[] message, byte[] signature, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance("Ed25519", "BC");
        verifier.initVerify(publicKey);
        verifier.update(message);
        return verifier.verify(signature);
    }

   /* public static void main(String[] args) throws Exception {
        // 生成密钥对
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 打印公钥和私钥（Base64编码）
        System.out.println("公钥: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        System.out.println("私钥: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

        // 测试签名和验证
        String message = "Hello, Ed25519!";
        byte[] signature = sign(message.getBytes(), privateKey);
        boolean isValid = verify(message.getBytes(), signature, publicKey);

        System.out.println("签名验证结果: " + isValid);
    }*/
}
