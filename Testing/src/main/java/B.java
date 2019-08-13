import s.A;

public class B<T> extends A {

    public B() {

        System.out.println(getClass().getSuperclass().getName());
    }

}
