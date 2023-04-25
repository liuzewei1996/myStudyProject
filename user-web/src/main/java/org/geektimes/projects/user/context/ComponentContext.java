package org.geektimes.projects.user.context;

import org.geektimes.function.ThrowableAction;
import org.geektimes.function.ThrowableFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.*;
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 组件上下文，web应用全局使用
 */
public class ComponentContext {

    //    public static final String COMPONENT_NAME = ComponentContext.class.getSimpleName();
    public static final String COMPONENT_NAME = ComponentContext.class.getName();
    private static ServletContext servletContext;// 请注意
    // 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）
    // static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）

    private Context envContext;

    private ClassLoader classLoader;

    private Map<String, Object> componentsMap = new LinkedHashMap<>();


    private static final Logger logger = Logger.getLogger(COMPONENT_NAME);


    public void initalize(ServletContext servletContext) throws RuntimeException {
//        try {
//            this.context = (Context) new InitialContext().lookup("java:comp/env/");
//        } catch (NamingException e) {
//            throw new RuntimeException(e);
//        }
//        servletContext.setAttribute(COMPONENT_NAME, this);
//        ComponentContext.servletContext = servletContext;

        ComponentContext.servletContext = servletContext;
        servletContext.setAttribute(COMPONENT_NAME, this);
        // 获取当前 ServletContext（WebApp）ClassLoader
        this.classLoader = servletContext.getClassLoader();
        initEnvContext();//lookup("java:comp/env/")
        //实例化组件
        instantiateComponents();
        //初始化组件
        initializeComponents();
    }

    private void initEnvContext() throws RuntimeException {
        if (this.envContext != null) {
            return;
        }
        Context context = null;
        try {
            context = new InitialContext();
            this.envContext = (Context) context.lookup("java:comp/env");
        } catch (NamingException e) {
            logger.warning("envContext not found");
            throw new RuntimeException(e);
        } finally {
            close(context);
        }
    }

    /**
     * 实例化组件
     */
    protected void instantiateComponents() {
        List<String> componentNames = listAllComponentNames();
        componentNames.forEach(name -> componentsMap.put(name, lookupComponent(name)));
    }

    /**
     * 初始化组件
     * <ol>
     *  <li>注入阶段 - {@link Resource}</li>
     *  <li>初始阶段 - {@link PostConstruct}</li>
     *  <li>销毁阶段 - {@link PreDestroy}</li>
     * </ol>
     */
    protected void initializeComponents() {
        componentsMap.values().forEach(component -> {
//            Class<?> componentClass = component.getClass();
            //注入阶段
            injectComponents(component);
            //初始化阶段
            processPostConstruct(component);
            //销毁阶段
            processPreDestroy();
        });
    }

    private void injectComponents(Object component) {
        Class<?> componentClass = component.getClass();
        Stream.of(componentClass.getDeclaredFields())
            .filter(field -> {
                int mods = field.getModifiers();
                return !Modifier.isStatic(mods) && field.isAnnotationPresent(Resource.class);
            })
            .forEach(field -> {
                Resource resource = field.getAnnotation(Resource.class);
                String resourceName = resource.name();
                Object injectedObject = lookupComponent(resourceName);
                field.setAccessible(true);
                try {
                    // 注入目标对象
                    field.set(component, injectedObject);
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "Error Injecting the object", e);
                }
            });

    }

    private void processPostConstruct(Object component) {
        Class<?> componentClass = component.getClass();
        Stream.of(componentClass.getMethods())
            .filter(method ->
                    !Modifier.isStatic(method.getModifiers()) &&      // 非 static
                            method.getParameterCount() == 0 &&        // 没有参数
                            method.isAnnotationPresent(PostConstruct.class) // 标注 @PostConstruct)
            ).forEach(method -> {
                try {
                    method.invoke(component);
                } catch (Exception e) {
                    throw new RuntimeException(e);

                }
            });
    }


    private List<String> listAllComponentNames() {
        return listComponentNames("/");
    }

    protected List<String> listComponentNames(String name) {
        return executeInContext(context -> {
            NamingEnumeration<NameClassPair> e = executeInContext(context, ctx -> ctx.list(name), true);
            if (e == null) { // 当前 JNDI 名称下没有子节点
                return Collections.emptyList();
            }
            List<String> fullNames = new LinkedList<>();
            while (e.hasMoreElements()) {
                NameClassPair classPair = e.nextElement();
                String className = classPair.getClassName();
                Class<?> targetClass = classLoader.loadClass(className);
                if (Context.class.isAssignableFrom(targetClass)) {
                    // 如果当前名称是目录（Context 实现类）的话，递归查找
                    fullNames.addAll(listComponentNames(classPair.getName()));
                } else {
                    // 否则，当前名称绑定目标类型的话，添加该名称到集合中
                    String fullName = name.startsWith("/") ? classPair.getName() : name + "/" + classPair.getName();
                    fullNames.add(fullName);
                }
            }
            return fullNames;

        });
    }

    protected <C> C lookupComponent(String name) {
        return executeInContext(context -> (C) context.lookup(name));
    }

    protected <R> R executeInContext(ThrowableFunction<Context, R> function) {
        return executeInContext(function, false);
    }

    protected <R> R executeInContext(ThrowableFunction<Context, R> function, boolean ignoredException) {
        return executeInContext(this.envContext, function, ignoredException);
    }

    private <R> R executeInContext(Context context, ThrowableFunction<Context, R> function,
                                   boolean ignoredException) {
        R result = null;
        try {
            result = ThrowableFunction.execute(context, function);
        } catch (Throwable e) {
            if (ignoredException) {
                logger.warning(e.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        }
        return result;
    }


    public static ComponentContext getInstance() {
//        return (ComponentContext) servletContext.getContext(COMPONENT_NAME);
        return (ComponentContext) servletContext.getAttribute(COMPONENT_NAME);
    }

    /**
     * 提供给外部使用通过名称进行依赖查找
     * 内部使用请参照：@link lookupComponent
     * @param name
     * @param <C>
     * @return
     */
    public <C> C getComponent(String name) {
        return (C) componentsMap.get(name);
    }

    public List<String> getComponentNames(){
        return new ArrayList<>(componentsMap.keySet());
    }

    private static void close(Context context) {
        if (context != null) {
            ThrowableAction.execute(() -> context.close());
        }
    }

    private void processPreDestroy() {
        // TODO
        logger.log(java.util.logging.Level.FINE, "component pre destroy");
        System.out.println("销毁阶段");
    }

    public synchronized void destroy() throws RuntimeException {
        close(this.envContext);
    }


}
