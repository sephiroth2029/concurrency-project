import io.atomix.core.Atomix;

public class Main {

    public static void main(String[] args) {
        System.setProperty("atomix.data", "member-1-data");
        Atomix atomix = new Atomix("D:\\Development\\Projects\\MSc\\Concurrency\\project\\atomixreplica\\src\\main\\resources\\atomix.conf");

        atomix.start().join();
    }

}
