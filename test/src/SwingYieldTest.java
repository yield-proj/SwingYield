import com.xebisco.yield.GameConfiguration;
import com.xebisco.yield.YldGame;

public class SwingYieldTest extends YldGame {
    public static void main(String[] args) {
        GameConfiguration config = new GameConfiguration();
        config.renderMasterName = "com.xebisco.swingyield.SwingYield";
        launch(new SwingYieldTest(), config);
    }
}
