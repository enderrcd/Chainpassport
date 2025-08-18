package com.web1.service;

import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ZKPService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 初始化ZKP认证流程
     */
    public void initializeZKP(HttpSession session) throws Exception {
        // 1. 生成Ed25519密钥对
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("Ed25519");
        KeyPairGenerator keyGen = new KeyPairGenerator();
        keyGen.initialize(spec, null);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        EdDSAPrivateKey privateKey = (EdDSAPrivateKey) keyPair.getPrivate();
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keyPair.getPublic();
        
        // 2. 生成用户UUID
        String userId = UUID.randomUUID().toString();
        
        // 3. 创建JWT令牌
        String jwt = jwtUtil.generateToken(userId);
        
        // 4. 将JWT存储在session中
        session.setAttribute("loginJwt", jwt);
        
        // 5. 将私钥存储到Redis中
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        redisUtil.set("loginPrivate:" + jwt, privateKeyBase64, 30, TimeUnit.MINUTES);
        
        // 6. 将公钥存储到Redis中
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        redisUtil.set("loginPublic:" + jwt, publicKeyBase64, 30, TimeUnit.MINUTES);
        
        // 7. 向认证服务发送请求
        sendToAuthService(publicKeyBase64, jwt);
    }
    
    /**
     * 执行零知识证明认证
     */
    public String performZKPAuthentication(HttpSession session) throws Exception {
        // 1. 从session中获取JWT
        String jwt = (String) session.getAttribute("loginJwt");
        if (jwt == null) {
            throw new RuntimeException("请先去A认证");
        }
        
        // 2. 向认证服务发送请求获取签名
        String signature = requestSignatureFromAuthService(jwt);
        
        // 3. 从Redis获取公钥
        String publicKeyBase64 = (String) redisUtil.get("loginPublic:" + jwt);
        if (publicKeyBase64 == null) {
            throw new RuntimeException("公钥不存在，请重新认证");
        }
        
        // 4. 验证签名
        boolean isValid = verifySignature(jwt, signature, publicKeyBase64);
        
        if (isValid) {
            return jwt; // 返回token
        } else {
            throw new RuntimeException("认证失败，请重新认证");
        }
    }
    
    /**
     * 向认证服务发送公钥和JWT
     */
    private void sendToAuthService(String publicKey, String jwt) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/public/login/ZKP");
        
        // 设置请求参数
        String jsonParams = String.format("{\"PublicSecret\":\"%s\",\"jwt\":\"%s\"}", publicKey, jwt);
        StringEntity entity = new StringEntity(jsonParams, "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        
        CloseableHttpResponse response = httpClient.execute(httpPost);
        response.close();
        httpClient.close();
    }
    
    /**
     * 从认证服务请求签名
     */
    private String requestSignatureFromAuthService(String jwt) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/login/ZKP");
        
        // 设置认证头
        httpPost.setHeader("Authorization", "Bearer " + jwt);
        
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String responseBody = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        
        return responseBody; // 这里应该解析JSON获取签名，简化处理直接返回
    }
    
    /**
     * 验证Ed25519签名
     */
    private boolean verifySignature(String message, String signatureBase64, String publicKeyBase64) throws Exception {
        try {
            // 解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("Ed25519");
            EdDSAPublicKey publicKey = new EdDSAPublicKey(new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(publicKeyBytes, spec));
            
            // 解码签名
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            
            // 验证签名
            Signature signature = new EdDSAEngine();
            signature.initVerify(publicKey);
            signature.update(message.getBytes("UTF-8"));
            
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}