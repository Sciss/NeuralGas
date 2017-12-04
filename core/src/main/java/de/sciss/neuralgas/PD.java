// ========================================================================== ;
//                                                                            ;
// Copyright 1996-1998 Hartmut S. Loos, Instit. f. Neuroinformatik, Bochum    ;
// Copyright 2012-2013 Bernd Fritzke                                          ;
//                                                                            ;
// This program is free software; you can redistribute it and/or modify       ;
// it under the terms of the GNU General Public License as published by       ;
// the Free Software Foundation; either version 1, or (at your option)        ;
// any later version.                                                         ;
//                                                                            ;
// This program is distributed in the hope that it will be useful,            ;
// but WITHOUT ANY WARRANTY; without even the implied warranty of             ;
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              ;
// GNU General Public License for more details.                               ;
//                                                                            ;
// You should have received a copy of the GNU General Public License          ;
// along with this program; if not, write to the Free Software                ;
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                  ;
//                                                                            ;
// ========================================================================== ;

package de.sciss.neuralgas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import static de.sciss.neuralgas.ComputeGNG.RING_FACTOR;

/**
 * @author B. Fritzke
 * enum for all probability distributions
 */

public interface PD {
    void getSignal(ComputeGNG compute);
    void draw(ComputeGNG compute, PanelLike panel, Graphics g, Dimension d);

    String getName();
    int ordinal();

    final class Rectangle implements PD {
        @Override
        public int ordinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "Rectangle";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int ll = wi/20;
            final int lr = hi/20;
            final int r2 = wi*9/10;
            final int l2 = hi*9/10;
            compute.SignalX = (int) (ll + (r2 * compute.random()));
            compute.SignalY = (int) (lr + (l2 * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int ll = d.width/20;
            final int lr = d.height/20;
            final int r2 = d.width*9/10;
            final int l2 = d.height*9/10;
            g.fillRect(ll, lr, r2, l2);
        }
    }

    final class Circle implements PD {
        @Override
        public int ordinal() {
            return 1;
        }

        @Override
        public String getName() {
            return "Circle";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int cx = wi/2;
            final int cy = hi/2;
            final int minDim=(wi < hi) ? wi : hi;
            final int diameter = minDim*9/10; // Diameter is proportional to the smallest panel dimension

            final Point.Double sig = compute.circlePoint();
            compute.SignalX = (int) (sig.getX()*diameter+cx);
            compute.SignalY = (int) (sig.getY()*diameter+cy);
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int minDim = (d.width < d.height) ? d.width : d.height;
            final int l2 = minDim*9/10; // Diameter is proportional to the smallest panel dimension

            final int ll = d.width/2 -l2/2;
            final int lr = d.height/2 -l2/2;

            g.fillOval(ll, lr, l2, l2);
        }
    }

    final class TwoCircles implements PD {
        @Override
        public int ordinal() {
            return 2;
        }

        @Override
        public String getName() {
            return "TwoCircles";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int cx = wi/2;
            final int cy = hi/2;
            final int l2;
            // circle space circle (3x1)
            if (wi/3 < hi){
                // limiting dimension is width
                l2=(int) (wi/2.6);
            } else {
                // limiting dimension is height
                l2=(int) (hi*0.95);
            }
            final int diameter = l2; // Diameter is proportional to the smallest panel dimension
            final Point2D.Double sig = compute.circlePoint();
            int dx;

            if (compute.random()>0.5) {
                dx = -l2*3/4;
            } else {
                dx = l2*3/4;
            }
            compute.SignalX = (int) (sig.getX()*diameter+cx+dx);
            compute.SignalY = (int) (sig.getY()*diameter+cy);
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int l2;
            // circle space circle (3x1)
            if (d.width/3 < d.height){
                // limiting dimension is width
                l2=(int) (d.width/2.6);
            } else {
                // limiting dimension is height
                l2=(int) (d.height*0.95);
            }

            final int ll = d.width/2 -l2*5/4;
            final int lr = d.height/2 -l2/2;
            g.fillOval(ll, lr, l2, l2);

            final int ll1 = d.width/2 +l2/4;
            g.fillOval(ll1, lr, l2, l2);
        }
    }

    final class Ring implements PD {
        @Override
        public int ordinal() {
            return 3;
        }

