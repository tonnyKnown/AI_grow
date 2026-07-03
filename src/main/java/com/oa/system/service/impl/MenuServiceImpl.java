package com.oa.system.service.impl;

import com.oa.system.entity.Menu;
import com.oa.system.mapper.MenuMapper;
import com.oa.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuByRoles(List<String> roleKeys) {
        List<Menu> menus = menuMapper.selectAll();

        if (roleKeys == null || roleKeys.isEmpty()) {
            List<Menu> accessibleMenus = menus.stream()
                    .filter(menu -> menu.getRoleKeys() == null || menu.getRoleKeys().isEmpty())
                    .collect(Collectors.toList());
            return buildMenuTree(accessibleMenus);
        }

        List<Menu> accessibleMenus = menus.stream()
                .filter(menu -> isMenuAccessible(menu, roleKeys))
                .collect(Collectors.toList());

        return buildMenuTree(accessibleMenus);
    }

    private boolean isMenuAccessible(Menu menu, List<String> userRoles) {
        String menuRoleKeys = menu.getRoleKeys();
        if (menuRoleKeys == null || menuRoleKeys.trim().isEmpty()) {
            return true;
        }
        String[] requiredRoles = menuRoleKeys.split(",");
        for (String required : requiredRoles) {
            if (userRoles.contains(required.trim())) {
                return true;
            }
        }
        return false;
    }

    private List<Menu> buildMenuTree(List<Menu> menus) {
        Map<Long, Menu> menuMap = new HashMap<>();
        List<Menu> rootMenus = new ArrayList<>();

        for (Menu menu : menus) {
            menuMap.put(menu.getId(), menu);
            menu.setChildren(new ArrayList<>());
        }

        for (Menu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0) {
                rootMenus.add(menu);
            } else {
                Menu parent = menuMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        rootMenus.sort(Comparator.comparingInt(Menu::getOrderNum));
        for (Menu menu : rootMenus) {
            if (menu.getChildren() != null) {
                menu.getChildren().sort(Comparator.comparingInt(Menu::getOrderNum));
            }
        }

        return rootMenus;
    }

    @Override
    public List<Menu> getAllMenus() {
        List<Menu> menus = menuMapper.selectAll();
        return buildMenuTree(menus);
    }

    @Override
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public Menu createMenu(Menu menu) {
        menu.setCreateTime(new Date());
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    public Menu updateMenu(Menu menu) {
        menu.setUpdateTime(new Date());
        menuMapper.update(menu);
        return menu;
    }

    @Override
    public void deleteMenu(Long id) {
        menuMapper.deleteById(id);
    }
}