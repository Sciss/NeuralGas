package de.sciss.neuralgas;

import java.awt.Color;

public interface PanelLike {
    Color getBackground();
    boolean isWhite();

    Color getDistributionColor();
    Color getLowDistributionColor();
    Color getHighDistributionColor();
}
