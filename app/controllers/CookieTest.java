package controllers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tool-taro.com
 */
public class CookieTest {

    public static String getCookie(HttpServletRequest request, String name) {
        String result = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    result = cookie.getValue();
                    break;
                }
            }
        }

        return result;
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String path, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        //httpsで稼働している環境であればCookieが暗号化されるようSecure属性をつける
        if ("https".equals(request.getScheme())) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
    }
}