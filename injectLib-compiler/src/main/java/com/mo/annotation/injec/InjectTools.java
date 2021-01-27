package com.mo.annotation.injec;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class InjectTools {

    /**
     * 检测是否是继承了 AppCompatActivity 或者 FragmentActivity 或者 Activity，
     * 不是特别严格的检测
     *
     * @param typeUtils    类型工具
     * @param classElement 要检测的元素
     * @return true or false  表示是否是继承了 Activity
     */
    public static boolean checkActivityScope(@NonNull Types typeUtils, @NonNull TypeElement classElement) {
        TypeElement superElement = (TypeElement) typeUtils.asElement(classElement.getSuperclass());
        if (superElement == null) {
            return false;
        }

        String fullClassname = superElement.getQualifiedName().toString();
        System.out.println("classname： " + fullClassname);
        if (fullClassname.contains("Activity")) {
            return inheritActivity(fullClassname);
        }
        while (superElement != null) {
            TypeMirror mirror = superElement.getSuperclass();
            TypeElement element = (TypeElement) typeUtils.asElement(mirror);

            if (element == null) {
                System.out.println("end(class == null)");
                break;
            }

            String name = element.getQualifiedName().toString();
            System.out.println("classname： " + name);
            if (name.contains("Activity")) {
                return inheritActivity(name);
            } else {
                superElement = element;
            }
        }
        return false;
    }

    private static boolean inheritActivity(String className) {
        return "androidx.appcompat.app.AppCompatActivity".equals(className) ||
                "androidx.fragment.app.FragmentActivity".equals(className) ||
                "android.app.Activity".equals(className);
    }

    /**
     * 首字母大写工具方法
     */
    public static String upperFirstLatter(String letter) {
        char[] chars = letter.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }
}
