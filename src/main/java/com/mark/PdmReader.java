/**
 * @file PDMReader.java
 * @project PDM2File
 * @copyright 无锡雅座在线科技股份有限公司
 */
package com.mark;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.*;

/**
 * PDM文件读取工具
 *
 * @author maliqiang
 * @version 1.0
 * @create 2017-12-20
 */
public class PdmReader {
    public static void main(String[] args) throws DocumentException {
//        AnsiConsole.systemInstall();
//        if (args.length < 1) {
//            throw new IllegalArgumentException("第一个参数必须是pdm文件路径");
//        }
        String fileName = "F:\\doc\\数据库模型\\物理数据模型.pdm";
//        String fileName = args[0];
        System.out.println(Ansi.ansi().fg(RED).a("PDM文件路径:") + Ansi.ansi().fg(Ansi.Color.GREEN).a(fileName).toString());

        long start = System.currentTimeMillis();

        SAXReader fileReader = new SAXReader();
        /**
         * 加载pdm文件
         */
        Document document = fileReader.read(new File(fileName));
        Element rootElement = document.getRootElement();


        Namespace oNamespace = new Namespace("o", "object");
        Namespace cNamespace = new Namespace("c", "collection");
        Namespace aNamespace = new Namespace("a", "attribute");

        Element rootObject = rootElement.element(new QName("RootObject", oNamespace));

        Element children = rootObject.element(new QName("Children", cNamespace));
        Element model = children.element(new QName("Model", oNamespace));

        List<Element> tableElements = new ArrayList<>();

        //解析package
        Element packagesEle = model.element(new QName("Packages", cNamespace));
        if (packagesEle != null) {
            List<Element> packageEles = packagesEle.elements(new QName("Package", oNamespace));
            for (Element packageEle : packageEles) {
                Element tablesEle = packageEle.element(new QName("Tables", cNamespace));
                if (tablesEle != null) {
                    tableElements.addAll(tablesEle.elements(new QName("Table", oNamespace)));
                }
            }
        }


        //直接解析table
        Element tablesEle = model.element(new QName("Tables", cNamespace));
        if (tablesEle != null) {
            tableElements.addAll(tablesEle.elements(new QName("Table", oNamespace)));
        }

        System.out.println(Ansi.ansi().fg(RED).a("表总数量:") + Ansi.ansi().fg(GREEN).a(tableElements.size()).toString());
        System.out.println(Ansi.ansi().fgDefault().a(" "));

        int i = 0;
        for (Element tableElement : tableElements) {
            i++;
            Element name = tableElement.element(new QName("Name", aNamespace));
            Element code = tableElement.element(new QName("Code", aNamespace));
            //表名称注释
            String ChineseTableName = name.getText();
            //表名
            String tableName = code.getText();
            System.out.println("******"
                    + Ansi.ansi().fg(BLUE).a("NO." + i)
                    + Ansi.ansi().fg(RED).a(" " + ChineseTableName + " ")
                    + Ansi.ansi().fg(YELLOW).a("(" + tableName+ ")")
                    + Ansi.ansi().fgDefault().a("******"));
            //解析主键
            Element primaryKeyEle = tableElement.element(new QName("PrimaryKey", cNamespace));
            List<String> pkIds = new ArrayList<>();
            if (primaryKeyEle != null) {
                List<Element> pks = primaryKeyEle.elements(new QName("Key", oNamespace));
                for (Element pk1 : pks) {
                    pkIds.add(pk1.attribute("Ref").getValue());
                }
            }

            Element keysEle = tableElement.element(new QName("Keys", cNamespace));
            List<String> pkColumnIds = new ArrayList<>();
            if (keysEle != null) {
                List<Element> keyEleList = keysEle.elements(new QName("Key", oNamespace));
                for (Element keyEle : keyEleList) {
                    Attribute id = keyEle.attribute("Id");
                    if (pkIds.contains(id.getValue())) {
                        List<Element> list = keyEle.element(new QName("Key.Columns", cNamespace)).elements(new QName("Column", oNamespace));
                        for (Element element : list) {
                            pkColumnIds.add(element.attribute("Ref").getValue());
                        }
                    }
                }
            }

            //解析column
            List<Element> columns = tableElement.element(new QName("Columns", cNamespace)).elements(new QName("Column", oNamespace));
            for (Element columnEle : columns) {
                String columnId = columnEle.attribute("Id").getValue();
                Element cname = columnEle.element(new QName("Name", aNamespace));
                Element ccode = columnEle.element(new QName("Code", aNamespace));
                Element cDataType = columnEle.element(new QName("DataType", aNamespace));
                Element cLength = columnEle.element(new QName("Length", aNamespace));
                Element cComment = columnEle.element(new QName("Comment", aNamespace));
                Element nullable = columnEle.element(new QName("Column.Mandatory", aNamespace));

                System.out.print(getPadString(ccode.getText(), 20));
                System.out.print(getPadString(getTextFromEle(cDataType), 15));
                System.out.print(getPadString(getTextFromEle(cLength), 7));

                if (pkColumnIds.contains(columnId)) {
                    System.out.print("√  ");
                } else {
                    System.out.print("   ");
                }
                //是否可为空
                if (nullable != null) {
                    System.out.print("非空");
                } else {
                    System.out.print("可空");
                }

                System.out.print(cname.getText());
                if (cComment != null) {
                    System.out.print("   (" + getTextFromEle(cComment).replace("\n", "  ") + ")");
                }
                System.out.println();
            }
            System.out.println();

        }

        System.out.println("=========================================================");
        System.out.println("转换共耗时:" + Ansi.ansi().fg(RED).a((System.currentTimeMillis() - start) / 1000F) + Ansi.ansi().fg(DEFAULT).a("s"));
        System.out.println();
        System.out.print(Ansi.ansi().fg(YELLOW).a("说明：\n "));
        System.out.print(Ansi.ansi().fg(DEFAULT).a(""));
        System.out.println("1、表标题分别为 列代码/类型/长度/是否为主键/是否允许为空/列可读名称及备注");
        System.out.println("2、√ 表示主键");
        System.out.println();
    }

    static String getTextFromEle(Element element) {
        if (element == null) {
            return "";
        }
        return element.getText();
    }

    /**
     * 计算输出时的间隔字符数
     * @param str
     * @param length
     * @return
     */
    static String getPadString(String str, int length) {
        int size = str.length();
        if (size < length) {
            str += getBlank(length - size);
            return str;
        } else {
            return str + "  ";
        }
    }


    static String getBlank(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append(" ");
        }
        return s.toString();
    }
}
