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

import java.awt.Dimension;

import static de.sciss.neuralgas.ComputeGNG.MAX_NODES;

/**
 * Compute Voronoi diagram.
 * A sweep-line algorithm is implemented (Steven Fortune, 1987).
 * It computes the Voronoi diagram/Delaunay triangulation of n sites
 * in time O(n log n) and space usage O(n).
 * Input: nodes[], Output: lines[] (global).
 * 
 */
public class Voronoi {
//    PanelGNG cGNG;
    ComputeGNG compute;

    /**
     * The maximum number of Voronoi lines (5 * maximum number of nodes).
     */
    public final int MAX_V_LINES = 6 * MAX_NODES;

    /**
     * The actual number of Voronoi lines.
     */
    public int nLines = 0;
    /**
     * The array of the actual used lines.
     */
    public LineFloat2D lines[] = new LineFloat2D[MAX_V_LINES];
    /**
     * The array of boolean to distinguish between Voronoi and Delaunay lines.
     */
    public boolean vd[] = new boolean[MAX_V_LINES];

    public boolean voronoiB;
    public boolean delaunayB;

    public Voronoi(ComputeGNG compute) {
        vSites          = new SiteVoronoi[MAX_NODES + 1];
        this.compute    = compute;
        dim             = new Dimension();
    }
    /**
     * This array of sites is sorted by y-coordinate (2nd y-coordinate).
     * vSites[1] is the index of the bottom node.
     */
    protected SiteVoronoi vSites[]; // = new SiteVoronoi[cGNG.MAX_NODES + 1];

    // Vars for Voronoi diagram (start).
    int siteIdx, nSites;
    int nVertices, nVEdges;
    int PQ_count;
    SiteVoronoi bottomSite;

    final static int LE = 0;
    final static int RE = 1;

    ListGNG list, pq;

    HalfEdgeVoronoi EL_leftEnd, EL_rightEnd;
    // Vars for Voronoi diagram (end).

    final Dimension dim;

    public Dimension getSize() { return new Dimension(dim); }

    public void setSize(Dimension dim) { this.dim.setSize(dim); }

    public void setSize(int w, int h) { this.dim.setSize(w, h); }

    /**
     * Sort the array of nodes. The result is in the <TT>vSite</TT>-array.
     * The implemented sorting algorithm is heap-sort.
     *
     * @param n          The number of nodes to be sorted
     * @see Voronoi#vSites
     * @see Voronoi#buildMaxHeap
     */
    protected void sortSites(int n) {
        SiteVoronoi exchange;
        int i;

        // Initialize the sorted site array
        final NodeGNG[] nodes = compute.nodes;
        for (i = 1; i <= n; i++) {
            NodeGNG nd      = nodes[i-1];
            SiteVoronoi sv  = new SiteVoronoi();
            sv.coord.x      = nd.x;
            sv.coord.y      = nd.y;
            sv.idx = i-1;
            sv.refCnt       = 0;
            vSites[i]       = sv;
        }
        compute.nNodesChangedB = false;

        // Build a maximum heap
        for (i = n/2; i > 0; i--)
            buildMaxHeap(i, n);

        // Switch elements 1 and i then rebuild heap
        for (i = n; i > 1; i--) {
            exchange    = vSites[1];
            vSites[1]   = vSites[i];
            vSites[i]   = exchange;
            buildMaxHeap(1, i-1);
        }
    }

    /**
     * Build a maximum-heap. The result is in the <TT>vSite</TT>-array.
     *
     * @param i          The start of the interval
     * @param k          The end of the interval
     * @see Voronoi#vSites
     * @see Voronoi#sortSites
     */
    protected void buildMaxHeap(int i, int k) {
        int j = i;
        int m1 = j << 1;
        int son;

        while (m1 <= k) {
            final int m2 = m1 + 1;
            if (m2 <= k) {
                final PointFloat2D c1 = vSites[m1].coord;
                final PointFloat2D c2 = vSites[m2].coord;
                if ((c1.y > c2.y) || (c1.y == c2.y && c1.x > c2.x))
                    son = m1;
                else
                    son = m2;
            } else {
                son = m1;
            }
            final SiteVoronoi   s1 = vSites[j];
            final SiteVoronoi   s2 = vSites[son];
            final PointFloat2D c3 = s1.coord;
            final PointFloat2D c4 = s2.coord;

            if ( (c3.y < c4.y) || (c3.y == c4.y && c3.x < c4.x) ) {
                vSites[j]   = s2;
                vSites[son] = s1;
                j           = son;
                m1          = j << 1;
            } else
                return;
        }
    }

