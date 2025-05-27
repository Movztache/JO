package com.example.jeuxolympiques.service.impl;

import com.example.jeuxolympiques.model.Role;
import com.example.jeuxolympiques.repository.RoleRepository;
import com.example.jeuxolympiques.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional
    public Role createRole(Role role) {
        // Vérifier si un rôle avec le même nom existe déjà
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Un rôle avec le nom '" + role.getName() + "' existe déjà");
        }

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Long roleId, Role role) {
        // Vérifier si le rôle existe
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé avec l'ID : " + roleId));

        // Si le nom n'a pas changé, pas besoin de vérifier les doublons
        if (!existingRole.getName().equals(role.getName())) {
            // Vérifier si le nouveau nom est déjà utilisé par un autre rôle
            Role roleWithSameName = roleRepository.findByName(role.getName());
            if (roleWithSameName != null && !roleWithSameName.getRoleId().equals(roleId)) {
                throw new IllegalArgumentException("Un rôle avec le nom '" + role.getName() + "' existe déjà");
            }
        }

        // Mettre à jour les propriétés du rôle
        existingRole.setName(role.getName());

        return roleRepository.save(existingRole);
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        // Vérifier si le rôle existe
        if (!roleRepository.existsById(roleId)) {
            return false;
        }

        // Supprimer le rôle
        roleRepository.deleteById(roleId);
        return true;
    }
}
