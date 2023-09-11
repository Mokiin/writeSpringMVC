package com.kiin.service.impl;

import com.kiin.pojo.Monster;
import com.kiin.service.MonsterService;
import com.kiin.springmvc.annotation.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonsterServiceImpl implements MonsterService {

    private static ArrayList<Monster> monsters;

    static {
        monsters = new ArrayList<>();
        monsters.add(new Monster("jack",22));
        monsters.add(new Monster("tom",1));
    }
    @Override
    public List<Monster> listMonster() {
        return monsters;
    }

    @Override
    public Monster findMonsterByName(String name) {
        for (Monster monster : monsters) {
            if (monster.getName().contains(name)){
                return monster;
            }
        }
        return null;
    }

    @Override
    public boolean isMonsterName(String name) {
        return "monster".equals(name);
    }
}
