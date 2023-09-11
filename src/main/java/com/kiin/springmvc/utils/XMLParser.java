package com.kiin.springmvc.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;

public class XMLParser {

    public static String parserXml(String xmlFile){
        SAXReader reader = new SAXReader();
        try {
            InputStream inputStream = XMLParser.class.getClassLoader().getResourceAsStream(xmlFile);
                Document document = reader.read(inputStream);
            Element rootElement = document.getRootElement();
            Element element = rootElement.element("component-scan");
            Attribute attribute = element.attribute("base-package");
            return attribute.getText();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
