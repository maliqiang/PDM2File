/**
 * @file PDMReader.java
 * @project PDM2File
 * @copyright 无锡雅座在线科技股份有限公司
 */
package com.mark;

import com.mark.util.PdmUtil;
import org.dom4j.DocumentException;

/**
 * PDM文件读取工具
 *
 * @author maliqiang
 * @version 1.0
 * @create 2017-12-20
 */
public class PdmReader {
    public static void main(String[] args) throws DocumentException {
//        if (args.length < 1) {
//            throw new IllegalArgumentException("第一个参数必须是pdm文件路径");
//        }
        String fileName = "F:\\doc\\数据库模型\\物理数据模型.pdm";
//        String fileName = args[0];
        PdmUtil.parse2Console(fileName);
    }
}
