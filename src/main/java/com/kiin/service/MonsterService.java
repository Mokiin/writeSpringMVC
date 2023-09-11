package com.kiin.service;

import com.kiin.pojo.Monster;

import java.util.List;

public interface MonsterService {

    List<Monster> listMonster();

    Monster findMonsterByName(String name);
    boolean isMonsterName(String name);
}
