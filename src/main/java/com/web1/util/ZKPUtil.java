package com.web1.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import net.i2p.crypto.eddsa.EdDSASecurityProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.Security;
import java.util.Base64;

import javax.annotation.PostConstruct;

/**
 * 零知识证明工具类
 * 处理Ed25519密钥生成和签名验证
 */
@Component
public class ZKPUtil {
    
    @PostConstruct
    public void init() {
        // 注册EdDSA安全提供者
        Security.addProvider(new EdDSASecurityProvider());
    }
    
    private static final String ZKP_SERVICE_URL = "http://localhost:8080/login/ZKP";
    private static final String ZKP_AUTH_SERVICE_URL = "http://localhost:8080/public/login/ZKP";
    
    /**
     * 生成Ed25519密钥对
     */
    public KeyPair generateKeyPair() {
        try {
            EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("Ed25519");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EdDSA", "EdDSA");
            keyPairGenerator.initialize(spec);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("密钥对生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 向认证服务发送公钥和JWT
     */
    public void sendToAuthService(String publicKey, String jwt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(ZKP_AUTH_SERVICE_URL);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建请求体
            String requestBody = String.format("{\"PublicSecret\":\"%s\",\"jwt\":\"%s\"}", publicKey, jwt);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new RuntimeException("认证服务调用失败，状态码：" + statusCode + "，响应：" + responseBody);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("调用认证服务出错：" + e.getMessage());
        }
    }
    
    /**
     * 调用ZKP认证服务
     */
    public String callZKPService(String jwt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(ZKP_SERVICE_URL);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + jwt);
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 解析响应获取签名数据
                    // 这里简化处理，实际应解析JSON响应
                    return responseBody;
                } else {
                    throw new RuntimeException("ZKP服务调用失败：" + responseBody);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("调用ZKP服务出错：" + e.getMessage());
        }
    }
    
    /**
     * 验证Ed25519签名
     */
    public boolean verifySignature(String message, String signatureData, String publicKeyStr) {
        try {
            // 解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("Ed25519");
            EdDSAPublicKeySpec publicKeySpec = new EdDSAPublicKeySpec(publicKeyBytes, spec);
            EdDSAPublicKey publicKey = new EdDSAPublicKey(publicKeySpec);
            
            // 解码签名
            byte[] signature = Base64.getDecoder().decode(signatureData);
            
            // 验证签名
            Signature sgr = new EdDSAEngine();
            sgr.initVerify(publicKey);
            sgr.update(message.getBytes());
            
            return sgr.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException("签名验证失败：" + e.getMessage());
        }
    }
}