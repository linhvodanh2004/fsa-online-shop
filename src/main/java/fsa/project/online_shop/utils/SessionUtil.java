package fsa.project.online_shop.utils;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final UserService userService;
    private final HttpServletRequest request;

    public User getUserFromSession() {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Long userId = (Long) session.getAttribute("userId");
        return (userId != null) ? userService.getUserById(userId) : null;
    }

//    public Map<Long, Integer> getCartFromSession() {
//        HttpSession session = request.getSession(false);
//        Map<Long, Integer> cartMap = (Map<Long, Integer>) session.getAttribute("cart");
//        if(cartMap == null){
//            cartMap = new HashMap<>();
//            session.setAttribute("cart", cartMap);
//        }
//        return cartMap;
//    }
//
//    public void setCartToSession(Map<Long, Integer> cartMap) {
//        HttpSession session = request.getSession(true);
//        session.setAttribute("cart", cartMap);
//    }

    /**
     * Update session user information after profile update
     */
    public void updateSessionUserInfo(User user) {
        HttpSession session = request.getSession(false);
        if (session != null && user != null) {
            session.setAttribute("fullname", user.getFullname());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());
        }
    }
}
