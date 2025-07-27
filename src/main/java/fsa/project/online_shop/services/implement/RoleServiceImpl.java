package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Role;
import fsa.project.online_shop.repositories.RoleRepository;
import fsa.project.online_shop.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name).orElse(null);
    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }
}
