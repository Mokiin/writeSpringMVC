package com.kiin.controller;

import com.kiin.pojo.Monster;
import com.kiin.service.MonsterService;
import com.kiin.springmvc.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class MonsterController {

    @AutoWired
    private MonsterService monsterService;

    @RequestMapping("/monster/list")
    public void listMonster(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");//设置服务器端的编码，默认是ISO-8859-1；该方法必须在response.getWriter()之前进行设置
        response.setHeader("content-Type", "text/html; charset=utf-8");
        List<Monster> monsters = monsterService.listMonster();
        StringBuilder content = new StringBuilder("<h1>MonsterListData</h1>");
        content.append("<table border='1px' width='500px' style='border-collapse:collapse'>");
        for (Monster monster : monsters) {
            content.append("<tr><td>" + monster.getName() + "</td><td>" + monster.getAge() + "</td></tr>");
        }
        content.append("</table>");

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/monster/find")
    public void findMonsterByName(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name) {
        response.setCharacterEncoding("utf-8");//设置服务器端的编码，默认是ISO-8859-1；该方法必须在response.getWriter()之前进行设置
        response.setHeader("content-Type", "text/html; charset=utf-8");
        Monster monster = monsterService.findMonsterByName(name);
        StringBuilder content = new StringBuilder("<h1>MonsterData</h1>");
        content.append("<table border='1px' width='500px' style='border-collapse:collapse'>");
        content.append("<tr><td>" + monster.getName() + "</td><td>" + monster.getAge() + "</td></tr>");
        content.append("</table>");

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/monster/login")
    public String isMonsterName(HttpServletRequest request, HttpServletResponse response, @RequestParam("mName") String name) {
        System.out.println("mName :: " + name);
        request.setAttribute("mName", name);
        if (monsterService.isMonsterName(name)) {
            return "forward:/login_ok.jsp";
        } else {
            return "forward:/login_error.jsp";
        }
    }

    @RequestMapping("/monster/listJson")
    @ResponseBody
    public List<Monster> listMonsterByJson(HttpServletRequest request, HttpServletResponse response) {
        return monsterService.listMonster();
    }
}
