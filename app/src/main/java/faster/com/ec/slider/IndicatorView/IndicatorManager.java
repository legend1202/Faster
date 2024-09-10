package faster.com.ec.slider.IndicatorView;

import androidx.annotation.Nullable;
import faster.com.ec.slider.IndicatorView.animation.AnimationManager;
import faster.com.ec.slider.IndicatorView.animation.controller.ValueController;
import faster.com.ec.slider.IndicatorView.animation.data.Value;
import faster.com.ec.slider.IndicatorView.draw.DrawManager;
import faster.com.ec.slider.IndicatorView.draw.data.Indicator;

public class IndicatorManager implements ValueController.UpdateListener {

    private DrawManager drawManager;
    private AnimationManager animationManager;
    private Listener listener;

    interface Listener {
        void onIndicatorUpdated();
    }

    IndicatorManager(@Nullable Listener listener) {
        this.listener = listener;
        this.drawManager = new DrawManager();
        this.animationManager = new AnimationManager(drawManager.indicator(), this);
    }

    public AnimationManager animate() {
        return animationManager;
    }

    public Indicator indicator() {
        return drawManager.indicator();
    }

    public DrawManager drawer() {
        return drawManager;
    }

    @Override
    public void onValueUpdated(@Nullable Value value) {
        drawManager.updateValue(value);
        if (listener != null) {
            listener.onIndicatorUpdated();
        }
    }
}
