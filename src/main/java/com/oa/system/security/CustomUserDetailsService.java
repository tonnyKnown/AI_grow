package com.oa.system.security;

import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;
import com.oa.system.entity.User;
import com.oa.system.entity.UserRole;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        List<UserRole> userRoles = userRoleMapper.selectByUserId(user.getId());
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Role> roles = roleMapper.selectByIds(roleIds);
            
            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleKey()));
            }

            for (Long roleId : roleIds) {
                List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
                for (Permission permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission.getPermissionKey()));
                }
            }
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 返回自定义的 LoginUser 对象，包含完整的用户信息
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setStatus(user.getStatus());
        loginUser.setAuthorities(authorities);
        
        return loginUser;
    }
}
