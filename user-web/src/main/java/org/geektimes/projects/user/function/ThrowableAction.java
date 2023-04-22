package org.geektimes.projects.user.function;

@FunctionalInterface
public interface ThrowableAction {

    void execute() throws Throwable;

    static void execute(ThrowableAction action) throws RuntimeException{
        try {
            action.execute();
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }
}
