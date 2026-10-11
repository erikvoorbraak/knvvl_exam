package org.knvvl.exam.lplus;

import com.google.gson.annotations.SerializedName;

/**
 * Maps the JSON body returned by the LPlus TestStudio editor auth endpoint:
 * {@code POST /ltseditor/api/api/auth}.
 *
 * <p>Example response:
 * <pre>{@code
 * {
 *   "token_type":        "Bearer",
 *   "access_token":      "eyJhbGci...",
 *   "refresh_token":     "eyJhbGci...",
 *   "expires_in":        300,
 *   "token_expires_at":  "2026-10-09T13:45:31.9345248Z",
 *   "username":          "evoorbraak@hotmail.com",
 *   "lts_access_token":  "zEoOJHv...",
 *   "lts_refresh_token": "51c64e4b..."
 * }
 * }</pre>
 */
public class LPlusTokenResponse
{
    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("token_expires_at")
    private String tokenExpiresAt;

    @SerializedName("username")
    private String username;

    @SerializedName("lts_access_token")
    private String ltsAccessToken;

    @SerializedName("lts_refresh_token")
    private String ltsRefreshToken;

    public String getTokenType()      { return tokenType; }
    public String getAccessToken()    { return accessToken; }
    public String getRefreshToken()   { return refreshToken; }
    public int    getExpiresIn()      { return expiresIn; }
    public String getTokenExpiresAt() { return tokenExpiresAt; }
    public String getUsername()       { return username; }
    public String getLtsAccessToken() { return ltsAccessToken; }
    public String getLtsRefreshToken(){ return ltsRefreshToken; }

    /** Convenience: the full value for an {@code Authorization: Bearer ...} header. */
    public String toBearerHeader()
    {
        return "Bearer " + accessToken;
    }
}
