package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import com.intellij.psi.CommonClassNames;
import java.lang.reflect.Modifier;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
/**
 *
 * @author Александр
 */
public class NetBeansJavaElementUtil {

    @NotNull
    static Visibility getVisibility(@NotNull Element member){
        int flags = member.getKind().getDeclaringClass().getModifiers();
        
        if (Modifier.isPublic(flags)){
            return Visibilities.PUBLIC;
        } else if (Modifier.isPrivate(flags)){
            return Visibilities.PRIVATE;
        } else if (Modifier.isProtected(flags)){
            return Modifier.isStatic(flags) ? JavaVisibilities.PROTECTED_STATIC_VISIBILITY :
                    JavaVisibilities.PROTECTED_AND_PACKAGE;
        }
        
        return JavaVisibilities.PACKAGE_VISIBILITY;
    }
    
    @Nullable
    public static ClassId computeClassId(Class classBinding){
        Class container = classBinding.getDeclaringClass();
        if (container != null){
            ClassId parentClassId = computeClassId(container);
            return parentClassId == null ? null : parentClassId.createNestedClassId(Name.identifier(classBinding.getName()));
        }
        
        String fqName = classBinding.getCanonicalName(); //Not sure
        return fqName == null ? null : ClassId.topLevel(new FqName(fqName));
    }
    
    public static JavaAnnotation findAnnotation(@NotNull Element bindings, @NotNull FqName fqName){
        for (AnnotationMirror annotation : bindings.getAnnotationMirrors()){//.getKind().getDeclaringClass().getAnnotations()){
            String annotationFQName = annotation.getClass().getCanonicalName();//.annotationType().getCanonicalName(); //not sure
            if (fqName.asString().equals(annotationFQName)){
                return new NetBeansJavaAnnotation(annotation.getAnnotationType().asElement());
            }
        }
        
        return null;
    }
    
    private static List<Element> getSuperTypes(@NotNull Element typeBinding){
        List<Element> superTypes = Lists.newArrayList();
        for (Element superInterface : typeBinding.getEnclosedElements()){
            if (superInterface.getKind().isInterface()){
                superTypes.add(superInterface);
            }
        }
        Element packageElement = typeBinding.getEnclosingElement();
        for (Element elem : packageElement.getEnclosedElements()){
            if (elem.getKind().equals(ElementKind.CLASS)){
                superTypes.add(elem); //searching for a superclass
            }
        }
        return superTypes;
    }
    
    public static Element[] getSuperTypesWithObject(@NotNull Element typeBinding){
        List<Element> allSuperTypes = Lists.newArrayList();
        
        boolean javaLangObjectInSuperTypes = false;
        for (Element superType : getSuperTypes(typeBinding)){
            javaLangObjectInSuperTypes = superType.getKind().getDeclaringClass().
                    getCanonicalName().equals(CommonClassNames.JAVA_LANG_OBJECT);
            allSuperTypes.add(superType);
        }
        
        if (!javaLangObjectInSuperTypes && !typeBinding.getKind().getDeclaringClass().getCanonicalName().
                equals(CommonClassNames.JAVA_LANG_OBJECT)){
        //    allSuperTypes.add(getJavaLangObjectBinding(OpenProjects.getDefault().getOpenProjects()[0]));
        }
        
        return allSuperTypes.toArray(new Element[allSuperTypes.size()]);
    }
    
    
//    @NotNull
//    private static Element getJavaLangObjectBinding(@NotNull Project project){
//        
//    }
    
}
