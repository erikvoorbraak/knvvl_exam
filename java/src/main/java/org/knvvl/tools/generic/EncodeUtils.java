package org.knvvl.tools.generic;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic encoding utils.
 * 
 * @author gevmic0
 *
 */
public final class EncodeUtils
{
    private EncodeUtils()
    {
    }

    /**
     * Convert bytes to hex string
     * 
     * @param bytes The bytes to encode
     * @return The bytes encoded as hex string
     */
    public static String bytesToHex(byte[] bytes)
    {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte h : bytes) 
        {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Convert bytes to base64 string
     * 
     * @param bytes The bytes to encode
     * @return The bytes encoded as base64 string
     */
    public static String bytesToBase64(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static String urlEncodeParams(Map<String, String> params)
    {
        return params.entrySet().stream().map(entry -> Stream.of(
            urlEncode(entry.getKey()), urlEncode(entry.getValue()))
            .collect(Collectors.joining("="))).collect(Collectors.joining("&"));
    }

    public static String urlEncodeMultiParams(Map<String, List<String>> params)
    {
        List<String> parts = new ArrayList<>();
        params.forEach((k,v) -> v.forEach(lv -> parts.add(lv == null || lv.isBlank() ? urlEncode(k) : String.join("=", List.of(urlEncode(k), urlEncode(lv))))));
        return String.join("&", parts);
    }

    private static String urlEncode(String str)
    {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
}
