package com.mo.annotation.injec;

import com.google.auto.service.AutoService;
import com.mo.annotation.annos.ParamInject;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

// 这个注解的作用是用来生成 META-INF/javax.annotation.processing.Processor 这个文件，文件里就是
// 注解处理器的全路径，这个文件会被 ServiceLoader 类使用，用于加载注解服务。
@AutoService(Processor.class)
// 指定注解处理器支持的 JDK 编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
// 指定注解处理器支持处理的注解
// @SupportedAnnotationTypes({ProcessorConstants.BINDVIEW_FULLNAME})
// 模块名 , 存放生成的 APT 文件的包名
// @SupportedOptions({ProcessorConstants.MODULE_NAME, ProcessorConstants.PACKAGENAME_FOR_APT})
public class ExtraInjectProcessor extends AbstractProcessor {

    /**
     * 打印日志类
     */
    private Messager mMessager;

    /**
     * 操作 Element 的工具类（类，函数，属性，枚举，构造方法都是 Element）
     */
    private Elements mElementUtils;

    /**
     * 用来对类型进行操作的实用工具方法
     */
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(ParamInject.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 拿到所有是使用 ParamInject 注解的字段
        Set<? extends Element> sets = roundEnvironment.getElementsAnnotatedWith(ParamInject.class);
        if (sets.isEmpty()) {
            // 只处理 ParamInject 注解
            return false;
        }

        Map<TypeElement, ArrayList<Element>> groups = groupElement(sets);
        for (TypeElement originTypeElement : groups.keySet()) {
            // 创建class
            TypeSpec.Builder classBuilder = makeTypeSpecBuilder(originTypeElement);
            // 创建构造方法
            MethodSpec.Builder constructorBuilder = makeConstructor(originTypeElement);
            // 生成注入代码
            buildConstructorCode(constructorBuilder, groups.get(originTypeElement));
            // 给类添加构造方法
            classBuilder.addMethod(constructorBuilder.build());
            // 添加关联的类
            classBuilder.addOriginatingElement(originTypeElement);

            // 输出 Java 文件
            TypeSpec classSpec = classBuilder.build();
            JavaFile file = JavaFile.builder(makePackageName(originTypeElement), classSpec).build();
            try {
                Filer filer = this.processingEnv.getFiler();
                file.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                mMessager.printMessage(Diagnostic.Kind.NOTE, "InjectProcessor --> 文件生成失败");
                return false;
            }
        }
        return true;
    }

    /**
     * 生成如下代码
     * class XX_Inject{
     *
     * }
     */
    private TypeSpec.Builder makeTypeSpecBuilder(TypeElement typeElement) {
        String className = typeElement.getSimpleName() + "_Inject";
        return TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
    }

    /**
     * 创建构造函数
     * public XX_Inject(XX target){
     *
     * }
     */
    private MethodSpec.Builder makeConstructor(TypeElement typeElement) {
        TypeMirror typeMirror = typeElement.asType();
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeMirror), "target");
    }

    /**
     * 生成包名
     */
    private String makePackageName(Element typeElement) {
        Element ele = typeElement;
        while (ele.getKind() != ElementKind.PACKAGE) {
            ele = ele.getEnclosingElement();
        }
        return ((PackageElement) ele).getQualifiedName().toString();
    }

    private void buildConstructorCode(MethodSpec.Builder bindMethodBuilder, ArrayList<Element> elements) {
        /*
            生成的语句模板,支持三种类型，1.基本类型 2.String 类型 3.Parcelable 类型
            target.{fieldName} = getIntent().get{typeName}Extra();
         */
        for (Element item : elements) {
            // 生成语句
            StringBuilder statementBuilder = new StringBuilder();
            String paramKey = item.getAnnotation(ParamInject.class).value();
            // 获取属性名
            String fieldName = item.toString();
            if (paramKey == null || "".equals(paramKey)) {
                paramKey = fieldName;
                // 使用默认值
            }
            statementBuilder.append("target.").append(fieldName).append(" = ").append(" target.getIntent().get");
            //属性类型
            TypeMirror fieldTypeMirror = item.asType();
            TypeName typeName = ClassName.get(fieldTypeMirror);
            if (typeName.isPrimitive()) {
                // 基本类型
                String name = typeName.toString();
                String upName = InjectTools.upperFirstLatter(name);
                System.out.println("Primitive typeName : " + name + " UpName:" + upName);
                statementBuilder.append(upName).append("Extra(\"").append(paramKey).append("\",");
                // 插入默认值
                switch (fieldTypeMirror.getKind()) {
                    case INT:
                        statementBuilder.append(0);
                        break;
                    case LONG:
                        statementBuilder.append(0L);
                        break;
                    case FLOAT:
                        statementBuilder.append(0F);
                        break;
                    case BOOLEAN:
                        statementBuilder.append(false);
                        break;
                    case BYTE:
                        statementBuilder.append((byte) 0);
                        break;
                    default:
                        break;
                }
                statementBuilder.append(")");
            } else {
                System.out.println("typeName : " + typeName.toString());
                if ("java.lang.String".equals(typeName.toString())) {
                    statementBuilder.append("StringExtra(\"").append(paramKey).append("\")");
                } else {
                    statementBuilder.append("ParcelableExtra(\"").append(paramKey).append("\")");
                }
            }

            bindMethodBuilder.addStatement(statementBuilder.toString());
        }
    }

    /**
     * 根据注解所在的类来分组
     */
    private Map<TypeElement, ArrayList<Element>> groupElement(Set<? extends Element> elements) {
        HashMap<TypeElement, ArrayList<Element>> groups = new HashMap<>();
        StringBuilder logString = new StringBuilder();
        for (Element element : elements) {
            checkAnnotationLegal(element);
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement(); // 拿到父元素
//            mMessager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName() + " 的父元素 typeElement " + enclosingElement.getQualifiedName());
            if (groups.containsKey(enclosingElement)) {
                groups.get(enclosingElement).add(element);
            } else {
                ArrayList<Element> list = new ArrayList<>();
                list.add(element);
                groups.put(enclosingElement, list);
                logString.append(" { ").append(enclosingElement.getQualifiedName()).append(" } ");
            }
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, "group: [ " + logString.toString() + " ]");

        return groups;
    }

    /**
     * 检查注解的合法性
     *
     * @param element 检查的元素
     */
    private void checkAnnotationLegal(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new RuntimeException("@ParamInject only use filed!");
        }
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.FINAL)) {
            throw new RuntimeException("@ParamInject filed can not be final!");
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            throw new RuntimeException("@ParamInject filed can not be private!");
        }
    }
}