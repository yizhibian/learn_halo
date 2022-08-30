package run.halo.app.security.context;

import org.springframework.lang.Nullable;
import run.halo.app.security.authentication.Authentication;

/**
 * Security context interface.
 *
 * @author johnniang
 */
public interface SecurityContext {

    /**
     * 获取当前经过身份验证的主体。
     * Gets the currently authenticated principal.
     *
     * @return the Authentication or null if authentication information is unavailable
     */
    @Nullable
    Authentication getAuthentication();

    /**
     * 更改当前经过身份验证的主体，或删除身份验证信息。
     * Changes the currently authenticated principal, or removes the authentication information.
     *
     * @param authentication the new authentication or null if no further authentication should
     * not be stored
     */
    void setAuthentication(@Nullable Authentication authentication);

    /**
     * 检查当前上下文是否已通过身份验证。
     * Check if the current context has authenticated or not.
     *
     * @return true if authenticate; false otherwise
     */
    default boolean isAuthenticated() {
        return getAuthentication() != null;
    }
}