    /**
     * Compute Voronoi diagram.
     * A sweep-line algorithm is implemented (Steven Fortune, 1987).
     * It computes the Voronoi diagram/Delaunay triangulation of n sites
     * in time O(n log n) and space usage O(n).
     * Input: nodes[], Output: lines[] (global).
     *
     * For this to go all the way through, the `compute.nNodes` should
     * be `compute.maxNodes` or the algorithm must be `GNG` or `GG`.
     *
     * `voronoiB` or `delaunayB` should be set if the caller is interested in the lines.
     *
     * This methods calculates `xMin`, `xMax`, `yMin`, `yMax` and then proceeds to `doVoronoi`.
     */
    public boolean computeVoronoi() {
        nLines = 0;

        int i;
        float xMin  = 0f;
        float yMin  = 0f;
        float xMax  = dim.width;
        float yMax  = dim.height;
        siteIdx     = 0;
        nSites      = compute.nNodes;
        nVertices   = 0;
        nVEdges     = 0;
        PQ_count    = 0;

        // Copy nodes[] to vSites[] and sort them
        sortSites(compute.nNodes);

        if ((compute.nNodes == 0) ||
                ((compute.nNodes != compute.maxNodes) && (compute.algorithm != Algorithm.GNG) && (compute.algorithm != Algorithm.GG)))
            return true;

        final PointFloat2D c1 = vSites[1].coord;
        xMin = c1.x;
        xMax = c1.x;
        for (i = 2; i <= nSites; i++) {
            final PointFloat2D c2 = vSites[i].coord;
            if (c2.x < xMin)
                xMin = c2.x;
            if (c2.x > xMax)
                xMax = c2.x;
        }
        yMin = c1.y;
        yMax = vSites[nSites].coord.y;

        doVoronoi();
        return false;
    }

    /**
     * Compute Voronoi diagram (2).
     * A sweep-line algorithm is implemented (Steven Fortune, 1987).
     * Input: nodes[], Output: lines[] (global).
     *
     * @see Voronoi#computeVoronoi
     */
    public void doVoronoi() {
        SiteVoronoi newSite, bot, top, temp, p, v;
        PointFloat2D newIntStar = new PointFloat2D();
        int pm;
        HalfEdgeVoronoi lBnd, rBnd, llBnd, rrBnd, bisector;
        EdgeVoronoi e;

        pq          = new ListGNG();
        bottomSite  = nextSite();
        EL_initialize();
        newSite     = nextSite();

        while (true) {
            if (!pq.empty())
                newIntStar = pq.PQ_min();

            if ((newSite != null) &&
                    (pq.empty() ||
                            (newSite.coord.y < newIntStar.y) ||
                            ((newSite.coord.y == newIntStar.y) && (newSite.coord.x < newIntStar.x))
                    )) {
                lBnd        = EL_leftBnd(newSite.coord);
                rBnd        = lBnd.EL_right;
                bot         = rightReg(lBnd);
                e           = bisect(bot, newSite);
                bisector    = new HalfEdgeVoronoi(e, LE);
                EL_insert(lBnd, bisector);
                if ( (p = intersect(lBnd, bisector)) != null ) {
                    PQ_delete(lBnd);
                    PQ_insert(lBnd, p, dist(p,newSite));
                }
                lBnd        = bisector;
                bisector    = new HalfEdgeVoronoi(e, RE);
                EL_insert(lBnd, bisector);
                if ( (p = intersect(bisector, rBnd)) != null ) {
                    PQ_insert(bisector, p, dist(p,newSite));
                }

                newSite = nextSite();

            } else if ( !pq.empty() ) {
                // intersection is smallest
                PQ_count--;
                lBnd    = pq.PQ_extractMin();
                llBnd   = lBnd.EL_left;
                rBnd    = lBnd.EL_right;
                rrBnd   = rBnd.EL_right;
                bot     = leftReg(lBnd);
                top     = rightReg(rBnd);
                v       = lBnd.vertex;
                makeVertex(v);
                endPoint(lBnd.EL_edge, lBnd.EL_pm, v);
                endPoint(rBnd.EL_edge, rBnd.EL_pm, v);
                EL_delete(lBnd);
                PQ_delete(rBnd);
                EL_delete(rBnd);
                pm = LE;
                if (bot.coord.y > top.coord.y) {
                    temp = bot;
                    bot = top;
                    top = temp;
                    pm = RE;
                }
                e = bisect(bot, top);
                bisector = new HalfEdgeVoronoi(e, pm);
                EL_insert(llBnd, bisector);
                endPoint(e, RE-pm, v);
                deRef(v);
                if ((p = intersect(llBnd, bisector)) != null) {
                    PQ_delete(llBnd);
                    PQ_insert(llBnd, p, dist(p, bot));
                }
                if ((p = intersect(bisector, rrBnd)) != null)
                    PQ_insert(bisector, p, dist(p, bot));
            } else
                break;
        }

        if (voronoiB) {
            for (lBnd = EL_leftEnd.EL_right;
                 lBnd != EL_rightEnd;
                 lBnd = lBnd.EL_right) {
                e = lBnd.EL_edge;
                out_ep(e);
            }
        }
    }

