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
        allowedURIs.add("index.html");
        allowedURIs.add("index.css");
        allowedURIs.add("index.js");
        allowedURIs.add("util.js");
        allowedURIs.add("/api/login");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        Customer customer;
        if ((customer = (Customer) ((HttpServletRequest) servletRequest).getSession().getAttribute("customer")) == null) {
            servletRequest.getServletContext().log("LoginFilter: not login");
        } else {
            servletRequest.getServletContext().log("LoginFilter: Customer(id=" + customer.id + ",firstName=" + customer.firstName + ")");
        }

        filterChain.doFilter(servletRequest, servletResponse);

//        if (allowedURIs.contains(httpRequest.getRequestURI())) {
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
//
//        if (httpRequest.getSession().getAttribute("customer") == null) {
//            httpResponse.sendRedirect("index.html");
//        } else {
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
    }
}
