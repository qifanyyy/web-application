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
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedURIs.add("/index.html");
        allowedURIs.add("/index.css");
        allowedURIs.add("/index.js");
        allowedURIs.add("/util.js");
        allowedURIs.add("/api/login");
        allowedURIs.add("/favicon.ico");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        httpRequest.getSession().getServletContext().log(httpRequest.getRequestURI());
        if (allowedURIs.contains(httpRequest.getRequestURI()) || httpRequest.getSession().getAttribute("customer") != null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            httpResponse.sendRedirect("/index.html");
        }
    }
}
