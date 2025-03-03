package hu.perit.spvitamin.spring.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        String state = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if ("oauth2_state".equals(cookie.getName()))
                {
                    state = cookie.getValue();

                    // Removing cookie
                    Cookie deleteCookie = new Cookie("oauth2_state", "");
                    deleteCookie.setPath("/");
                    deleteCookie.setMaxAge(0);
                    response.addCookie(deleteCookie);

                    break;
                }
            }
        }

        if (state != null && !state.isEmpty())
        {
            log.info("Authentication successful, redirecting back to {}", state);
            // Redirect to the given URL
            getRedirectStrategy().sendRedirect(request, response, state);
        }
        else
        {
            // Default redirect
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