        @Override
        public String getName() {
            return "Ring";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int cx = wi/2;
            final int cy = hi/2;
            final int l2 = (cx < cy) ? cx : cy; // Diameter
            final int ll = cx - l2;
            final int lr = cy - l2;
            final int ringRadius = (int) (l2 * RING_FACTOR);

            int SignalX;
            int SignalY;
            float rDist = 0f;

            do {
                SignalX = (int) (ll + (2*l2 * compute.random()));
                SignalY = (int) (lr + (2*l2 * compute.random()));
                rDist = (float) Math.sqrt(((cx - SignalX) *
                        (cx - SignalX) +
                        (cy - SignalY) *
                                (cy - SignalY)));
            } while ( (rDist > l2) || (rDist < (l2 - ringRadius)) );

            compute.SignalX = SignalX;
            compute.SignalY = SignalY;
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int cx = d.width/2; // horizontal center of panel
            final int cy = d.height/2;// vertical center of panel
            final int l2 = (cx < cy) ? cx : cy; // Diameter

            final int ll = cx - l2;
            final int lr = cy - l2;
            final int ringRadius = (int) (l2 * RING_FACTOR);

            g.fillOval(ll, lr, 2*l2, 2*l2);
            if (panel.isWhite())
                g.setColor(Color.white);
            else
                g.setColor(panel.getBackground());
            g.fillOval(ll + ringRadius,
                    lr+ringRadius,
                    2*l2-2*ringRadius,
                    2*l2-2*ringRadius);
        }
    }

    final class UNI implements PD {
        @Override
        public int ordinal() {
            return 4;
        }

