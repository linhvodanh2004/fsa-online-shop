package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.CartRepository;
import fsa.project.online_shop.repositories.UserRepository;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User handleSaveUser(User user) {
        if(user.getCart() == null) {
            Cart cart = Cart.builder()
                    .user(userRepository.save(user))
                    .sum(0D)
                    .build();
            cartRepository.save(cart);
            user.setCart(cart);
        }
        return userRepository.save(user);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


}