    public void out_bisector(EdgeVoronoi e) {
        line(e.reg[0].coord.x, e.reg[0].coord.y,
             e.reg[1].coord.x, e.reg[1].coord.y, false);
    }

    /** Returns `true` if `line` was called and thus a new line has been added. */
    public boolean out_ep(EdgeVoronoi e) {
        SiteVoronoi s1, s2;
        float x1, x2, y1, y2;

        final float pxMin = 0.0f;
        final float pyMin = 0.0f;
        final float pxMax = dim.width;
        final float pyMax = dim.height;

        if ((e.a == 1.0f) && (e.b >= 0.0f)) {
            s1 = e.ep[1];
            s2 = e.ep[0];
        } else {
            s1 = e.ep[0];
            s2 = e.ep[1];
        }

        if (e.a == 1.0) {
            y1 = pyMin;
            if ((s1 != null) && (s1.coord.y > pyMin))
                y1 = s1.coord.y;
            if (y1 > pyMax)
                return false;
            x1 = e.c - e.b * y1;
            y2 = pyMax;
            if ((s2 != null) && (s2.coord.y < pyMax))
                y2 = s2.coord.y;
            if (y2 < pyMin)
                return false;
            x2 = e.c - e.b * y2;
            if ((x1 > pxMax & x2 > pxMax) | (x1 < pxMin & x2 < pxMin))
                return false;
            if (x1 > pxMax) {
                x1 = pxMax;
                y1 = (e.c - x1) / e.b;
            }
            if (x1 < pxMin) {
                x1 = pxMin;
                y1 = (e.c - x1) / e.b;
            }
            if (x2 > pxMax) {
                x2 = pxMax;
                y2 = (e.c - x2) / e.b;
            }
            if (x2 < pxMin) {
                x2 = pxMin;
                y2 = (e.c - x2) / e.b;
            }
        } else {
            x1 = pxMin;
            if ((s1 != null) && (s1.coord.x > pxMin))
                x1 = s1.coord.x;
            if (x1 > pxMax)
                return false;
            y1 = e.c - e.a * x1;
            x2 = pxMax;
            if ((s2 != null) && (s2.coord.x < pxMax))
                x2 = s2.coord.x;
            if (x2 < pxMin)
                return false;
            y2 = e.c - e.a * x2;
            if ((y1 > pyMax & y2 > pyMax) | (y1 < pyMin & y2 < pyMin))
                return false;
            if (y1 > pyMax) {
                y1 = pyMax;
                x1 = (e.c - y1) / e.a;
            }
            if (y1 < pyMin) {
                y1 = pyMin;
                x1 = (e.c - y1) / e.a;
            }
            if (y2 > pyMax) {
                y2 = pyMax;
                x2 = (e.c - y2) / e.a;
            }
            if (y2 < pyMin) {
                y2 = pyMin;
                x2 = (e.c - y2) / e.a;
            }
        }
        line(x1, y1, x2, y2, true);
        return true;
    }

