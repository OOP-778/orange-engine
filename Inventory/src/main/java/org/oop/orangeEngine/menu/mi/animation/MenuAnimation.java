package org.brian.core.mi.animation;

import org.brian.core.energiser.ETask;
import org.brian.core.mi.AMenu;
import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.designer.MenuDesigner;
import org.brian.core.utils.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MenuAnimation {

    private long switchEvery = 1000;
    private Map<Integer, MenuDesigner> frameList = new HashMap<>();
    private Consumer<AMenu> runEachFrame;
    private int repeatTimes = 1;

    private int currentFrame = -1;
    private AMenu owner;
    private ETask task;
    private int repeatCount = 0;

    public void start() {

        Assert.assertTrue("How do I know what to animate if owner isn't defined, huh?", owner == null);

        Consumer<ETask> animationConsumer = (task) -> {

            //Check for current frame
            if (currentFrame == -1) currentFrame = frameList.keySet().stream().findFirst().orElse(-1);

            if (currentFrame == -1) {
                task.shutdown();
                return;
            }

            MenuDesigner menuDesigner = frameList.get(currentFrame);

            //Clean buttons with filler mark
            owner.buttonList().removeIf(AMenuButton::isFiller);

            //Put new fillers
            menuDesigner.applyAsButtons(owner);

            owner.build();
            if (runEachFrame != null) runEachFrame.accept(owner);

            currentFrame++;

        };

        this.task = new ETask(animationConsumer);
        task.repeat(true);
        task.delay(switchEvery);

    }

}
