import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final Set<String> allowedURIs = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) {
        allowedURIs.add("favicon.ico");
        allowedURIs.add("login.html");
        allowedURIs.add("/css/login.css");
        allowedURIs.add("/js/login.js");
        allowedURIs.add("/js/util.js");
        allowedURIs.add("/api/login");
        allowedURIs.add("employee_login.html");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String requestURI;

        if ((requestURI = httpRequest.getRequestURI()).endsWith("api/logout")) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else if ((requestURI.endsWith("dashboard.html") ||
                requestURI.endsWith("dashboard.js") ||
                requestURI.endsWith("dashboard.css") ||
                requestURI.endsWith("api/dashboard")
        )) {
            if (httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect("/employee_login.html");
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else if (httpRequest.getSession().getAttribute("customer") == null) {
            httpResponse.sendRedirect("/login.html");
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }
}
