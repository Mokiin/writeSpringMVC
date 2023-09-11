package com.kiin.controller;

import com.kiin.springmvc.annotation.Controller;
import com.kiin.springmvc.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class TestMonster {

    @RequestMapping("/test/monster")
    public void testMonster(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write("<h1>妖怪列表</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            writer.close();
        }

    }
}
