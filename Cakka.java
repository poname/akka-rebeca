package org.rebecalang.compiler.cakka;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.rebecalang.compiler.modelcompiler.RebecaCompiler;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.utils.CodeCompilationException;
import org.rebecalang.compiler.utils.CompilerFeature;
import org.rebecalang.compiler.utils.ExceptionContainer;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.rebecalang.compiler.cakka.Utils.PACKAGE_NAME;

public class Cakka {
    private static Cakka instance;
    public static Cakka getInstance() {
        if (instance == null)
            instance = new Cakka();
        return instance;
    }

    private File rebecaFile;

    public void transform(String filePath) throws ExceptionContainer, CodeCompilationException, UnsupportedStatementException, UnsupportedExpressionException {
        rebecaFile = new File(filePath);

        Set<CompilerFeature> features = new HashSet<>();
        features.add(CompilerFeature.CORE_2_1);

        RebecaCompiler compiler = new RebecaCompiler();
        compiler.compileRebecaFile(rebecaFile, features);

        if (compiler.getExceptionContainer().getExceptions().size() != 0) {
            throw compiler.getExceptionContainer();
        }

        RebecaModel model = compiler.syntaxCheckRebecaFile(rebecaFile, features);

        if (model.getRebecaCode().getReactiveClassDeclaration() != null) {
            for (ReactiveClassDeclaration reactiveClass : model.getRebecaCode().getReactiveClassDeclaration()) {
                new ReactiveClass(reactiveClass, rebecaFile).transform();
            }
        }

        main(model.getRebecaCode().getMainDeclaration());
        messages(model.getRebecaCode().getReactiveClassDeclaration());
        injector(model.getRebecaCode().getReactiveClassDeclaration());
    }

    private void main(MainDeclaration mainDeclaration) throws UnsupportedExpressionException {

//        FieldSpec system = FieldSpec.builder(akka.actor.ActorSystem.class, "system")
//                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
//                .initializer("ActorSystem.create(\"Rebeca\")")
//                .build();

        MethodSpec.Builder mainBuilder = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("final $T system = ActorSystem.create($S)", akka.actor.ActorSystem.class, "Rebeca");

        for (MainRebecDefinition mainRebec : mainDeclaration.getMainRebecDefinition()) {
            mainBuilder.addStatement("final $T $N = system.actorOf($N.props($S), $S)", akka.actor.ActorRef.class, mainRebec.getName(), ((OrdinaryPrimitiveType)mainRebec.getType()).getName(), mainRebec.getName(), mainRebec.getName());
        }

        for (MainRebecDefinition mainRebec : mainDeclaration.getMainRebecDefinition()) {
            StringBuilder bindings = new StringBuilder("");
            ExpressionTransformer expressionTransformer = new ExpressionTransformer(new HashMap<String, String>());
            for (int i=0 ; i<mainRebec.getBindings().size(); i++) {
                Expression expression = mainRebec.getBindings().get(i);
                bindings.append(expressionTransformer.translate(expression));
                if (i < mainRebec.getBindings().size() - 1)
                    bindings.append(", ");
            }
            mainBuilder.addStatement("Injector.inject($N, new $NKnownActors($N))", mainRebec.getName(), ((OrdinaryPrimitiveType)mainRebec.getType()).getName(), bindings.toString());
        }

        for (MainRebecDefinition mainRebec : mainDeclaration.getMainRebecDefinition()) {
            mainBuilder.addStatement("$N.tell(new Messages.$N.initial(), $T.noSender())", mainRebec.getName(), ((OrdinaryPrimitiveType)mainRebec.getType()).getName(), akka.actor.ActorRef.class);
        }

        TypeSpec mainClass = TypeSpec.classBuilder("Main")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(mainBuilder.build())
//                .addField(system)
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME , mainClass)
                .build();

        Utils.writeToFile(rebecaFile, javaFile);
    }

    private void messages(List<ReactiveClassDeclaration> classes) {

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();

        TypeSpec.Builder messagesClass = TypeSpec.classBuilder("Messages")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor);
//                .addField(system);

        for (ReactiveClassDeclaration rcd : classes) {

            ClassName reactiveClassName = ClassName.get(PACKAGE_NAME, rcd.getName());
            TypeSpec.Builder reactiveClassBuilder = TypeSpec.classBuilder(reactiveClassName.simpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            for (MsgsrvDeclaration msgsrv : rcd.getMsgsrvs()) {
                ClassName messageName = ClassName.get(PACKAGE_NAME, msgsrv.getName());
                reactiveClassBuilder.addType(TypeSpec.classBuilder(messageName.simpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build());
            }

            messagesClass.addType(reactiveClassBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, messagesClass.build())
                .build();

        Utils.writeToFile(rebecaFile, javaFile);
    }

    private void injector(List<ReactiveClassDeclaration> classes) {
        TypeSpec.Builder injectorBuilder = TypeSpec.classBuilder("Injector")
                .addModifiers(Modifier.PUBLIC);

        for (ReactiveClassDeclaration rcd : classes) {
            ClassName reactiveClassName = ClassName.get(PACKAGE_NAME, rcd.getName() + "KnownActors");
            MethodSpec inject = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(akka.actor.ActorRef.class, "actor")
                    .addParameter(reactiveClassName, "args")
                    .addStatement("actor.tell(args, ActorRef.noSender())")
                    .build();

            injectorBuilder.addMethod(inject);
        }

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, injectorBuilder.build())
                .build();

        Utils.writeToFile(rebecaFile, javaFile);
    }

}
