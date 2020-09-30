public class DummyTest {
    public static void main(String[] args) {
        String s = "https://woof.tube/stream/RhjUyoHfznU";
        String domain = s.split("https.//")[1].split("/")[0];
        System.out.println(domain);
    }
}
