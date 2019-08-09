public class Runner {
    public static void main(String[] args) {

        ResolvedQueue queue = new ResolvedQueue();
        queue.offer(2);
        queue.offer(5);
        queue.offer(26);

        try {
            queue.getClass().getMethod("offer", Integer.class).invoke(queue, 10, 10);
        } catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println(queue.poll());

    }
}