    public void line(float x1, float y1, float x2, float y2, boolean vdB) {
//        final LineInt2D l = new LineInt2D((int) x1, (int) y1, (int) x2, (int) y2);
        final LineFloat2D l = new LineFloat2D(x1, y1, x2, y2);
        lines  [nLines] = l;
        vd     [nLines] = vdB;
        nLines++;
    }

    public void PQ_insert(HalfEdgeVoronoi he, SiteVoronoi v, float offset) {
        he.vertex = v;
        v.refCnt++;
        he.yStar = v.coord.y + offset;

        pq.PQ_insert(he);
        PQ_count++;
    }

    public void PQ_delete(HalfEdgeVoronoi he) {
        if(he.vertex != null) {
            pq.PQ_delete(he);
            PQ_count--;
            deRef(he.vertex);
            he.vertex = null;
        }
    }

    public float dist(SiteVoronoi s, SiteVoronoi t) {
        float dx, dy;
        dx = s.coord.x - t.coord.x;
        dy = s.coord.y - t.coord.y;
        return ((float) Math.sqrt(dx * dx + dy * dy));
    }

    public SiteVoronoi intersect(HalfEdgeVoronoi el1, HalfEdgeVoronoi el2) {
        EdgeVoronoi e1, e2, e;
        HalfEdgeVoronoi el;
        float d, xInt, yInt;
        boolean right_of_site;
        SiteVoronoi v;

        e1 = el1.EL_edge;
        e2 = el2.EL_edge;
        if ((e1 == null) || (e2 == null))
            return null;
        if (e1.reg[1] == e2.reg[1])
            return null;

        d = e1.a * e2.b - e1.b * e2.a;
        if ( (-1.0e-10 < d) && (d < 1.0e-10) )
            return null;

        xInt = (e1.c * e2.b - e2.c * e1.b)/d;
        yInt = (e2.c * e1.a - e1.c * e2.a)/d;

        if ((e1.reg[1].coord.y < e2.reg[1].coord.y) ||
                ((e1.reg[1].coord.y == e2.reg[1].coord.y) && (e1.reg[1].coord.x < e2.reg[1].coord.x))) {
            el  = el1;
            e   = e1;
        } else {
            el  = el2;
            e   = e2;
        }
        right_of_site = (xInt >= e.reg[1].coord.x);
        if ((right_of_site && el.EL_pm == LE) ||
                (!right_of_site && el.EL_pm == RE))
            return null;

        v           = new SiteVoronoi();
        v.refCnt    = 0;
        v.coord.x   = xInt;
        v.coord.y   = yInt;
        return v;
    }

    public void EL_insert(HalfEdgeVoronoi lb, HalfEdgeVoronoi henew) {
        henew.EL_left           = lb;
        henew.EL_right          = lb.EL_right;
        (lb.EL_right).EL_left   = henew;
        lb.EL_right             = henew;
    }

    public void deRef(SiteVoronoi v) {
        v.refCnt--;
        if (v.refCnt == 0) v = null;
    }

    public EdgeVoronoi bisect(SiteVoronoi s1, SiteVoronoi s2) {
        float dx, dy, adx, ady;
        EdgeVoronoi newEdge;

        newEdge = new EdgeVoronoi();

        newEdge.reg[0] = s1;
        newEdge.reg[1] = s2;
        s1.refCnt++;
        s2.refCnt++;
        newEdge.ep[0] = null;
        newEdge.ep[1] = null;

        dx = s2.coord.x - s1.coord.x;
        dy = s2.coord.y - s1.coord.y;
        adx = (dx > 0) ? dx : -dx;
        ady = (dy > 0) ? dy : -dy;
        newEdge.c = s1.coord.x * dx + s1.coord.y * dy + (dx*dx + dy*dy) * 0.5f;
        if (adx > ady) {
            newEdge.a = 1.0f;
            newEdge.b = dy/dx;
            newEdge.c /= dx;
        } else {
            newEdge.b = 1.0f;
            newEdge.a = dx/dy;
            newEdge.c /= dy;
        }

        newEdge.edgeIdx = nVEdges;
        if (delaunayB)
            out_bisector(newEdge);
        nVEdges++;
        return(newEdge);
    }