        @Override
        public String getName() {
            return "UNI";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int w = wi/9;
            final int h = hi/5;
            final int xA[] = new int[14];
            final int yA[] = new int[14];
            xA[0] = w;
            yA[0] = h;
            xA[1] = w;
            yA[1] = 2*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = 2*w;
            yA[3] = 3*h;
            xA[4] = 3*w;
            yA[4] = 3*h;
            xA[5] = 3*w;
            yA[5] = 2*h;
            xA[6] = 3*w;
            yA[6] = h;
            xA[7] = 4*w;
            yA[7] = h;
            xA[8] = 5*w;
            yA[8] = h;
            xA[9] = 5*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 3*h;
            xA[11] = 7*w;
            yA[11] = h;
            xA[12] = 7*w;
            yA[12] = 2*h;
            xA[13] = 7*w;
            yA[13] = 3*h;

            final int z = (int) (14 * compute.random());
            compute.SignalX = (int) (xA[z] + (w * compute.random()));
            compute.SignalY = (int) (yA[z] + (h * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int w = d.width/9;
            final int h = d.height/5;
            final int xA[] = new int[14];
            final int yA[] = new int[14];
            xA[0] = w;
            yA[0] = h;
            xA[1] = w;
            yA[1] = 2*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = 2*w;
            yA[3] = 3*h;
            xA[4] = 3*w;
            yA[4] = 3*h;
            xA[5] = 3*w;
            yA[5] = 2*h;
            xA[6] = 3*w;
            yA[6] = h;
            xA[7] = 4*w;
            yA[7] = h;
            xA[8] = 5*w;
            yA[8] = h;
            xA[9] = 5*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 3*h;
            xA[11] = 7*w;
            yA[11] = h;
            xA[12] = 7*w;
            yA[12] = 2*h;
            xA[13] = 7*w;
            yA[13] = 3*h;

            for (int i = 0; i < 14; i++)
                g.fillRect(xA[i], yA[i], w, h);
        }
    }

    final class SmallSpirals implements PD {
        @Override
        public int ordinal() {
            return 5;
        }

        @Override
        public String getName() {
            return "SmallSpirals";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int w = wi/9;
            final int h = hi/7;
            final int xA[] = new int[22];
            final int yA[] = new int[22];
            xA[0] = w;
            yA[0] = 5*h;
            xA[1] = w;
            yA[1] = 4*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = w;
            yA[3] = 2*h;
            xA[4] = 1*w;
            yA[4] = h;
            xA[5] = 2*w;
            yA[5] = h;
            xA[6] = 3*w;
            yA[6] = h;
            xA[7] = 4*w;
            yA[7] = h;
            xA[8] = 5*w;
            yA[8] = 1*h;
            xA[9] = 5*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 3*h;
            xA[11] = 3*w;
            yA[11] = 3*h;
            xA[12] = 3*w;
            yA[12] = 4*h;
            xA[13] = 3*w;
            yA[13] = 5*h;
            xA[14] = 4*w;
            yA[14] = 5*h;
            xA[15] = 5*w;
            yA[15] = 5*h;
            xA[16] = 6*w;
            yA[16] = 5*h;
            xA[17] = 7*w;
            yA[17] = 5*h;
            xA[18] = 7*w;
            yA[18] = 4*h;
            xA[19] = 7*w;
            yA[19] = 3*h;
            xA[20] = 7*w;
            yA[20] = 2*h;
            xA[21] = 7*w;
            yA[21] = 1*h;

            final int z = (int) (22 * compute.random());
            compute.SignalX = (int) (xA[z] + (w * compute.random()));
            compute.SignalY = (int) (yA[z] + (h * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int w = d.width/9;
            final int h = d.height/7;
            final int xA[] = new int[22];
            final int yA[] = new int[22];
            xA[0] = w;
            yA[0] = 5*h;
            xA[1] = w;
            yA[1] = 4*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = w;
            yA[3] = 2*h;
            xA[4] = 1*w;
            yA[4] = h;
            xA[5] = 2*w;
            yA[5] = h;
            xA[6] = 3*w;
            yA[6] = h;
            xA[7] = 4*w;
            yA[7] = h;
            xA[8] = 5*w;
            yA[8] = 1*h;
            xA[9] = 5*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 3*h;
            xA[11] = 3*w;
            yA[11] = 3*h;
            xA[12] = 3*w;
            yA[12] = 4*h;
            xA[13] = 3*w;
            yA[13] = 5*h;
            xA[14] = 4*w;
            yA[14] = 5*h;
            xA[15] = 5*w;
            yA[15] = 5*h;
            xA[16] = 6*w;
            yA[16] = 5*h;
            xA[17] = 7*w;
            yA[17] = 5*h;
            xA[18] = 7*w;
            yA[18] = 4*h;
            xA[19] = 7*w;
            yA[19] = 3*h;
            xA[20] = 7*w;
            yA[20] = 2*h;
            xA[21] = 7*w;
            yA[21] = 1*h;

            for (int i = 0; i < 22; i++)
                g.fillRect(xA[i], yA[i], w, h);
        }
    }

    final class LargeSpirals implements PD {
        @Override
        public int ordinal() {
            return 6;
        }

        @Override
        public String getName() {
            return "LargeSpirals";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int xA[] = new int[58];
            final int yA[] = new int[58];
            final int w = wi/13;
            final int h = hi/11;
            xA[0] = w;
            yA[0] = h;
            xA[1] = w;
            yA[1] = 2*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = w;
            yA[3] = 4*h;
            xA[4] = 1*w;
            yA[4] = 5*h;
            xA[5] = 1*w;
            yA[5] = 6*h;
            xA[6] = 1*w;
            yA[6] = 7*h;
            xA[7] = 1*w;
            yA[7] = 8*h;
            xA[8] = 1*w;
            yA[8] = 9*h;
            xA[9] = 2*w;
            yA[9] = 1*h;
            xA[10] = 3*w;
            yA[10] = 1*h;
            xA[11] = 4*w;
            yA[11] = 1*h;
            xA[12] = 5*w;
            yA[12] = 1*h;
            xA[13] = 6*w;
            yA[13] = 1*h;
            xA[14] = 7*w;
            yA[14] = 1*h;
            xA[15] = 8*w;
            yA[15] = 1*h;
            xA[16] = 9*w;
            yA[16] = 1*h;
            xA[17] = 9*w;
            yA[17] = 2*h;
            xA[18] = 9*w;
            yA[18] = 3*h;
            xA[19] = 9*w;
            yA[19] = 4*h;
            xA[20] = 9*w;
            yA[20] = 5*h;
            xA[21] = 9*w;
            yA[21] = 6*h;
            xA[22] = 9*w;
            yA[22] = 7*h;
            xA[23] = 8*w;
            yA[23] = 7*h;
            xA[24] = 7*w;
            yA[24] = 7*h;
            xA[25] = 6*w;
            yA[25] = 7*h;
            xA[26] = 5*w;
            yA[26] = 7*h;
            xA[27] = 5*w;
            yA[27] = 6*h;
            xA[28] = 5*w;
            yA[28] = 5*h;
            xA[29] = 3*w;
            yA[29] = 3*h;
            xA[30] = 3*w;
            yA[30] = 4*h;
            xA[31] = 3*w;
            yA[31] = 5*h;
            xA[32] = 3*w;
            yA[32] = 6*h;
            xA[33] = 3*w;
            yA[33] = 7*h;
            xA[34] = 3*w;
            yA[34] = 8*h;
            xA[35] = 3*w;
            yA[35] = 9*h;
            xA[36] = 4*w;
            yA[36] = 3*h;
            xA[37] = 5*w;
            yA[37] = 3*h;
            xA[38] = 6*w;
            yA[38] = 3*h;
            xA[39] = 7*w;
            yA[39] = 3*h;
            xA[40] = 7*w;
            yA[40] = 4*h;
            xA[41] = 7*w;
            yA[41] = 5*h;
            xA[42] = 4*w;
            yA[42] = 9*h;
            xA[43] = 5*w;
            yA[43] = 9*h;
            xA[44] = 6*w;
            yA[44] = 9*h;
            xA[45] = 7*w;
            yA[45] = 9*h;
            xA[46] = 8*w;
            yA[46] = 9*h;
            xA[47] = 9*w;
            yA[47] = 9*h;
            xA[48] =10*w;
            yA[48] = 9*h;
            xA[49] =11*w;
            yA[49] = 9*h;
            xA[50] =11*w;
            yA[50] = 8*h;
            xA[51] =11*w;
            yA[51] = 7*h;
            xA[52] =11*w;
            yA[52] = 6*h;
            xA[53] =11*w;
            yA[53] = 5*h;
            xA[54] =11*w;
            yA[54] = 4*h;
            xA[55] =11*w;
            yA[55] = 3*h;
            xA[56] =11*w;
            yA[56] = 2*h;
            xA[57] =11*w;
            yA[57] = 1*h;

            final int z = (int) (58 * compute.random());
            compute.SignalX = (int) (xA[z] + (w * compute.random()));
            compute.SignalY = (int) (yA[z] + (h * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int w = d.width/13;
            final int h = d.height/11;
            final int xA[] = new int[58];
            final int yA[] = new int[58];
            xA[0] = w;
            yA[0] = h;
            xA[1] = w;
            yA[1] = 2*h;
            xA[2] = w;
            yA[2] = 3*h;
            xA[3] = w;
            yA[3] = 4*h;
            xA[4] = 1*w;
            yA[4] = 5*h;
            xA[5] = 1*w;
            yA[5] = 6*h;
            xA[6] = 1*w;
            yA[6] = 7*h;
            xA[7] = 1*w;
            yA[7] = 8*h;
            xA[8] = 1*w;
            yA[8] = 9*h;
            xA[9] = 2*w;
            yA[9] = 1*h;
            xA[10] = 3*w;
            yA[10] = 1*h;
            xA[11] = 4*w;
            yA[11] = 1*h;
            xA[12] = 5*w;
            yA[12] = 1*h;
            xA[13] = 6*w;
            yA[13] = 1*h;
            xA[14] = 7*w;
            yA[14] = 1*h;
            xA[15] = 8*w;
            yA[15] = 1*h;
            xA[16] = 9*w;
            yA[16] = 1*h;
            xA[17] = 9*w;
            yA[17] = 2*h;
            xA[18] = 9*w;
            yA[18] = 3*h;
            xA[19] = 9*w;
            yA[19] = 4*h;
            xA[20] = 9*w;
            yA[20] = 5*h;
            xA[21] = 9*w;
            yA[21] = 6*h;
            xA[22] = 9*w;
            yA[22] = 7*h;
            xA[23] = 8*w;
            yA[23] = 7*h;
            xA[24] = 7*w;
            yA[24] = 7*h;
            xA[25] = 6*w;
            yA[25] = 7*h;
            xA[26] = 5*w;
            yA[26] = 7*h;
            xA[27] = 5*w;
            yA[27] = 6*h;
            xA[28] = 5*w;
            yA[28] = 5*h;
            xA[29] = 3*w;
            yA[29] = 3*h;
            xA[30] = 3*w;
            yA[30] = 4*h;
            xA[31] = 3*w;
            yA[31] = 5*h;
            xA[32] = 3*w;
            yA[32] = 6*h;
            xA[33] = 3*w;
            yA[33] = 7*h;
            xA[34] = 3*w;
            yA[34] = 8*h;
            xA[35] = 3*w;
            yA[35] = 9*h;
            xA[36] = 4*w;
            yA[36] = 3*h;
            xA[37] = 5*w;
            yA[37] = 3*h;
            xA[38] = 6*w;
            yA[38] = 3*h;
            xA[39] = 7*w;
            yA[39] = 3*h;
            xA[40] = 7*w;
            yA[40] = 4*h;
            xA[41] = 7*w;
            yA[41] = 5*h;
            xA[42] = 4*w;
            yA[42] = 9*h;
            xA[43] = 5*w;
            yA[43] = 9*h;
            xA[44] = 6*w;
            yA[44] = 9*h;
            xA[45] = 7*w;
            yA[45] = 9*h;
            xA[46] = 8*w;
            yA[46] = 9*h;
            xA[47] = 9*w;
            yA[47] = 9*h;
            xA[48] =10*w;
            yA[48] = 9*h;
            xA[49] =11*w;
            yA[49] = 9*h;
            xA[50] =11*w;
            yA[50] = 8*h;
            xA[51] =11*w;
            yA[51] = 7*h;
            xA[52] =11*w;
            yA[52] = 6*h;
            xA[53] =11*w;
            yA[53] = 5*h;
            xA[54] =11*w;
            yA[54] = 4*h;
            xA[55] =11*w;
            yA[55] = 3*h;
            xA[56] =11*w;
            yA[56] = 2*h;
            xA[57] =11*w;
            yA[57] = 1*h;

            for (int i = 0; i < 58; i++)
                g.fillRect(xA[i], yA[i], w, h);
        }
    }

    final class HiLoDensity implements PD {
        @Override
        public int ordinal() {
            return 7;
        }

        @Override
        public String getName() {
            return "HiLoDensity";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int w = wi/10;
            final int h = hi/10;
            final int xA[] = new int[2];
            final int yA[] = new int[2];
            xA[0] = 2 * w;
            yA[0] = 4 * h;
            xA[1] = 5 * w;
            yA[1] = 1 * h;

            final int z = (int) (2 * compute.random());
            if (z == 0) {
                compute.SignalX = (int) (xA[z] + (w * compute.random()));
                compute.SignalY = (int) (yA[z] + (h * compute.random()));
            } else {
                compute.SignalX = (int) (xA[z] + (4 * w * compute.random()));
                compute.SignalY = (int) (yA[z] + (8 * h * compute.random()));
            }
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int w = d.width/10;
            final int h = d.height/10;
            final int xA[] = new int[2];
            final int yA[] = new int[2];
            xA[0] = 2 * w;
            yA[0] = 4 * h;
            xA[1] = 5 * w;
            yA[1] = 1 * h;

            final Algorithm algorithm = compute.algorithm;
            if (!algorithm.isDiscrete())
                g.setColor(panel.getHighDistributionColor());
            g.fillRect(xA[0], yA[0], w, h);
            if (!algorithm.isDiscrete())
                g.setColor(panel.getLowDistributionColor());
            g.fillRect(xA[1], yA[1], 4 * w, 8 * h);
        }
    }

    final class DiscreteMixture implements PD {
        @Override
        public int ordinal() {
            return 8;
        }

        @Override
        public String getName() {
            return "DiscreteMixture";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int z = (int) (compute.MIXTURE_SIZE * compute.random());
            compute.SignalX = Math.round(compute.discreteSignalsX[z]);
            compute.SignalY = Math.round(compute.discreteSignalsY[z]);
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            //int RADIUS = 2;
            final float[] discreteSignalsX  = compute.discreteSignalsX;
            final float[] discreteSignalsY  = compute.discreteSignalsY;
            final int numDiscreteSignals    = compute.numDiscreteSignals;
            for (int i = 0; i < numDiscreteSignals; i++) {
                final int x = Math.round(discreteSignalsX[i]);
                final int y = Math.round(discreteSignalsY[i]);

                g.setColor(panel.getDistributionColor());
                g.fillOval(x - 1, y - 1, 2, 2);
                g.setColor(Color.black);
                g.drawOval(x - 1, y - 1, 2, 2);
            }
        }
    }

    final class UNIT implements PD {
        @Override
        public int ordinal() {
            return 9;
        }

        @Override
        public String getName() {
            return "UNIT";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int xA[] = new int[28];
            final int yA[] = new int[28];
            final int w = wi/17;
            final int h = hi/8;
            xA[0] = w;
            yA[0] = 2*h;
            xA[1] = w;
            yA[1] = 3*h;
            xA[2] = w;
            yA[2] = 4*h;
            xA[3] = w;
            yA[3] = 5*h;
            xA[4] = 2*w;
            yA[4] = 5*h;
            xA[5] = 3*w;
            yA[5] = 5*h;
            xA[6] = 3*w;
            yA[6] = 4*h;
            xA[7] = 3*w;
            yA[7] = 3*h;
            xA[8] = 3*w;
            yA[8] = 2*h;
            xA[9] = 4*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 2*h;
            xA[11] = 6*w;
            yA[11] = 2*h;
            xA[12] = 7*w;
            yA[12] = 2*h;
            xA[13] = 7*w;
            yA[13] = 3*h;
            xA[14] = 7*w;
            yA[14] = 4*h;
            xA[15] = 7*w;
            yA[15] = 5*h;
            xA[16] = 8*w;
            yA[16] = 5*h;
            xA[17] = 9*w;
            yA[17] = 5*h;
            xA[18] = 10*w;
            yA[18] = 5*h;
            xA[19] = 11*w;
            yA[19] = 5*h;
            xA[20] = 11*w;
            yA[20] = 4*h;
            xA[21] = 11*w;
            yA[21] = 3*h;
            xA[22] = 11*w;
            yA[22] = 2*h;
            xA[23] = 14*w;
            yA[23] = 2*h;
            xA[24] = 15*w;
            yA[24] = 2*h;
            xA[25] = 15*w;
            yA[25] = 3*h;
            xA[26] = 15*w;
            yA[26] = 4*h;
            xA[27] = 15*w;
            yA[27] = 5*h;

            final int z = (int) (28 * compute.random());
            compute.SignalX = (int) (xA[z] + (w * compute.random()));
            compute.SignalY = (int) (yA[z] + (h * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int w = d.width/17;
            final int h = d.height/8;
            final int xA[] = new int[28];
            final int yA[] = new int[28];
            xA[0] = w;
            yA[0] = 2*h;
            xA[1] = w;
            yA[1] = 3*h;
            xA[2] = w;
            yA[2] = 4*h;
            xA[3] = w;
            yA[3] = 5*h;
            xA[4] = 2*w;
            yA[4] = 5*h;
            xA[5] = 3*w;
            yA[5] = 5*h;
            xA[6] = 3*w;
            yA[6] = 4*h;
            xA[7] = 3*w;
            yA[7] = 3*h;
            xA[8] = 3*w;
            yA[8] = 2*h;
            xA[9] = 4*w;
            yA[9] = 2*h;
            xA[10] = 5*w;
            yA[10] = 2*h;
            xA[11] = 6*w;
            yA[11] = 2*h;
            xA[12] = 7*w;
            yA[12] = 2*h;
            xA[13] = 7*w;
            yA[13] = 3*h;
            xA[14] = 7*w;
            yA[14] = 4*h;
            xA[15] = 7*w;
            yA[15] = 5*h;
            xA[16] = 8*w;
            yA[16] = 5*h;
            xA[17] = 9*w;
            yA[17] = 5*h;
            xA[18] = 10*w;
            yA[18] = 5*h;
            xA[19] = 11*w;
            yA[19] = 5*h;
            xA[20] = 11*w;
            yA[20] = 4*h;
            xA[21] = 11*w;
            yA[21] = 3*h;
            xA[22] = 11*w;
            yA[22] = 2*h;
            xA[23] = 14*w;
            yA[23] = 2*h;
            xA[24] = 15*w;
            yA[24] = 2*h;
            xA[25] = 15*w;
            yA[25] = 3*h;
            xA[26] = 15*w;
            yA[26] = 4*h;
            xA[27] = 15*w;
            yA[27] = 5*h;

            for (int i = 0; i < 28; i++)
                g.fillRect(xA[i], yA[i], w, h);
        }
    }

    final class MoveJump implements PD {
        @Override
        public int ordinal() {
            return 10;
        }

        @Override
        public String getName() {
            return "MoveJump";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int r2 = wi/4;
            final int l2 = hi/4;
            final int numSignals = compute.numSignals;
            final int ll = (int) (0.75 * (wi/2 +
                    Math.IEEEremainder(0.2 * numSignals,(wi))));
            final int lr = (int) (0.75 * (hi/2 +
                    Math.IEEEremainder(0.2 * numSignals,(hi))));

            compute.SignalX = (int) (ll + (r2 * compute.random()));
            compute.SignalY = (int) (lr + (l2 * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int r2 = d.width/4;
            final int l2 = d.height/4;
            final int ns0 = compute.numSignals;
            final int ll = (int) (0.75 * (d.width/2 +
                    Math.IEEEremainder(0.2 * ns0,(d.width))));
            final int lr = (int) (0.75 * (d.height/2 +
                    Math.IEEEremainder(0.2 * ns0,(d.height))));

            g.fillRect(ll, lr, r2, l2);
        }
    }

    final class Move implements PD {
        @Override
        public int ordinal() {
            return 11;
        }

        @Override
        public String getName() {
            return "Move";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int r2 = wi/4;
            final int l2 = hi/4;
            final int numSignals = compute.numSignals;
            final double remainderX = Math.IEEEremainder(0.2 * numSignals,(wi));
            final double remainderY = Math.IEEEremainder(0.2 * numSignals,(hi));

            if ( (compute.bounceX_old > 0) && (remainderX < 0) )
                compute.bounceX = compute.bounceX * (-1);
            if ( (compute.bounceY_old > 0) && (remainderY < 0) )
                compute.bounceY = compute.bounceY * (-1);

            final int ll = (int) (0.75 * (wi/2 + compute.bounceX * remainderX));
            final int lr = (int) (0.75 * (hi/2 + compute.bounceY * remainderY));

            compute.bounceX_old = remainderX;
            compute.bounceY_old = remainderY;

            compute.SignalX = (int) (ll + (r2 * compute.random()));
            compute.SignalY = (int) (lr + (l2 * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int r2 = d.width/4;
            final int l2 = d.height/4;
            final int ns1 = compute.numSignals;
            final int ll = (int) (0.75 * (d.width/2 +
                    compute.bounceX * Math.IEEEremainder(0.2 * ns1,
                            (d.width))));
            final int lr = (int) (0.75 * (d.height/2 +
                    compute.bounceY * Math.IEEEremainder(0.2 * ns1,
                            (d.height))));

            g.fillRect(ll, lr, r2, l2);
        }
    }

    final class Jump implements PD {
        @Override
        public int ordinal() {
            return 12;
        }

        @Override
        public String getName() {
            return "Jump";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int r2 = wi/4;
            final int l2 = hi/4;

            if (Math.ceil(Math.IEEEremainder(compute.numSignals, 1000.0)) == 0) {
                compute.jumpX = (int) ((wi - r2) * compute.random());
                compute.jumpY = (int) ((hi - l2) * compute.random());
            }

            compute.SignalX = (int) (compute.jumpX + (r2 * compute.random()));
            compute.SignalY = (int) (compute.jumpY + (l2 * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int r2 = d.width/4;
            final int l2 = d.height/4;
            g.fillRect(compute.jumpX, compute.jumpY, r2, l2);
        }
    }

    final class RightMouseB implements PD {
        @Override
        public int ordinal() {
            return 13;
        }

        @Override
        public String getName() {
            return "RightMouseB";
        }

        @Override
        public void getSignal(ComputeGNG compute) {
            final int wi = compute.panelWidth;
            final int hi = compute.panelHeight;
            final int r2 = wi/4;
            final int l2 = hi/4;

            compute.SignalX = (int) (compute.jumpX + (r2 * compute.random()));
            compute.SignalY = (int) (compute.jumpY + (l2 * compute.random()));
        }

        @Override
        public void draw(ComputeGNG compute, final PanelLike panel, final Graphics g, final Dimension d) {
            final int r2 = d.width/4;
            final int l2 = d.height/4;

            g.fillRect(compute.jumpX, compute.jumpY, r2, l2);
        }
    }

    PD Rectangle        = new Rectangle();
    PD Circle           = new Circle();
    PD TwoCircles       = new TwoCircles();
    PD Ring             = new Ring();
    PD UNI              = new UNI();
    PD SmallSpirals     = new SmallSpirals();
    PD LargeSpirals     = new LargeSpirals();
    PD HiLoDensity      = new HiLoDensity();
    PD DiscreteMixture  = new DiscreteMixture();
    PD UNIT             = new UNIT();
    PD MoveJump         = new MoveJump();
    PD Move             = new Move();
    PD Jump             = new Jump();
    PD RightMouseB      = new RightMouseB();

    PD[] values = {
            Rectangle, Circle, TwoCircles, Ring, UNI, SmallSpirals, LargeSpirals,
            HiLoDensity, DiscreteMixture, UNIT, MoveJump, Move, Jump, RightMouseB
    };
}
