package com.kiin.test;

import com.kiin.springmvc.servlet.KiinDispatcherServlet;
import com.kiin.springmvc.utils.XMLParser;
import org.junit.Test;

public class springMVCTEst {

    @Test
    public void test(){
        System.out.println(XMLParser.parserXml("springMVC.xml"));
    }
}
