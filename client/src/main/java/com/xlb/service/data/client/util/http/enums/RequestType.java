package com.xlb.service.data.client.util.http.enums;

/**
 * 请求提交形式
 * <ul>
 * <li>NORMAL - 默认序列化方式，以URL格式序列化，所有参数值将转为字符串形式，转换方法为调用toString()方法</li>
 * <li>MULTIPART - 以Multipart表单格式序列化请求，其中File,Inputstream类型参数将按照流内容提交，其他类型参数将值转为字符串类型，调用对象的toString方法转换</li>
 * <li>Stream - 请求以流的形式提交</li>
 * <li>STRING - 以字符串的形式提交</li>
 * </ul>
 *
 * @author 赵嘉楠
 * @version 1.0
 * @since 1.0
 */
public enum RequestType {
    NORMAL, MULTIPART, STREAM, STRING;
}
