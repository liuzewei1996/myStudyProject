package org.geektimes.projects.user.context;

import org.geektimes.projects.user.function.ThrowableAction;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * 组件上下文，web应用全局使用
 */
public class ComponentContext {

    //    public static final String COMPONENT_NAME = ComponentContext.class.getSimpleName();
    public static final String COMPONENT_NAME = ComponentContext.class.getName();
    private static ServletContext servletContext;// 请注意
    // 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）
    // static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）

    private Context context;

    private static final Logger logger = Logger.getLogger(COMPONENT_NAME);


    public void initalize(ServletContext servletContext) throws RuntimeException {
        try {
            this.context = (Context) new InitialContext().lookup("java:comp/env/");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        servletContext.setAttribute(COMPONENT_NAME, this);
        ComponentContext.servletContext  = servletContext;
    }

    public static ComponentContext getInstance() {
        return (ComponentContext) servletContext.getContext(COMPONENT_NAME);
    }

    /**
     * 通过名称进行依赖查找
     * @param name
     * @param <C>
     * @return
     */
    public <C> C getComponent(String name) {
        C component = null;
        try {
            component = (C) context.lookup(name);
        } catch (NamingException e) {
            throw new NoSuchElementException(name);
        }
        return  component;
    }

    private static void close(Context context) {
        if (context != null) {
            ThrowableAction.execute(() -> context.close());
        }
    }

    public synchronized void destroy() throws RuntimeException{
        if(this.context!= null){
            try{
                this.context.close();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

}
