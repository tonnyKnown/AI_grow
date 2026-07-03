package com.oa.system.service;

import com.oa.system.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> getAllMenus();
    Menu getMenuById(Long id);
    List<Menu> getMenuTree();
    List<Menu> getMenusByRoles(String roles);
    void createMenu(Menu menu, Long createBy);
    void updateMenu(Menu menu, Long updateBy);
    void deleteMenu(Long id);
}
