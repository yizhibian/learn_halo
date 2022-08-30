package run.halo.app.security.filter;

import static run.halo.app.model.support.HaloConst.ADMIN_TOKEN_HEADER_NAME;
import static run.halo.app.model.support.HaloConst.ADMIN_TOKEN_QUERY_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.handler.DefaultAuthenticationFailureHandler;
import run.halo.app.security.service.OneTimeTokenService;
import run.halo.app.security.support.UserDetail;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;

/**
 * Admin authentication filter.
 *
 * @author johnniang
 */
@Slf4j
@Component
@Order(1)
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter {

    private final HaloProperties haloProperties;

    private final UserService userService;

    public AdminAuthenticationFilter(AbstractStringCacheStore cacheStore,
        UserService userService,
        HaloProperties haloProperties,
        OptionService optionService,
        OneTimeTokenService oneTimeTokenService,
        ObjectMapper objectMapper) {
        super(haloProperties, optionService, cacheStore, oneTimeTokenService);
        this.userService = userService;
        this.haloProperties = haloProperties;


        addUrlPatterns("/api/admin/**", "/api/content/comments");

        /*
        * 上面是拦截所有该路径下的
        * 下面是排除以下这些路径
        * */

        addExcludeUrlPatterns(
            "/api/admin/login",
            "/api/admin/refresh/*",
            "/api/admin/installations",
            "/api/admin/migrations/halo",
            "/api/admin/is_installed",
            "/api/admin/password/code",
            "/api/admin/password/reset",
            "/api/admin/login/precheck"
        );

        // set failure handler
        DefaultAuthenticationFailureHandler failureHandler =
            new DefaultAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.getMode().isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);

        setFailureHandler(failureHandler);

    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // 如果未设置认证
        /*
        * 如果博客未设置身份认证，
        * 那么将 users 表中的第一个用户作为当前用户，
        * 并存储到 ThreadLocal 容器中，
        * ThreadLocal 可用于在同一个线程内的多个函数或者组件之间传递公共信息。
        * 如果开启了身份认证，则继续向下执行。
        * */
        if (!haloProperties.isAuthEnabled()) {
            // Set security
            userService.getCurrentUser().ifPresent(user ->
                SecurityContextHolder.setContext(
                    new SecurityContextImpl(new AuthenticationImpl(new UserDetail(user)))));

            // Do filter
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 token, 从请求的 Query 参数中获取 admin_token 或者从 Header 中获取 Admin-Authorization
        // Get token from request
        String token = getTokenFromRequest(request);

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("未登录，请登录后访问");
        }

        // 根据 token 从 cacheStore 缓存中获取用户 id
        // Get user id from cache
        Optional<Integer> optionalUserId =
            cacheStore.getAny(SecurityUtils.buildTokenAccessKey(token), Integer.class);

        if (!optionalUserId.isPresent()) {
            throw new AuthenticationException("Token 已过期或不存在").setErrorData(token);
        }

        // Get the user
        User user = userService.getById(optionalUserId.get());

        // Build user detail
        UserDetail userDetail = new UserDetail(user);

        // 将用户信息存储到 ThreadLocal 中
        // Set security
        /*
        * 这个逼userDetail也只是包装一个user对象然后 get set 方法
        * 然后这个逼AuthenticationImpl只是包装了一个userDetail的对象
        * 最后这个逼SecurityContextImpl 也只是包装这个 AuthenticationImpl
        *
        * 将这个SecurityContextImpl作为ThreadLocal的value放进去 作为线程的局部变量
        * */
        SecurityContextHolder
            .setContext(new SecurityContextImpl(new AuthenticationImpl(userDetail)));

        // Do filter
        filterChain.doFilter(request, response);
    }

    @Override
    protected String getTokenFromRequest(@NonNull HttpServletRequest request) {
        return getTokenFromRequest(request, ADMIN_TOKEN_QUERY_NAME, ADMIN_TOKEN_HEADER_NAME);
    }

}
