package de.sciss.neuralgas.ui;

import de.sciss.neuralgas.Algorithm;
import de.sciss.neuralgas.ComputeGNG;
import de.sciss.neuralgas.EdgeGNG;
import de.sciss.neuralgas.GridNodeGNG;
import de.sciss.neuralgas.LineFloat2D;
import de.sciss.neuralgas.NodeGNG;
import de.sciss.neuralgas.PD;
import de.sciss.neuralgas.PanelLike;
import de.sciss.neuralgas.Voronoi;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

@SuppressWarnings("serial")
class PanelGNG extends JPanel implements
        PanelLike,
        Runnable,
        MouseMotionListener,
        MouseListener,
        ComponentListener,
        ChangeListener
{
    Voronoi voro;

    int delay = 10;

    void log(String prefix, String txt) {
        System.out.println(timeStamp()+" C: "+prefix+txt);
    }

    void log(String txt) {
        log("####### ", txt);
    }

    public void setSize(Dimension d) {
        log("COM: setsize");
    }

    final static private SimpleDateFormat format
            = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");

    public synchronized String timeStamp() {
        long lDateTime = new Date().getTime();
        String str;
        str = format.format(new java.util.Date())+"."+String.format("%03d",lDateTime%1000);
        return str;
    }

    /**
     * The version of the Growing Neural Gas Demo.
     */
    protected static final String DEMO_GNG_VERSION = "v2.2.0-SNAPSHOT"; // Version

    protected DemoGNG graph;

    Thread relaxer;
    GraphGNG errorGraph;
    final ComputeGNG compute;
    final ComputeGNG.Result result;

    @Override
    public boolean isWhite() {
        return whiteB;
    }

    @Override
    public Color getDistributionColor() {
        return distribColor;
    }

    @Override
    public Color getLowDistributionColor() {
        return lowDistribColor;
    }

    @Override
    public Color getHighDistributionColor() {
        return highDistribColor;
    }

    /**
     * The flag for a white background. Useful for making hard-copies
     */
    protected boolean whiteB = false;

    /**
     * The flag for showing the signal.
     *  This variable can be set by the user and shows the last input numSignals.
     */
    protected boolean signalsB = false;

    /**
     * display GG network in mapSpace
     */
    protected boolean mapSpaceGGB = false;
    /**
     * display SOM network in mapSpace
     */
    protected boolean mapSpaceSOMB = false;
    /**
     * The flag for displaying tau values
     */
    protected boolean tauB = false;

    /**
     * The flag for displaying usage
     */
    protected boolean usageB = false;

    /**
     * The flag for the teach-mode.
     *  This variable can be set by the user. If true a legend is displayed
     *  which describes the new form and color of some nodes. Furthermore
     *  all calculation is very slow.
     */
    protected boolean teachB = false;

    /**
     * The flag for displaying the edges.
     *  This variable can be set by the user.
     */
    protected boolean edgesB = true;

    /**
     * The flag for displaying the nodes.
     *  This variable can be set by the user.
     */
    protected boolean nodesB = true;

    /**
     * The flag for displaying the (motion) traces.
     *  This variable can be set by the user.
     */
    protected boolean tracesB = false;

    /**
     * The flag for displaying the error graph.
     *  This variable can be set by the user.
     */
    protected boolean errorGraphB = false;

    /**
     * The flag for displaying the probability distribution
     *  This variable can be set by the user.
     */
    protected boolean probDistB = true;

    /**
     * The flag for any moved nodes (to compute the Voronoi diagram/Delaunay
     *  triangulation).
     */
    protected boolean nodesMovedB = true;

    /**
     * This variable determines how long the compute thread sleeps. In this time
     *  the user can interact with the program. Slow machines and/or slow
     *  WWW-browsers need more time than fast machines and/or browsers.
     *  This variable can be set by the user.
     */
    protected int tSleep = 10; //should be taken from the speed choice

    /**
     * The constructor.
     *
     * @param graph	The drawing area
     */
    PanelGNG(DemoGNG graph) {
        addMouseMotionListener(this);
        addMouseListener(this);
        addComponentListener(this);
        this.graph      = graph;
        this.compute    = graph.compute;
        this.voro       = new Voronoi(compute);
        this.result     = new ComputeGNG.Result();
    }

    /**
     * Do resize calculations, start the learning method.
     *
     */
    @Override
    public void run() {
        int i;
        log("run(): run and learn until stopped .............................");
        if (graph.isGuiInitialized()){
            log("run(): gui already initialized ....");
        } else {
            log("run(): gui not yet initialized ....");
            //graph.prepareAlgo(algorithm);
        }

        int iter = 0;

        while (true) {

            // Relativate Positions
            Dimension d = getSize();
            if ( (d.width != compute.panelWidth) || (d.height != compute.panelHeight) ) {
                //
                // panel size has changed! ==> rescaling needed
                //
                nodesMovedB = true; // for Voronoi
                NodeGNG n;
                final int nNodes        = compute.nNodes;
                final NodeGNG[] nodes   = compute.nodes;
                final int panelWidth    = compute.panelWidth;
                final int panelHeight   = compute.panelHeight;
                for (i = 0 ; i < nNodes ; i++) {
                    n = nodes[i];

                    n.x = n.x * d.width  / panelWidth;
                    n.y = n.y * d.height / panelHeight;
                }

                if (compute.pd == PD.DiscreteMixture || compute.algorithm.isDiscrete()){
                    compute.rescaleDiscreteSignals(1.0*d.width / panelWidth, 1.0*d.height / panelHeight);
                }
                compute.panelWidth  = d.width;
                compute.panelHeight = d.height;
                //initDiscreteSignals(pd);
                if ( ( nNodes == 0) && (compute.algorithm.isDiscrete()) ) {
                    // can this happen???
                    // Generate some nodes
                    final int numDiscreteSignals = compute.numDiscreteSignals;
                    int z = (int) (numDiscreteSignals * compute.random());
                    int mod = 0;
                    final float[] discreteSignalsX = compute.discreteSignalsX;
                    final float[] discreteSignalsY = compute.discreteSignalsY;
                    final int maxNodes = compute.maxNodes;
                    for (i = 0; i < maxNodes; i++) {
                        mod = (z+i)%numDiscreteSignals;
                        compute.addNode(
                                Math.round(discreteSignalsX[mod]),
                                Math.round(discreteSignalsY[mod]));
                    }
                }
                repaint();
            }

            // Calculate the new positions
            if (!result.stop) {
                synchronized(this) {
                    compute.learn(result);
                }
                iter++;
//                System.out.println(compute.nNodes);
//                if (iter == 1000) {
//                    for (int k = 0; k < compute.nNodes; k++) {
//                        System.out.println(compute.nodes[k]);
//                    }
//                }

                if (result.repaint) {
                    repaint();
                }
                if (result.stop) {
                    graph.stop();
                }
                nodesMovedB = true;
            }

            // update error graph
            if (errorGraphB && !result.stop)
                errorGraph.graph.add(compute.valueGraph);

            if (result.stop)
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            else {
                repaint(); // todo: only when needed?
            }

            if (teachB) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(tSleep);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        log("run() end");
    }

    /**
     * The mouse-selected node.
     */
    protected NodeGNG pick;
    /**
     * The flag for mouse-selected node.
     */
    protected boolean pickFixed;

    /**
     * The color of input numSignals
     */
    protected final Color signalsColor = Color.red;
    /**
     * The color of the 1-D SOM torus.
     */
    protected final Color torusColor = Color.cyan;
    /**
     * The color of SOM polygons.
     */
    protected final Color somColor = Color.yellow;
    /**
     * The color of the unused nodes (usage=true).
     */
    protected final Color unusedColor = Color.black;
    /**
     * The color of the winner node (teach-mode).
     */
    protected final Color winnerColor = Color.red;
    /**
     * The color of the second node (teach-mode).
     */
    protected final Color secondColor = Color.orange;
    /**
     * The color of the last moved nodes (teach-mode).
     */
    protected final Color movedColor = Color.yellow;
    /**
     * The color of the last inserted node (teach-mode).
     */
    protected final Color insertedColor = Color.blue;
    /**
     * The color of the shown signal (teach-mode).
     */
    protected final Color signalColor = Color.black;
    /**
     * The color of the edges.
     */
    protected final Color edgeColor = Color.black;
    /**
     * The color of the Voronoi diagram.
     */
    protected final Color voronoiColor = Color.red;
    /**
     * The color of the Delaunay diagram.
     */
    protected final Color delaunayColor = new Color(205,155,29);
    /**
     * The color of the nodes.
     */
    protected final Color nodeColor = Color.green;
    /**
     * The color of the distribution.
     */
    protected final Color distribColor = new Color(203, 205, 252);
    /**
     * The color of the low density distribution.
     */
    protected final Color lowDistribColor = new Color(203, 205, 252);
    /**
     * The color of the high density distribution.
     */
    protected final Color highDistribColor = new Color(152, 161, 250);

    /**
     * Paint a node.
     *
     * @param g            The graphic context
     * @param n            The node
     */
    public void paintNode(Graphics g, NodeGNG n) {
        int RADIUS = 10;
        Color col = nodeColor;
        final Algorithm algorithm = compute.algorithm;

        if (teachB && (!algorithm.isDiscrete()) ) {
            if (n.isWinner) {
                RADIUS += 5;
                col = winnerColor;
            } else if (n.isSecond) {
                RADIUS += 3;
                col = secondColor;
            }
//			else if (n.hasMoved) {
//				RADIUS += 2;
//				col = movedColor;
//			}
        }

        if (algorithm.isSOMType() && usageB){
            if (n.tau < 1.0){
                //Color c = nodeColor;
                //col = new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(100*n.tau));
                //col = mixColor(nodeColor,Color.black,n.tau);
                col=unusedColor;
            }
        }
        if (n.isMostRecentlyInserted)  {
            RADIUS += 2;
            col = insertedColor;
        }

        if ( (algorithm.isDiscrete()) && (!n.hasMoved) ) {
            RADIUS += 4;
            col = movedColor;
        }

        g.setColor(col);
        if (mapSpaceGGB && (algorithm == Algorithm.GG|| algorithm == Algorithm.GR) || mapSpaceSOMB && algorithm == Algorithm.SOM) {
            g.fillOval((int)(gx2x((int)n.x_grid) - (RADIUS/2)), (int)(gy2y((int)n.y_grid) - (RADIUS/2)), RADIUS, RADIUS);
        } else {
            g.fillOval((int)n.x - (RADIUS/2), (int)n.y - (RADIUS/2), RADIUS, RADIUS);
        }
        g.setColor(Color.black);
        if (mapSpaceGGB && (algorithm == Algorithm.GG|| algorithm == Algorithm.GR) || mapSpaceSOMB && algorithm == Algorithm.SOM) {
            g.drawOval((int)gx2x(n.x_grid) - (RADIUS/2), (int)gy2y(n.y_grid) - (RADIUS/2), RADIUS, RADIUS);
        } else {
            g.drawOval((int)n.x - (RADIUS/2), (int)n.y - (RADIUS/2), RADIUS, RADIUS);
        }

    }

    // repaint --> update() ---> paintComponent()
//	/**
//	 * Update the drawing area.
//	 *
//	 * @param g          The graphic context
//	 */
//    static int paintCounter =0;
//    static int prevSigs=0;

    // helper function to compute mapspace coordinates for SOM-like networks
    public int gx2x(int gx){
        Dimension d = getSize();
        return (int) (gx *d.width*0.9f/(compute.gridWidth-1)+0.05*d.width);
    }
    public int gy2y(int gy){
        Dimension d = getSize();
        return (int) (gy *d.height*0.9f/(compute.gridHeight-1)+0.05*d.height);
    }

    protected void drawPD(final Graphics g, final Dimension d) {
        compute.pd.draw(compute, this, g, d);
    }

    public synchronized void paintComponent(Graphics g0) {
        final Graphics2D g = (Graphics2D) g0;
//        if (true) {
//            System.out.println(compute.nNodes);
//            return;
//        }

        //log("paintComponent() CGNG " + String.valueOf(paintCounter)+" numSignals:"+String.valueOf(numSignals) + "delta-sig:"+String.valueOf(numSignals - prevSigs));
//        paintCounter +=1;
//        prevSigs = compute.numSignals;
        Dimension d = getSize();
        int i, x, y;
        final Algorithm algorithm = compute.algorithm;

        if (whiteB)
            g.setColor(Color.white);
        else
            g.setColor(getBackground());

        g.fillRect(0, 0, d.width, d.height);

        // recompute Delaunay/Voronoi
        if ((voro.delaunayB || voro.voronoiB) && nodesMovedB) {
//            nLines = 0;
            voro.setSize(getSize());
            voro.computeVoronoi();// TODO: analyze
        }
        nodesMovedB = false;
        //
        // draw probability distribution .....
        // changes need to be reflected in the distribution itself
        //

        // Set color for distribution
        if (!algorithm.isDiscrete())
            g.setColor(distribColor);

        if (probDistB) drawPD(g, d);

        final int gridWidth     = compute.gridWidth;
        final int gridHeight    = compute.gridHeight;

        // Draw the edges
        if (edgesB) {
            int x1, y1, x2, y2;
            EdgeGNG e;
            final int nEdges = compute.nEdges;
            final EdgeGNG[] edges = compute.edges;
            final NodeGNG[] nodes = compute.nodes;
            for (i = 0 ; i < nEdges ; i++) {
                e = edges[i];
                if (mapSpaceGGB && (algorithm == Algorithm.GG|| algorithm == Algorithm.GR) || mapSpaceSOMB && algorithm == Algorithm.SOM) {
                    x1 = gx2x(nodes[e.from].x_grid);
                    y1 = gy2y(nodes[e.from].y_grid);
                    x2 = gx2x(nodes[e.to].x_grid);
                    y2 = gy2y(nodes[e.to].y_grid);
                } else {
                    x1 = (int)nodes[e.from].x;
                    y1 = (int)nodes[e.from].y;
                    x2 = (int)nodes[e.to].x;
                    y2 = (int)nodes[e.to].y;
                }
                g.setColor(edgeColor);
                g.drawLine(x1, y1, x2, y2);
            }
        } else if (algorithm.isSOMType()) {
            g.setColor(edgeColor);
            // draw the outer edges, i.e. where for *both* endpoints holds:
            // gridx=0 or gridy=0 or gridx = width-1 or grid y= height-1
            final GridNodeGNG[][] grid = compute.grid;
            for (i = 0; i < gridWidth-1; i++) {
                NodeGNG n1 = grid[i][0].node;
                NodeGNG n2 = grid[i+1][0].node;
                g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
                n1 = grid[i][gridHeight-1].node;
                n2 = grid[i+1][gridHeight-1].node;
                g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
            }
            for (i = 0; i < gridHeight-1; i++) {
                NodeGNG n1 = grid[0][i].node;
                NodeGNG n2 = grid[0][i+1].node;
                g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
                n1 = grid[gridWidth-1][i].node;
                n2 = grid[gridWidth-1][i+1].node;
                g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
            }
        }

        // draw the filled polygons of fixed-dimensional networks (SOM, GG. eventually GCS)
        if (algorithm.isSOMType()){
            Color c = somColor;
            Color cT = new Color(c.getRed(),c.getGreen(),c.getBlue(),80);
            //Color dd = Color.black;
            //Color dT = new Color(dd.getRed(),dd.getGreen(),dd.getBlue(),80);
            int j;
            int xPoints[] = new int[5];
            int yPoints[] = new int[5];
            // draw the rectangles (as closed polygons)
            if (gridHeight>1){
                for (i = 0; i < gridWidth-1; i++) {
                    for (j = 0; j < gridHeight-1; j++) {
                        // draw polygon i,j;i+1,j;i+1,j+1;i,j+1;i,j
                        if (mapSpaceGGB && (algorithm == Algorithm.GG|| algorithm == Algorithm.GR) || mapSpaceSOMB && algorithm == Algorithm.SOM) {
                            xPoints[0]=gx2x(i);
                            xPoints[1]=gx2x(i+1);
                            xPoints[2]=gx2x(i+1);
                            xPoints[3]=gx2x(i);
                            xPoints[4]=gx2x(i);

                            yPoints[0]=gy2y(j);
                            yPoints[1]=gy2y(j);
                            yPoints[2]=gy2y(j+1);
                            yPoints[3]=gy2y(j+1);
                            yPoints[4]=gy2y(j);

                        } else {
                            final GridNodeGNG[][] grid = compute.grid;
                            xPoints[0]=(int)grid[i][j].node.x;
                            xPoints[1]=(int)grid[i+1][j].node.x;
                            xPoints[2]=(int)grid[i+1][j+1].node.x;
                            xPoints[3]=(int)grid[i][j+1].node.x;
                            xPoints[4]=(int)grid[i][j].node.x;

                            yPoints[0]=(int)grid[i][j].node.y;
                            yPoints[1]=(int)grid[i+1][j].node.y;
                            yPoints[2]=(int)grid[i+1][j+1].node.y;
                            yPoints[3]=(int)grid[i][j+1].node.y;
                            yPoints[4]=(int)grid[i][j].node.y;
                        }
                        g.setColor(cT);
                        g.fillPolygon(xPoints,yPoints,5);
                        //g.setColor(dT);
                        //g.drawPolygon(xPoints,yPoints,5);
                    }
                }
            } else if ((algorithm == Algorithm.SOM && compute.torusSOMB) || ((algorithm == Algorithm.GG|| algorithm == Algorithm.GR) && compute.torusGGB)){
                c = torusColor;
                cT = new Color(c.getRed(),c.getGreen(),c.getBlue(),80);
                g.setColor(cT);
                Polygon p = new Polygon();
                final GridNodeGNG[][] grid = compute.grid;
                for (i = 0; i < gridWidth-1; i++) {
                    p.addPoint((int)grid[i][0].node.x,(int)grid[i][0].node.y);
                }
                g.fillPolygon(p);
            }
        }

        // Draw the Voronoi or Delaunay diagram
        if (voro.voronoiB || voro.delaunayB) {
            LineFloat2D l;
            final Line2D ln = new Line2D.Float();
            final int nLines = voro.nLines;
            for (i = 0; i < nLines; i++) {
                l = voro.lines[i];
                if (voro.vd[i])
                    // voronoi
                    g.setColor(voronoiColor);
                else
                    // delaunay
                    g.setColor(delaunayColor);
                ln.setLine(l.x1, l.y1, l.x2, l.y2);
                g.draw(ln); // drawLine(l.x1, l.y1, l.x2, l.y2);
            }
        }

        // Draw the nodes
        if (nodesB) {
            final int nNodes = compute.nNodes;
            final NodeGNG[] nodes = compute.nodes;
            for (i = 0; i < nNodes; i++)
                paintNode(g, nodes[i]);
        }

        // draw the tau values
        if ((algorithm == Algorithm.GG|| algorithm == Algorithm.GR) && tauB){
            g.setColor(Color.black);
            int j;
            final GridNodeGNG[][] grid = compute.grid;
            for (i = 0; i < gridWidth; i++) {
                for (j = 0; j < gridHeight; j++) {
                    GridNodeGNG xx = grid[i][j];
                    if (i==0 && j==0){
                        if (mapSpaceGGB) {
                            g.drawString("x"+String.valueOf((int)xx.node.tau)+"x", gx2x(xx.node.x_grid), gy2y(xx.node.y_grid));

                        } else {
                            g.drawString("x"+String.valueOf((int)xx.node.tau)+"x", (int)xx.node.x, (int)xx.node.y);
                        }
                    } else {
                        if (mapSpaceGGB) {
                            g.drawString(String.valueOf((int)xx.node.tau),  gx2x(xx.node.x_grid), gy2y(xx.node.y_grid));
                        } else {
                            g.drawString(String.valueOf((int)xx.node.tau), (int)xx.node.x, (int)xx.node.y);

                        }
                    }
                }
            }

        }

        // Draw the motion traces
        if (tracesB && !algorithm.isLBGType()){ // traces not working for LBG for some reason
            g.setColor(Color.black);
            final int nNodes = compute.nNodes;
            final NodeGNG[] nodes = compute.nodes;
            for (i = 0; i < nNodes; i++) {
                Vector<Float> tr = nodes[i].getTrace();
                if (tr.size()<4)
                    break;
                int x1,y1,x2,y2;
                x1=(int) Math.round(tr.get(0));
                y1=(int) Math.round(tr.get(1));
                for (int j=2;j<tr.size();j+=2){
                    x2=(int) Math.round(tr.get(j));
                    y2=(int) Math.round(tr.get(j+1));
                    g.drawLine(x1, y1, x2, y2);
                    x1=x2;
                    y1=y2;
                }
            }
        }


        //Draw teach mode info
        if ( teachB ) {
            int r = 6;
            int offset_x = 12;
            int offset2_x = offset_x + 5;
            int offset_y = d.height/4;

            if (algorithm.isDiscrete()) {
                // Draw legend
                g.setColor(Color.black);
                g.drawString("Legend:", 	2, offset_y); offset_y += 15;

                g.setColor(movedColor);
                g.fillOval(offset_x - r, offset_y - r, r, r);
                g.setColor(Color.black);
                g.drawString("Not moved", offset2_x,
                        offset_y); offset_y += 15;
            } else {
                g.setColor(signalColor);
                g.fillOval((int) compute.SignalX - r/2, (int) compute.SignalY - r/2, r, r);

                // Draw legend
                g.setColor(Color.black);
                g.drawString("Legend:", 	2, offset_y); offset_y += 15;

                g.setColor(winnerColor);
                g.fillOval(offset_x - r, offset_y - r, r, r);
                g.setColor(Color.black);
                g.drawString("Winner", offset2_x, offset_y); offset_y += 15;

                if (algorithm != Algorithm.HCL) {
                    g.setColor(secondColor);
                    g.fillOval(offset_x - r, offset_y - r, r, r);
                    g.setColor(Color.black);
                    g.drawString("Second", offset2_x, offset_y); offset_y += 15;
                }

                if (algorithm == Algorithm.GNG) {
                    g.setColor(movedColor);
                    g.fillOval(offset_x - r, offset_y - r, r, r);
                    g.setColor(Color.black);
                    g.drawString("Neighbors", offset2_x, offset_y); offset_y += 15;

                    g.setColor(insertedColor);
                    g.fillOval(offset_x - r, offset_y - r, r, r);
                    g.setColor(Color.black);
                    g.drawString("Last inserted", offset2_x, offset_y); offset_y += 15;
                }

                g.setColor(signalColor);
                g.fillOval(offset_x - r, offset_y - r, r, r);
                g.setColor(Color.black);
                g.drawString("Signal", offset2_x, offset_y); offset_y += 15;
            }
        }

        g.setColor(Color.black);
        g.drawString("Signals: "+String.valueOf(compute.numSignals), 10, 10);
        if (compute.maxNodes == 1)
            g.drawString(String.valueOf(compute.nNodes) + " node",
                    10, d.height - 10);
        else
            g.drawString("Nodes: "+String.valueOf(compute.nNodes),
                    10, d.height - 10);

        g.drawString("DemoGNG "+ DEMO_GNG_VERSION, d.width - 130, 10);
        if ( compute.readyLBG_B && (algorithm.isLBGType()) ) {
            g.drawString("READY!", d.width-50, d.height-10);
        }
        if ( compute.fineTuningB && (algorithm == Algorithm.GG|| algorithm == Algorithm.GR) )
            g.drawString(compute.fineTuningS, d.width-130, d.height-10);

        //
        // draw numSignals
        //
        if ( signalsB && (!algorithm.isDiscrete()) ) {
            final float[] lastSignalsX = compute.lastSignalsX;
            final float[] lastSignalsY = compute.lastSignalsY;
            final int stepSize = compute.stepSize;
            for (i = 0; i < stepSize; i++) {
                x = (int) (lastSignalsX[i]);
                y = (int) (lastSignalsY[i]);

                //g.setColor(Color.green);
                //g.fillOval(x - 1, y - 1, 2, 2);
                g.setColor(signalsColor);
                g.fillOval(x - 2, y - 2, 3, 3);
                g.drawOval(x - 2, y - 2, 3, 3);
            }
        }

        if (algorithm.isDiscrete()) {
            final int numDiscreteSignals    = compute.numDiscreteSignals;
            final float[] discreteSignalsX  = compute.discreteSignalsX;
            final float[] discreteSignalsY  = compute.discreteSignalsY;
            for (i = 0; i < numDiscreteSignals; i++) {
                x = Math.round(discreteSignalsX[i]);
                y = Math.round(discreteSignalsY[i]);

                g.setColor(distribColor);
                g.fillOval(x - 1, y - 1, 2, 2);
                g.setColor(Color.black);
                g.drawOval(x - 1, y - 1, 2, 2);
            }
        }

        // Show error graph or not
        if (errorGraph != null) {
            if (errorGraphB)
                errorGraph.setVisible(true);
            else
                errorGraph.setVisible(false);
        }

    }

    @Override
    public void mousePressed(MouseEvent evt) {
        int x = evt.getX();
        int y = evt.getY();
        if ((evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
            if (compute.pd == PD.RightMouseB)  {
                Dimension d = getSize();

                compute.jumpX = x;
                compute.jumpY = y;
                // Draw distribution only inside the visible region
                if (compute.jumpX > (0.75 * d.width))
                    compute.jumpX = (int) (0.75 * d.width);

                if (compute.jumpY > (0.75 * d.height))
                    compute.jumpY = (int) (0.75 * d.height);

                repaint();
                return;// true;
            } else return;
        } else if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {

            float bestDist = Float.MAX_VALUE;
            NodeGNG n;
            float dist;

            final int nNodes = compute.nNodes;
            final NodeGNG[] nodes = compute.nodes;
            for (int i = 0 ; i < nNodes ; i++) {
                n = nodes[i];
                dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
                if (dist <= bestDist) {
                    pick = n;
                    bestDist = dist;
                }
            }
            pickFixed = pick.isMouseSelected;
            pick.isMouseSelected = true;
            pick.x = x;
            pick.y = y;

            if (compute.algorithm.isDiscrete())
                pick.hasMoved = true;

            nodesMovedB = true;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        int x = evt.getX();
        int y = evt.getY();
        Dimension d = getSize();

        if ((evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
            if (compute.pd == PD.RightMouseB)  {
                compute.jumpX = x;
                compute.jumpY = y;

                // Draw distribution only inside the visible region
                if (compute.jumpX < 0)
                    compute.jumpX = 0;
                else if (compute.jumpX > (0.75 * d.width))
                    compute.jumpX = (int) (0.75 * d.width);

                if (compute.jumpY < 0)
                    compute.jumpY = 0;
                else if (compute.jumpY > (0.75 * d.height))
                    compute.jumpY = (int) (0.75 * d.height);

                repaint();
            }
        } else if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {

            pick.x = x;
            pick.y = y;

            // Draw nodes only inside the visible region
            if (pick.x < 0)
                pick.x = 0;
            else if (pick.x > d.width)
                pick.x = d.width;

            if (pick.y < 0)
                pick.y = 0;
            else if (pick.y > d.height)
                pick.y = d.height;

            nodesMovedB = true;
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        Dimension d = getSize();
        int x = evt.getX();
        int y = evt.getY();

        if ((evt.getButton()==java.awt.event.MouseEvent.BUTTON3) && (compute.pd == PD.RightMouseB) ) {
            compute.jumpX = x;
            compute.jumpY = y;

            // Draw distribution only inside the visible region
            if (compute.jumpX < 0)
                compute.jumpX = 0;
            else if (compute.jumpX > (0.75 * d.width))
                compute.jumpX = (int) (0.75 * d.width);

            if (compute.jumpY < 0)
                compute.jumpY = 0;
            else if (compute.jumpY > (0.75 * d.height))
                compute.jumpY = (int) (0.75 * d.height);

            repaint();
        } else if (evt.getButton()==java.awt.event.MouseEvent.BUTTON1) {

            pick.x = x;
            pick.y = y;
            pick.isMouseSelected = pickFixed;

            // Draw nodes only inside the visible region
            if (pick.x < 0)
                pick.x = 0;
            else if (pick.x > d.width)
                pick.x = d.width;

            if (pick.y < 0)
                pick.y = 0;
            else if (pick.y > d.height)
                pick.y = d.height;

            pick = null;

            nodesMovedB = true;
            repaint();
        }
    }

    public void start() {
        result.stop = false;
        log("start() ......");
        relaxer = new Thread(this);
        relaxer.start();

        if ( errorGraphB  && (errorGraph != null) )
            errorGraph.setVisible(true);
    }

    public void stop(){
        log("stop() ......");
        result.stop = true;
        if (relaxer != null) {
            relaxer.interrupt(); //!!!!
            relaxer = null;
            log("set relaxer to 0");
        }
        if ( errorGraphB  && (errorGraph != null) )
            errorGraph.setVisible(false);
    }

    public void destroy() {
        if ( errorGraphB  && (errorGraph != null) ) {
            errorGraph.dispose();
            errorGraph = null;
        }
    }

    public void graphClose() {
        if ( errorGraphB  && (errorGraph != null) ) {
            errorGraph.dispose();
            errorGraph = null;
        }
    }
    //	@Override
//	public void mouseDragged(MouseEvent e) {
//		// TODO Auto-generated method stub
//		//log("mouseDragged");
//
//	}
    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        //log("mouseMoved");

    }

    // mouse listener
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    //	@Override
//	public void mousePressed(MouseEvent e) {
//		// TODO Auto-generated method stub
//		log("mouseMoved");
//		log(e.toString());
//
//	}
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		// TODO Auto-generated method stub
//
//	}
    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void componentResized(ComponentEvent e) {
        // TODO Auto-generated method stub
        log("....................... resized!!!!!!!! ..............");
        nodesMovedB=true;
        //wasResized();
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
        log("......................... SH O W N ..................");

    }
    @Override
    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void stateChanged(ChangeEvent e) {
        // TODO Auto-generated method stub
        JSlider source = (JSlider)e.getSource();
        if (true/*!source.getValueIsAdjusting()*/) {
            delay = (50-(int)source.getValue())*10;
        }
    }
}
