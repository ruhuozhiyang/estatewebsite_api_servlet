import java.io.IOException;
import java.nio.channels.Selector;

public class SelectorWakeup {

  public static void main(String[] args) throws IOException {
    Selector selector = Selector.open();
    selector.wakeup();
    selector.selectNow();
    selector.wakeup();
    selector.selectNow();
    selector.wakeup();
  }
}
