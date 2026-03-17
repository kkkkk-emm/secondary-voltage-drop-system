package com.straykun.svd.svdsys.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 密码编码器。
 */
public class Sha256PasswordEncoder implements PasswordEncoder {

    /**
     * 执行 encode 业务逻辑。
     *
     * @param rawPassword 参数 rawPassword。
     * @return 返回字符串结果。
     */
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }

    /**
     * 执行 matches 业务逻辑。
     *
     * @param rawPassword 参数 rawPassword。
     * @param encodedPassword 参数 encodedPassword。
     * @return 返回校验结果。
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        String rawEncoded = encode(rawPassword);
        return encodedPassword.equalsIgnoreCase(rawEncoded);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
