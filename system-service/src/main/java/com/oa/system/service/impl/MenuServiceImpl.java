package com.oa.system.service.impl;

import com.oa.system.entity.Menu;
import com.oa.system.mapper.MenuMapper;
import com.oa.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getAllMenus() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildTree(allMenus);
    }

    @Override
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildTree(allMenus);
    }

    @Override
    public List<Menu> getMenusByRoles(String roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> roleList = Arrays.asList(roles.split(","));
        List<Menu> result = new ArrayList<>();
        for (String role : roleList) {
            List<Menu> menus = menuMapper.selectByRoleKey(role.trim());
            result.addAll(menus);
        }
        return buildTree(result);
    }

    @Override
    public void createMenu(Menu menu, Long createBy) {
        menu.setCreateBy(createBy);
        menu.setCreateTime(new java.util.Date());
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(Menu menu, Long updateBy) {
        menu.setUpdateBy(updateBy);
        menu.setUpdateTime(new java.util.Date());
        menuMapper.update(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        menuMapper.deleteById(id);
    }

    private List<Menu> buildTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Menu> menuMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            menu.setChildren(new ArrayList<>());
            menuMap.put(menu.getId(), menu);
        }
        List<Menu> roots = new ArrayList<>();
        for (Menu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(menu);
            } else {
                Menu parent = menuMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(menu);
                } else {
                    roots.add(menu);
                }
            }
        }
        return roots;
    }
}
