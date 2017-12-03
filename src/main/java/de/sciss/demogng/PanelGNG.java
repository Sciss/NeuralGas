package de.sciss.demogng;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

@SuppressWarnings("serial")
class PanelGNG extends JPanel implements
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
     * The flag for debugging.
     */
    protected final boolean DEBUG = false;
    /**
     * The maximum number of elements to draw/calculate for the distributions.
     */
    protected final int MAX_COMPLEX = 58;
    /**
     * The maximum number of nodes.
     */
    protected final int MAX_NODES = 30000;
    /**
     * The maximum number of edges (3 * maximum number of nodes).
     */
    protected final int MAX_EDGES = 6 * MAX_NODES;
    /**
     * The maximum number of Voronoi lines (5 * maximum number of nodes).
     */
    protected final int MAX_V_LINES = 6 * MAX_NODES;
    /**
     * The maximum stepsize.
     */
    protected final int MAX_STEPSIZE = 500;
    /**
     * The size of the DiscreteMixture signal set.
     */
    protected final int MIXTURE_SIZE = 500;
    /**
     * The maximum number of discrete signals.
     */
    protected final int MAX_DISCRETE_SIGNALS = 20000;
    /**
     * The maximum x size of the grid array.
     */
    protected final int MAX_GRID_X = 10000;
    /**
     * The maximum y size of the grid array.
     */
    protected final int MAX_GRID_Y = 100;

    /**
     * The factor for the ring-thickness (distribution).
     */
    protected final float RING_FACTOR = 0.4f;	// Factor < 1
    /**
     * The version of the Growing Neural Gas Demo.
     */
    protected static final String DGNG_VERSION = "v2.2.0-SNAPSHOT"; // Version
    /**
     * The current maximum number of nodes.
     */
    protected int maxNodes = 100;
    /**
     * The current number of runs to insert a new node (GNG).
     */
    protected int lambdaGNG = 600;
    /**
     * The current number of input signals used for adaptation.
     */
    protected int sigs = 0;

    /**
     * The temporal backup of a run.
     */
    protected int sigsTmp = 0;
    /**
     * The x-position of the actual signal.
     */
    protected float SignalX = 0f;
    /**
     * The y-position of the actual signal.
     */
    protected float SignalY = 0f;
    /**
     * The initial width of the drawing area.
     * This value can only be changed by resizing the appletviewer.
     */
    protected int panelWidth = 550;
    /**
     * The initial height of the drawing area.
     * This value can only be changed by resizing the appletviewer.
     */
    protected int panelHeight = 310;

    protected DemoGNG graph;
    /**
     * The actual number of nodes.
     */
    protected int nNodes = 0;
    /**
     * The array of the actual used nodes.
     */
    protected NodeGNG nodes[] = new NodeGNG[MAX_NODES];
    /**
     * The sorted array of indices of nodes.
     * The indices of the nodes are sorted by their distance from the actual
     * signal. sNodes[1] is the index of the nearest node.
     */
    protected int sNodes[] = new int[MAX_NODES + 1];
    /**
     * The array of the nodes in the grid.
     */
    protected GridNodeGNG grid[][] = new GridNodeGNG[MAX_GRID_X][MAX_GRID_Y];
    /**
     * The array of the last computed signals (x-coordinate).
     */
    protected float lastSignalsX[] = new float[MAX_STEPSIZE];
    /**
     * The array of the last computed signals (y-coordinate).
     */
    protected float lastSignalsY[] = new float[MAX_STEPSIZE];
    /**
     * The array of the discrete signals (x-coordinate).
     */
    protected float discreteSignalsX[] = new float[MAX_DISCRETE_SIGNALS];
    /**
     * The array of the discrete signals (y-coordinate).
     */
    protected float discreteSignalsY[] = new float[MAX_DISCRETE_SIGNALS];
    /**
     * The array of the best distance (discrete signals).
     */
    protected float discreteSignalsD1[] = new float[MAX_DISCRETE_SIGNALS];
    /**
     * The array of the second best distance (discrete signals).
     */
    protected float discreteSignalsD2[] = new float[MAX_DISCRETE_SIGNALS];
    /**
     * The array of the second best distance (discrete signals).
     */
    protected FPoint Cbest[] = new FPoint[MAX_NODES];

    /**
     * The current number of discrete signals.
     */
    protected int numDiscreteSignals = 500;
    /**
     * The actual number of edges.
     */
    protected int nEdges = 0;
    /**
     * The array of the actual used edges.
     */
    protected EdgeGNG edges[] = new EdgeGNG[MAX_EDGES];
    /**
     * The actual number of Voronoi lines.
     */
    protected int nlines = 0;
    /**
     * The array of the actual used lines.
     */
    protected LineGNG lines[] = new LineGNG[MAX_V_LINES];
    /**
     * The array of boolean to distinguish between Voronoi and Delaunay lines.
     */
    protected boolean vd[] = new boolean[MAX_V_LINES];

    Thread relaxer;
    GraphGNG errorGraph;
    ComputeGNG compute;
    ComputeGNG.Result result;

    /**
     * The flag for playing the sound for a new inserted node.
     */
    protected boolean insertedSoundB = false;
    /**
     * The flag for a white background. Useful for making hardcopies
     */
    protected boolean whiteB = false;
    /**
     * The flag for random init. The nodes will be placed only in the specified
     *  distribution or not.
     */
    protected boolean rndInitB = false;
    /**
     * The flag for entering the fine-tuning phase (GG).
     */
    protected boolean fineTuningB = false;
    /**
     * The flag for showing the signal.
     *  This variable can be set by the user and shows the last input signals.
     */
    protected boolean signalsB = false;
    /**
     * stop the algo when max number of nodes is reached
     */
    protected boolean autoStopB = true;

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
     * close GG to a torus
     */
    protected boolean torusGGB = false;
    /**
     * close SOM to a torus
     */
    protected boolean torusSOMB = false;
    /**
     * The flag for displaying usage
     */
    protected boolean usageB = false;
    /**
     * The flag for inserting new nodes in GNG.
     *  This variable can be set by the user. If true no new nodes are
     *  inserted.
     */
    protected boolean noNewNodesGNGB = false;
    /**
     * The flag for inserting new nodes in GG.
     *  This variable can be set by the user. If true no new nodes are
     *  inserted.
     */
    protected boolean noNewNodesGGB = false;
    /**
     * The flag for stopping the demo.
     *  This variable can be set by the user. If true no calculation is done.
     */
    private boolean stopB = false;
    /**
     * The flag for the sound.
     *  This variable can be set by the user. If false no sound is played.
     */
    protected boolean soundB = false;
    /**
     * The flag for the teach-mode.
     *  This variable can be set by the user. If true a legend is displayed
     *  which describes the new form and color of some nodes. Furthermore
     *  all calculation is very slow.
     */
    protected boolean teachB = false;
    /**
     * The flag for variable movement (HCL).
     *  This variable can be set by the user.
     */
    protected boolean variableB = false;
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
     * The flag for displaying the Voronoi diagram.
     *  This variable can be set by the user.
     */
    protected boolean voronoiB = false;
    /**
     * The flag for displaying the Delaunay triangulation.
     *  This variable can be set by the user.
     */
    protected boolean delaunayB = false;
    /**
     * The flag for any moved nodes (to compute the Voronoi diagram/Delaunay
     *  triangulation).
     */
    protected boolean nodesMovedB = true;
    /**
     * The flag for using utility (GNG-U).
     */
    protected boolean GNG_U_B = false;
    /**
     * The flag for changed number of nodes.
     */
    protected boolean nNodesChangedB = true;

    /**
     * The flag for LBG-U method
     */
    protected boolean LBG_U_B = false;
    /**
     * The flag for end of calculation (LBG)
     */
    protected boolean readyLBG_B = false;

    /**
     * The current maximum number to delete an old edge (GNG,NGwCHL).
     *  This variable can be set by the user.
     */
    protected int MAX_EDGE_AGE = 88;
    /**
     * The current number of calculations done in one step.
     *  This variable can be set by the user. After <TT> stepSize </TT>
     *  calculations the result is displayed.
     */
    protected int stepSize = 50;
    /**
     * This variable determines how long the compute thread sleeps. In this time
     *  the user can interact with the program. Slow machines and/or slow
     *  WWW-browsers need more time than fast machines and/or browsers.
     *  This variable can be set by the user.
     */
    protected int tSleep = 10; //should be taken from the speed choice

    /**
     * This value is displayed in the error graph.
     */
    protected float valueGraph = 0.0f;
    /**
     * This value contains the best error value for LBG-U up to now.
     */
    protected float errorBestLBG_U = Float.MAX_VALUE;
    /**
     * The string shown in the fine-tuning phase of the method GG.
     */
    protected String fineTuningS = "";

    /**
     * The actual number of Voronoi lines.
     */
    protected int nLines = 0;

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
        this.voro       = new Voronoi(this);
        this.compute    = graph.compute;
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
            //graph.prepareAlgo(algo);
        }
        while (true) {

            // Relativate Positions
            Dimension d = getSize();
            if ( (d.width != panelWidth) || (d.height != panelHeight) ) {
                //
                // panel size has changed! ==> rescaling needed
                //
                nodesMovedB = true; // for Voronoi
                NodeGNG n;
                for (i = 0 ; i < nNodes ; i++) {
                    n = nodes[i];

                    n.x = n.x * d.width / panelWidth;
                    n.y = n.y * d.height / panelHeight;
                }

                if (compute.pd == PD.DiscreteMixture || compute.algo.isDiscrete()){
                    compute.rescaleDiscreteSignals(1.0*d.width / panelWidth, 1.0*d.height / panelHeight);
                }
                panelWidth = d.width;
                panelHeight = d.height;
                //initDiscreteSignals(pd);
                if ( ( nNodes == 0) && (compute.algo.isDiscrete()) ) {
                    // can this happen???
                    // Generate some nodes
                    int z = (int) (numDiscreteSignals * Math.random());
                    int mod = 0;
                    for (i = 0; i < maxNodes; i++) {
                        mod = (z+i)%numDiscreteSignals;
                        compute.addNode(Math.round(discreteSignalsX[mod]),
                                Math.round(discreteSignalsY[mod]));
                    }
                }
                repaint();
            }

            // Calculate the new positions
            if (!stopB) {
                compute.learn(result);
                nodesMovedB = true;
            }

            // update error graph
            if (errorGraphB && !stopB)
                errorGraph.graph.add(valueGraph);

            if (stopB)
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
     * The color of input signals
     */
    protected final Color signalsColor = Color.red;
    /**
     * The color of the 1-D SOM torus.
     */
    protected final Color torusColor = Color.cyan;
    /**
     * The color of SOM polgons.
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
        final Algo algo = compute.algo;

        if (teachB && (!algo.isDiscrete()) ) {
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

        if (algo.isSOMType() && usageB){
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

        if ( (algo.isDiscrete()) && (!n.hasMoved) ) {
            RADIUS += 4;
            col = movedColor;
        }

        g.setColor(col);
        if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
            g.fillOval((int)(gx2x((int)n.x_grid) - (RADIUS/2)), (int)(gy2y((int)n.y_grid) - (RADIUS/2)), RADIUS, RADIUS);
        } else {
            g.fillOval((int)n.x - (RADIUS/2), (int)n.y - (RADIUS/2), RADIUS, RADIUS);
        }
        g.setColor(Color.black);
        if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
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
    static int paintCounter =0;
    static int prevSigs=0;

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
        int ll,lr,r2,l2;
        int xA[] = new int[MAX_COMPLEX];
        int yA[] = new int[MAX_COMPLEX];
        int w;
        int h;
        int mindim;
        int ringRadius;
        int i, x, y;
        mindim = (d.width < d.height) ? d.width : d.height;

        switch (compute.pd) {
            case Rectangle: // Rectangle
                ll = d.width/20;
                lr = d.height/20;
                r2 = d.width*9/10;
                l2 = d.height*9/10;
                g.fillRect(ll, lr, r2, l2);
                break;
            case Circle: // Circle

                l2 = mindim*9/10; // Diameter is proportional to the smallest panel dimension

                ll = d.width/2 -l2/2;
                lr = d.height/2 -l2/2;

                g.fillOval(ll, lr, l2, l2);
                break;
            case TwoCircles: // Circle
                // circle space circle (3x1)
                if (d.width/3 < d.height){
                    // limiting dimension is width
                    l2=(int) (d.width/2.6);
                } else {
                    // limiting dimension is height
                    l2=(int) (d.height*0.95);
                }

                ll = d.width/2 -l2*5/4;
                lr = d.height/2 -l2/2;
                g.fillOval(ll, lr, l2, l2);

                ll = d.width/2 +l2/4;
                g.fillOval(ll, lr, l2, l2);

                break;
            case Ring: // Ring
                int cx = d.width/2; // horizontal center of panel
                int cy = d.height/2;// vertical center of panel
                l2 = (cx < cy) ? cx : cy; // Diameter

                ll = cx - l2;
                lr = cy - l2;
                ringRadius = (int) (l2 * RING_FACTOR);

                g.fillOval(ll, lr, 2*l2, 2*l2);
                if (whiteB)
                    g.setColor(Color.white);
                else
                    g.setColor(getBackground());
                g.fillOval(ll + ringRadius,
                        lr+ringRadius,
                        2*l2-2*ringRadius,
                        2*l2-2*ringRadius);
                break;
            case UNI: // Complex (1)
                w = d.width/9;
                h = d.height/5;
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

                for (i = 0; i < 14; i++)
                    g.fillRect(xA[i], yA[i], w, h);
                break;
            case SmallSpirals: // Complex (2)
                w = d.width/9;
                h = d.height/7;
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

                for (i = 0; i < 22; i++)
                    g.fillRect(xA[i], yA[i], w, h);
                break;
            case LargeSpirals: // Complex (3)
                w = d.width/13;
                h = d.height/11;
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

                for (i = 0; i < 58; i++)
                    g.fillRect(xA[i], yA[i], w, h);
                break;

            case HiLoDensity: // HiLo-Density
                w = d.width/10;
                h = d.height/10;
                xA[0] = 2 * w;
                yA[0] = 4 * h;
                xA[1] = 5 * w;
                yA[1] = 1 * h;

                final Algo algo = compute.algo;
                if (!algo.isDiscrete())
                    g.setColor(highDistribColor);
                g.fillRect(xA[0], yA[0], w, h);
                if (!algo.isDiscrete())
                    g.setColor(lowDistribColor);
                g.fillRect(xA[1], yA[1], 4 * w, 8 * h);
                break;

            case DiscreteMixture: // discrete
                //int RADIUS = 2;
                for (i = 0; i < numDiscreteSignals; i++) {
                    x = Math.round(discreteSignalsX[i]);
                    y = Math.round(discreteSignalsY[i]);

                    g.setColor(distribColor);
                    g.fillOval(x - 1, y - 1, 2, 2);
                    g.setColor(Color.black);
                    g.drawOval(x - 1, y - 1, 2, 2);
                }
                break;

            case UNIT: // Complex (4)
                w = d.width/17;
                h = d.height/8;
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

                for (i = 0; i < 28; i++)
                    g.fillRect(xA[i], yA[i], w, h);
                break;
            case MoveJump: // Moving and Jumping Rectangle
                r2 = d.width/4;
                l2 = d.height/4;
                ll = (int) (0.75 * (d.width/2 +
                        Math.IEEEremainder(0.2 * sigs,(d.width))));
                lr = (int) (0.75 * (d.height/2 +
                        Math.IEEEremainder(0.2 * sigs,(d.height))));

                g.fillRect(ll, lr, r2, l2);
                break;
            case Move: // Moving Rectangle
                r2 = d.width/4;
                l2 = d.height/4;
                ll = (int) (0.75 * (d.width/2 +
                        compute.bounceX * Math.IEEEremainder(0.2 * sigs,
                                (d.width))));
                lr = (int) (0.75 * (d.height/2 +
                        compute.bounceY * Math.IEEEremainder(0.2 * sigs,
                                (d.height))));

                g.fillRect(ll, lr, r2, l2);
                break;

            case Jump: // Jumping Rectangle
                r2 = d.width/4;
                l2 = d.height/4;

                g.fillRect(compute.jumpX, compute.jumpY, r2, l2);
                break;

            case RightMouseB: // R.Mouse Rectangle
                r2 = d.width/4;
                l2 = d.height/4;

                g.fillRect(compute.jumpX, compute.jumpY, r2, l2);
                break;
        }
    }

    public synchronized void paintComponent(Graphics g) {

        //log("paintComponent() CGNG " + String.valueOf(paintCounter)+" signals:"+String.valueOf(sigs) + "delta-sig:"+String.valueOf(sigs - prevSigs));
        paintCounter +=1;
        prevSigs = sigs;
        Dimension d = getSize();
        int i, x, y;
        final Algo algo = compute.algo;

        if (whiteB)
            g.setColor(Color.white);
        else
            g.setColor(getBackground());

        g.fillRect(0, 0, d.width, d.height);

        // recompute Delaunay/Voronoi
        if ((delaunayB || voronoiB) && nodesMovedB) {
            nlines = 0;
            voro.computeVoronoi();// TODO: analyze
        }
        nodesMovedB = false;
        //
        // draw probability distribution .....
        // changes need to be reflected in the distribution itself
        //

        // Set color for distribution
        if (!algo.isDiscrete())
            g.setColor(distribColor);

        if (probDistB) drawPD(g, d);

        final int gridWidth     = compute.gridWidth;
        final int gridHeight    = compute.gridHeight;

        // Draw the edges
        if (edgesB) {
            int x1, y1, x2, y2;
            EdgeGNG e;
            for (i = 0 ; i < nEdges ; i++) {
                e = edges[i];
                if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
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
        } else if (algo.isSOMType()) {
            g.setColor(edgeColor);
            // draw the outer edges, i.e. where for *both* endpoints holds:
            // gridx=0 or gridy=0 or gridx = width-1 or grid y= height-1
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
        if (algo.isSOMType()){
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
                        if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
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
            } else if ((algo==Algo.SOM && torusSOMB) || ((algo==Algo.GG||algo==Algo.GR) && torusGGB)){
                c = torusColor;
                cT = new Color(c.getRed(),c.getGreen(),c.getBlue(),80);
                g.setColor(cT);
                Polygon p = new Polygon();
                for (i = 0; i < gridWidth-1; i++) {
                    p.addPoint((int)grid[i][0].node.x,(int)grid[i][0].node.y);
                }
                g.fillPolygon(p);
            }
        }

        // Draw the Voronoi or Delaunay diagram
        if (voronoiB || delaunayB) {
            LineGNG l;
            for (i = 0; i < nlines; i++) {
                l = lines[i];
                if (vd[i])
                    // voronoi
                    g.setColor(voronoiColor);
                else
                    // delaunay
                    g.setColor(delaunayColor);
                g.drawLine(l.x1, l.y1, l.x2, l.y2);
            }
        }

        // Draw the nodes
        if (nodesB)
            for (i = 0; i < nNodes; i++)
                paintNode(g, nodes[i]);

        // draw the tau values
        if ((algo==Algo.GG||algo==Algo.GR) && tauB){
            g.setColor(Color.black);
            int j;
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
        if (tracesB && !algo.isLBGType()){ // traces not working for LBG for some reason
            g.setColor(Color.black);
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

            if (algo.isDiscrete()) {
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
                g.fillOval((int) SignalX - r/2, (int) SignalY - r/2, r, r);

                // Draw legend
                g.setColor(Color.black);
                g.drawString("Legend:", 	2, offset_y); offset_y += 15;

                g.setColor(winnerColor);
                g.fillOval(offset_x - r, offset_y - r, r, r);
                g.setColor(Color.black);
                g.drawString("Winner", offset2_x, offset_y); offset_y += 15;

                if (algo != Algo.HCL) {
                    g.setColor(secondColor);
                    g.fillOval(offset_x - r, offset_y - r, r, r);
                    g.setColor(Color.black);
                    g.drawString("Second", offset2_x, offset_y); offset_y += 15;
                }

                if (algo == Algo.GNG) {
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
        g.drawString("Signals: "+String.valueOf(sigs), 10, 10);
        if (maxNodes == 1)
            g.drawString(String.valueOf(nNodes) + " node",
                    10, d.height - 10);
        else
            g.drawString("Nodes: "+String.valueOf(nNodes),
                    10, d.height - 10);

        g.drawString("DemoGNG "+DGNG_VERSION, d.width - 130, 10);
        if ( readyLBG_B && (algo.isLBGType()) ) {
            g.drawString("READY!", d.width-50, d.height-10);
        }
        if ( fineTuningB && (algo==Algo.GG||algo==Algo.GR) )
            g.drawString(fineTuningS, d.width-130, d.height-10);

        //
        // draw signals
        //
        if ( signalsB && (!algo.isDiscrete()) ) {
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


        //
        // play insert sound
        //
        if ( insertedSoundB && soundB ) {
            //log("beep!!!");
            //log(graph.getCodeBase().toString());
            graph.play(graph.getCodeBase(), "audio/drip.au");
            insertedSoundB = false;
        }

        if (algo.isDiscrete()) {
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

            if (compute.algo.isDiscrete())
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
        stopB = false;
        log("start() ......");
        relaxer = new Thread(this);
        relaxer.start();

        if ( errorGraphB  && (errorGraph != null) )
            errorGraph.setVisible(true);
    }

    public void stop(){
        log("stop() ......");
        stopB =true;
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
