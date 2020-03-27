package org.swdc.fx.anno;

/**
 * Created by lenovo on 2019/6/8.
 */
public enum PropType {

    /**
     * 文件夹选择
     */
    FOLDER_SELECT,

    /**
     * 文件夹选择，允许导入
     */
    FOLDER_SELECT_IMPORTABLE,

    /**
     * 文件选择
     */
    FILE_SELECT,

    /**
     * 文件选择且允许导入
     */
    FILE_SELECT_IMPORTABLE,

    /**
     * 文本内容
     */
    TEXT,

    /**
     * 数值内容
     */
    NUMBER,

    /**
     * 数值内容，可以通过
     * 拖动进行调整
     */
    NUMBER_SELECTABLE,

    /**
     * 颜色选择
     */
    COLOR,

    /**
     * 复选框
     */
    CHECK

}
