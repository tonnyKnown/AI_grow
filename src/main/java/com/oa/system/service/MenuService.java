package com.oa.system.service;

import com.oa.system.entity.Menu;

import java.util.List;

public interface MenuService {

    List<Menu> getMenuByRoles(List<String> roleKeys);

    List<Menu> getAllMenus();

    Menu getMenuById(Long id);

    Menu createMenu(Menu menu);

    Menu updateMenu(Menu menu);

    void deleteMenu(Long id);
}