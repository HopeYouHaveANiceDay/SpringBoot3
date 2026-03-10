package Java.SpringBoot3.provider;

import Java.SpringBoot3.domain.UserPrincipal;
import Java.SpringBoot3.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static Java.SpringBoot3.constant.Constants.*;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/*
📖 總結
這個方法會根據使用者資訊 (UserPrincipal) 建立一個 JWT 存取權杖 (Access Token)，其中包含：
    簽發者 withIssuer(GET_ARRAYS_LLC)
    受眾 withAudience(CUSTOMER_MANAGEMENT_SERVICE)
    簽發時間 withIssuedAt(new Date())
    使用者名稱 withSubject(userPrincipal.getUsername()) 設定 Token 的主體 (Subject)，通常是使用者名稱或 ID。
    使用者權限
    到期時間
最後用 HMAC512 演算法與密鑰簽署，產生一個安全的 Token，供後續 API 認證與授權使用。
 */
@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    private final UserService userService;

    public String createAccessToken(UserPrincipal userPrincipal) {
        return JWT.create().withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(String.valueOf(userPrincipal.getUser().getId())).withArrayClaim(AUTHORITIES, getClaimsFromUser(userPrincipal))
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));

    }

    public String createRefreshToken(UserPrincipal userPrincipal) {
        return JWT.create().withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(String.valueOf(userPrincipal.getUser().getId()))
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));

    }

    public Long getSubject(String token, HttpServletRequest request) {
        try {
            return Long.valueOf(getJWTVerifier().verify(token).getSubject());
        } catch (TokenExpiredException exception) {
            request.setAttribute("expiredMessage", exception.getMessage());
            throw exception;
        } catch (InvalidClaimException exception) {
            request.setAttribute("invalidClaim", exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            throw exception;
        }
    }

    public List<GrantedAuthority>getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(toList());
    }

    public Authentication getAuthentication(Long userId, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(userService.getUserById(userId), null, authorities);
        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }

    public boolean isTokenValid(Long userId, String token) {
        JWTVerifier verifier = getJWTVerifier();
        return !Objects.isNull(userId) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            //HMAC512(secret.getBytes()) 與 HMAC512(secret) 是不同的 API 呼叫。前者需要 byte[]，後者需要 String。這樣會造成簽署和驗證時使用的演算法不一致，導致 Token 無法驗證成功。
            Algorithm algorithm = HMAC512(secret);
            //Algorithm algorithm = HMAC512(secret.getBytes()); // 統一使用 byte[]
            verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();
        }catch (JWTVerificationException exception) { throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED); }
        return verifier;
    }
}