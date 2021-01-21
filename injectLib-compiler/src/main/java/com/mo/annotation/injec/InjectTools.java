package com.mo.annotation.injec;

import javax.lang.model.type.TypeMirror;

public class InjectTools {
    /**
     *         getIntent().getStringExtra("myParam");
     *         getIntent().getIntExtra();
     *         getIntent().getLongExtra();
     *         getIntent().getFloatExtra();
     *         getIntent().getBooleanExtra();
     *         getIntent().getByteExtra();
     *         getIntent().getParcelableExtra()
     * @return 返回变量的类型。
     */
    public static String getTypeName(TypeMirror varType) {
        String typeName;
        System.out.println("varType: " + varType.getKind());
        switch (varType.getKind()) {
            case INT:
                typeName = "Int";
                break;
            case LONG:
                typeName = "Long";
                break;
            case FLOAT:
                typeName = "Float";
                break;
            case BOOLEAN:
                typeName = "Boolean";
                break;
            case BYTE:
                typeName = "Byte";
                break;
            default:
                typeName = "Other";
        }
        return typeName;
    }

    public static String upperFirstLatter(String letter){
        char[] chars = letter.toCharArray();
        if(chars[0]>='a' && chars[0]<='z'){
            chars[0] = (char) (chars[0]-32);
        }
        return new String(chars);
    }
}