    public void endPoint(EdgeVoronoi e, int lr, SiteVoronoi s) {
        e.ep[lr] = s;
        s.refCnt++;
        if (e.ep[RE-lr] == null)
            return;
        if (voronoiB)
            out_ep(e);
        deRef(e.reg[LE]);
        deRef(e.reg[RE]);
        e = null;
    }

    public void makeVertex(SiteVoronoi v) {
        v.idx = nVertices;
        nVertices++;
    }

    public void EL_delete(HalfEdgeVoronoi he) {
        (he.EL_left ).EL_right = he.EL_right;
        (he.EL_right).EL_left  = he.EL_left;
        he.EL_edge = null;
    }

    public SiteVoronoi rightReg(HalfEdgeVoronoi he) {
        if(he.EL_edge == null)
            return(bottomSite);
        return( he.EL_pm == LE ?
                he.EL_edge.reg[RE] :
                    he.EL_edge.reg[LE] );
    }

    public SiteVoronoi leftReg(HalfEdgeVoronoi he) {
        if (he.EL_edge == null)
            return(bottomSite);
        return( he.EL_pm == LE ?
                he.EL_edge.reg[LE] :
                    he.EL_edge.reg[RE] );
    }

    public void EL_initialize() {
        list                    = new ListGNG();
        EL_leftEnd              = new HalfEdgeVoronoi(null, 0);
        EL_rightEnd             = new HalfEdgeVoronoi(null, 0);
        EL_leftEnd.EL_left      = null;
        EL_leftEnd.EL_right     = EL_rightEnd;
        EL_rightEnd.EL_left     = EL_leftEnd;
        EL_rightEnd.EL_right    = null;
        list.insert(EL_leftEnd, list.head);
        list.insert(EL_rightEnd, list.last());
    }

    public HalfEdgeVoronoi EL_leftBnd(PointFloat2D p) {
        HalfEdgeVoronoi he;
        he = (list.first()).elem;
        // Now search linear list of half-edges for the correct one
        if ( he == EL_leftEnd || (he != EL_rightEnd && isRightOf(he,p)) ) {
            do {
                he = he.EL_right;
            } while ( (he != EL_rightEnd) && isRightOf(he,p) );
            he = he.EL_left;
        } else {
            do {
                he = he.EL_left;
            } while ( he != EL_leftEnd && !isRightOf(he,p) );
        }
        return(he);
    }

    // returns true if p is to right of half-edge e
    public boolean isRightOf(HalfEdgeVoronoi el, PointFloat2D p) {
        EdgeVoronoi e;
        SiteVoronoi topSite;
        boolean right_of_site, above, fast;
        float dxp, dyp, dxs, t1, t2, t3, yl;

        e = el.EL_edge;
        topSite = e.reg[1];
        right_of_site = p.x > topSite.coord.x;
        if(right_of_site && (el.EL_pm == LE) )
            return(true);
        if(!right_of_site && (el.EL_pm == RE) )
            return (false);

        if (e.a == 1.0) {
            dyp = p.y - topSite.coord.y;
            dxp = p.x - topSite.coord.x;
            fast = false;
            if ( (!right_of_site & e.b < 0.0) | (right_of_site & e.b >= 0.0) ) {
                above = (dyp >= e.b * dxp);
                fast = above;
            }
            else {
                above = (p.x + p.y * e.b) > e.c;
                if(e.b < 0.0)
                    above = !above;
                if (!above)
                    fast = true;
            }
            if (!fast) {
                dxs = topSite.coord.x - (e.reg[0]).coord.x;
                above = (e.b * (dxp*dxp - dyp*dyp)) <
                        (dxs * dyp * (1.0 + 2.0 * dxp/dxs + e.b * e.b));
                if(e.b < 0.0)
                    above = !above;
            }
        } else {  // e.b == 1.0
                yl = e.c - e.a * p.x;
                t1 = p.y - yl;
                t2 = p.x - topSite.coord.x;
                t3 = yl - topSite.coord.y;
                above = t1*t1 > t2*t2 + t3*t3;
        }
        return (el.EL_pm == LE ? above : !above);
    }

    public SiteVoronoi nextSite() {
        siteIdx++;
        return siteIdx > nSites ? null : vSites[siteIdx];
    }
}
