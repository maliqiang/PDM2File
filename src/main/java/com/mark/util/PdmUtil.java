/**
 * @file PdmUtil.java
 * @project PDM2File
 * @copyright 无锡雅座在线科技股份有限公司
 */
package com.mark.util;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * pdm读取工具类
 *
 * @author maliqiang
 * @version 1.0
 * @create 2017-12-20
 */
public class PdmUtil {
    /**
     * 读取pdm并输出
     * @param fileName
     * @throws DocumentException
     */
    public static void parse2Console(String fileName) throws DocumentException {
        System.out.println(ansi().fg(RED).a("PDM文件路径:") + ansi().fg(Ansi.Color.GREEN).a(fileName).toString());
        System.out.println(ansi().fg(YELLOW).a("说明：\n"));
        System.out.println("1、表标题分别为: 列名称|类型|长度|是否为主键|是否允许为空|备注");
        System.out.println("2、目前仅支持对pdm文件数据的简单解析");
        System.out.println();



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

        System.out.println(ansi().fg(RED).a("表总数量:") + ansi().fg(GREEN).a(tableElements.size()).toString());
        System.out.println(ansi().fgDefault().a(" "));

        int i = 0;
        for (Element tableElement : tableElements) {
            i++;
            Element name = tableElement.element(new QName("Name", aNamespace));
            Element code = tableElement.element(new QName("Code", aNamespace));
            //表名称注释
            String ChineseTableName = name.getText();
            //表名
            String tableName = code.getText();
            System.out.println("======== "
                    + ansi().fg(BLUE).a("NO." + i)
                    + ansi().fg(RED).a(" " + ChineseTableName + " ")
                    + ansi().fg(YELLOW).a("(" + tableName + ")")
                    + ansi().fgDefault().a(" ========"));
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
                System.out.print(getPadString(getTextFromElement(cDataType), 15));
//                System.out.print(getPadString(getTextFromElement(cLength), 7));

                if (pkColumnIds.contains(columnId)) {
                    System.out.print(" 主键\t");
                } else {
                    System.out.print("\t\t\t");
                }
                //是否可为空
                if (nullable != null) {
                    System.out.print("非空\t");
                } else {
                    System.out.print("可空\t");
                }
//                System.out.print(cname.getText());
                if (cComment != null) {
                    System.out.print("   【" + getTextFromElement(cComment).replace("\n", "  ") + "】");
                }
                System.out.println();
            }
            System.out.println();

        }

        System.out.println("=========================================================");
        System.out.println("转换共耗时:" + ansi().fg(RED).a((System.currentTimeMillis() - start) / 1000F) + ansi().fg(DEFAULT).a("s"));
        System.out.println();
    }

    static String getTextFromElement(Element element) {
        if (element == null) {
            return "";
        }
        return element.getText();
    }

    /**
     * 计算输出时的间隔字符数
     *
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
