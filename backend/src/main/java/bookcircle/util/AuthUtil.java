package bookcircle.util;

import bookcircle.security.JwtPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtil {
    private AuthUtil() {}

    public static JwtPrincipal principal() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof JwtPrincipal p)) {
            throw new IllegalStateException("No authenticated principal");
        }
        return p;
    }
}
