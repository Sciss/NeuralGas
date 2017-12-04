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
    PanelGNG cGNG;
    ComputeGNG compute;

    Voronoi() {
    }

    Voronoi(PanelGNG cGNG) {
        this.cGNG = cGNG;
        vSites = new SiteVoronoi[MAX_NODES + 1];
        this.compute = cGNG.compute;
    }
    /**
     * This array of sites is sorted by y-coordinate (2nd y-coordinate).
     * vSites[1] is the index of the bottom node.
     */
    protected SiteVoronoi vSites[];// = new SiteVoronoi[cGNG.MAX_NODES + 1];
    // Vars for Voronoi diagram (start).
    int xMin, yMin, xMax, yMax;
    int deltaX, deltaY;
    int siteIdx, nSites, sqrt_nSites;
    int nVertices, nVEdges;
    int PQ_count, PQ_min;
    SiteVoronoi bottomSite;
    final int LE = 0;
    final int RE = 1;
    final int DELETED = -2;
    ListGNG list, pq;
    boolean debug = true;
    HalfEdgeVoronoi EL_leftEnd, EL_rightEnd;
    float pxMin, pyMin, pxMax, pyMax;
    // Vars for Voronoi diagram (end).

    /**
     * Sort the array of nodes. The result is in the <TT> vSite</TT>-array.
     *  The implemented sorting algorithm is heap-sort.
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
            NodeGNG nd = nodes[i-1];
            SiteVoronoi sv = new SiteVoronoi();
            sv.coord.x = nd.x;
            sv.coord.y = nd.y;
            sv.sitenbr = i-1;
            sv.refcnt = 0;
            vSites[i] = sv;
        }
        compute.nNodesChangedB = false;

        // Build a maximum heap
        for (i = n/2; i > 0; i--)
            buildMaxHeap(i, n);

        // Switch elements 1 and i then reheap
        for (i = n; i > 1; i--) {
            exchange = vSites[1];
            vSites[1] = vSites[i];
            vSites[i] = exchange;
            buildMaxHeap(1, i-1);
        }
    }

    /**
     * Build a maximum-heap. The result is in the <TT> vSite</TT>-array.
     *
     * @param i          The start of the interval
     * @param k          The end of the interval
     * @see Voronoi#vSites
     * @see Voronoi#sortSites
     */
    protected void buildMaxHeap(int i, int k) {
        int j = i;
        int son;

        while (2*j <= k) {
            if (2*j+1 <= k)
                if ( (vSites[2*j].coord.y > vSites[2*j+1].coord.y) ||
                        (vSites[2*j].coord.y == vSites[2*j+1].coord.y &&
                        vSites[2*j].coord.x > vSites[2*j+1].coord.x) )
                    son = 2*j;
                else
                    son = 2*j + 1;
            else
                son = 2*j;

            if ( (vSites[j].coord.y < vSites[son].coord.y) ||
                    (vSites[j].coord.y == vSites[son].coord.y &&
                    vSites[j].coord.x < vSites[son].coord.x) ) {
                SiteVoronoi exchange = vSites[j];
                vSites[j] = vSites[son];
                vSites[son] = exchange;
                j = son;
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
     */
    public boolean computeVoronoi() {
        Dimension d = cGNG.getSize();
        int i;
        xMin = 0;
        yMin = 0;
        xMax = deltaX = d.width;
        yMax = deltaY = d.height;
        siteIdx = 0;
        nSites = compute.nNodes;
        nVertices = 0;
        nVEdges = 0;
        sqrt_nSites = (int) Math.sqrt(nSites + 4);
        PQ_count = 0;
        PQ_min = 0;

        // Copy nodes[] to vSites[] and sort them
        sortSites(compute.nNodes);

        if ( (compute.nNodes == 0) ||
                ( (compute.nNodes != compute.maxNodes) && (compute.algorithm != Algorithm.GNG) && (compute.algorithm != Algorithm.GG) ) )
            return true;

        xMin = (int) vSites[1].coord.x;
        xMax = (int) vSites[1].coord.x;
        for(i = 2; i <= nSites; i++) {
            if (vSites[i].coord.x < xMin)
                xMin = (int) vSites[i].coord.x;
            if (vSites[i].coord.x > xMax)
                xMax = (int) vSites[i].coord.x;
        }
        yMin = (int) vSites[1].coord.y;
        yMax = (int) vSites[nSites].coord.y;

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
        FPoint newIntStar = new FPoint();
        int pm;
        HalfEdgeVoronoi lbnd, rbnd, llbnd, rrbnd, bisector;
        EdgeVoronoi e;

        pq = new ListGNG();
        bottomSite = nextSite();
        EL_initialize();
        newSite = nextSite();

        while(true) {
            if (!pq.empty())
                newIntStar = pq.PQ_min();

            if ( (newSite != null) &&
                    ( pq.empty() ||
                            (newSite.coord.y < newIntStar.y) ||
                            ( (newSite.coord.y == newIntStar.y) &&
                                    (newSite.coord.x < newIntStar.x) )
                            ) ) {
                lbnd = ELleftbnd(newSite.coord);
                rbnd = lbnd.ELright;
                bot = rightReg(lbnd);
                e = bisect(bot, newSite);
                bisector = new HalfEdgeVoronoi(e, LE);
                EL_insert(lbnd, bisector);
                if ( (p = intersect(lbnd, bisector)) != null ) {
                    PQ_delete(lbnd);
                    PQ_insert(lbnd, p, dist(p,newSite));
                }
                lbnd = bisector;
                bisector = new HalfEdgeVoronoi(e, RE);
                EL_insert(lbnd, bisector);
                if ( (p = intersect(bisector, rbnd)) != null ) {
                    PQ_insert(bisector, p, dist(p,newSite));
                }

                newSite = nextSite();

            } else if ( !pq.empty() ) {
                // intersection is smallest
                PQ_count--;
                lbnd = pq.PQextractmin();
                llbnd = lbnd.ELleft;
                rbnd = lbnd.ELright;
                rrbnd = rbnd.ELright;
                bot = leftReg(lbnd);
                top = rightReg(rbnd);
                v = lbnd.vertex;
                makeVertex(v);
                endpoint(lbnd.ELedge, lbnd.ELpm, v);
                endpoint(rbnd.ELedge, rbnd.ELpm, v);
                EL_delete(lbnd);
                PQ_delete(rbnd);
                EL_delete(rbnd);
                pm = LE;
                if (bot.coord.y > top.coord.y) {
                    temp = bot;
                    bot = top;
                    top = temp;
                    pm = RE;
                }
                e = bisect(bot, top);
                bisector = new HalfEdgeVoronoi(e, pm);
                EL_insert(llbnd, bisector);
                endpoint(e, RE-pm, v);
                deref(v);
                if ( (p = intersect(llbnd, bisector)) != null ) {
                    PQ_delete(llbnd);
                    PQ_insert(llbnd, p, dist(p, bot) );
                }
                if ( (p = intersect(bisector, rrbnd)) != null )
                    PQ_insert(bisector, p, dist(p, bot) );
            } else
                break;
        }

        if (cGNG.voronoiB) {
            for(lbnd = EL_leftEnd.ELright;
                    lbnd != EL_rightEnd;
                    lbnd = lbnd.ELright) {
                e = lbnd.ELedge;
                out_ep(e);
            }
        }
    }

    public void out_bisector(EdgeVoronoi e) {
        line(e.reg[0].coord.x, e.reg[0].coord.y,
                e.reg[1].coord.x, e.reg[1].coord.y, false);

    }

    public void out_ep(EdgeVoronoi e) {
        SiteVoronoi s1, s2;
        float x1, x2, y1, y2;
        Dimension dim = cGNG.getSize();

        pxMin = 0.0f;
        pyMin = 0.0f;
        pxMax = dim.width;
        pyMax = dim.height;

        if ( (e.a == 1.0f) && (e.b >= 0.0f) ) {
            s1 = e.ep[1];
            s2 = e.ep[0];
        } else {
            s1 = e.ep[0];
            s2 = e.ep[1];
        }

        if (e.a == 1.0) {
            y1 = pyMin;
            if ( (s1 != null) && (s1.coord.y > pyMin) )
                y1 = s1.coord.y;
            if (y1 > pyMax)
                return;
            x1 = e.c - e.b * y1;
            y2 = pyMax;
            if ( (s2 != null) && (s2.coord.y < pyMax) )
                y2 = s2.coord.y;
            if (y2 < pyMin)
                return;
            x2 = e.c - e.b * y2;
            if ( (x1 > pxMax & x2 > pxMax) | (x1 < pxMin & x2 < pxMin) )
                return;
            if (x1 > pxMax) {
                x1 = pxMax;
                y1 = (e.c - x1)/e.b;
            }
            if (x1 < pxMin) {
                x1 = pxMin;
                y1 = (e.c - x1)/e.b;
            }
            if (x2 > pxMax) {
                x2 = pxMax;
                y2 = (e.c - x2)/e.b;
            }
            if (x2 < pxMin) {
                x2 = pxMin;
                y2 = (e.c - x2)/e.b;
            }
        } else {
            x1 = pxMin;
            if ( (s1 != null) && (s1.coord.x > pxMin) )
                x1 = s1.coord.x;
            if (x1 > pxMax)
                return;
            y1 = e.c - e.a * x1;
            x2 = pxMax;
            if ( (s2 != null) && (s2.coord.x < pxMax) )
                x2 = s2.coord.x;
            if (x2 < pxMin)
                return;
            y2 = e.c - e.a * x2;
            if ( (y1 > pyMax & y2 > pyMax) | ( y1 < pyMin & y2 < pyMin) )
                return;
            if (y1 > pyMax) {
                y1 = pyMax;
                x1 = (e.c - y1)/e.a;
            }
            if(y1 < pyMin) {
                y1 = pyMin;
                x1 = (e.c - y1)/e.a;
            }
            if(y2 > pyMax) {
                y2 = pyMax;
                x2 = (e.c - y2)/e.a;
            }
            if(y2 < pyMin) {
                y2 = pyMin;
                x2 = (e.c - y2)/e.a;
            }
        }
        line(x1, y1, x2, y2, true);
    }

    public void line(float x1, float y1, float x2, float y2, boolean vdB) {
        LineGNG l = new LineGNG((int) x1, (int) y1, (int) x2, (int) y2);
        cGNG.lines[cGNG.nLines] = l;
        cGNG.vd[cGNG.nLines] = vdB;
        cGNG.nLines++;
    }

    public void PQ_insert(HalfEdgeVoronoi he, SiteVoronoi v, float offset) {
        he.vertex = v;
        v.refcnt++;
        he.ystar = v.coord.y + offset;

        pq.PQinsert(he);
        PQ_count++;
    }

    public void PQ_delete(HalfEdgeVoronoi he) {
        if(he.vertex != null) {
            pq.PQdelete(he);
            PQ_count--;
            deref(he.vertex);
            he.vertex = null;
        }
    }

    public float dist(SiteVoronoi s, SiteVoronoi t)
    {
        float dx,dy;
        dx = s.coord.x - t.coord.x;
        dy = s.coord.y - t.coord.y;
        return( (float) Math.sqrt(dx*dx + dy*dy) );
    }

    public SiteVoronoi intersect(HalfEdgeVoronoi el1, HalfEdgeVoronoi el2) {
        EdgeVoronoi e1, e2, e;
        HalfEdgeVoronoi el;
        float d, xInt, yInt;
        boolean right_of_site;
        SiteVoronoi v;

        e1 = el1.ELedge;
        e2 = el2.ELedge;
        if ( (e1 == null) || (e2 == null) )
            return(null);
        if (e1.reg[1] == e2.reg[1])
            return(null);

        d = e1.a * e2.b - e1.b * e2.a;
        if ( (-1.0e-10 < d) && (d < 1.0e-10) )
            return(null);

        xInt = (e1.c * e2.b - e2.c * e1.b)/d;
        yInt = (e2.c * e1.a - e1.c * e2.a)/d;

        if ( (e1.reg[1].coord.y < e2.reg[1].coord.y) ||
                ( (e1.reg[1].coord.y == e2.reg[1].coord.y) &&
                        (e1.reg[1].coord.x < e2.reg[1].coord.x) ) ) {
            el = el1;
            e = e1;
        } else {
            el = el2;
            e = e2;
        }
        right_of_site = (xInt >= e.reg[1].coord.x);
        if ( (right_of_site && el.ELpm == LE) ||
                (!right_of_site && el.ELpm == RE) )
            return(null);

        v = new SiteVoronoi();
        v.refcnt = 0;
        v.coord.x = xInt;
        v.coord.y = yInt;
        return(v);
    }

    public void EL_insert(HalfEdgeVoronoi lb, HalfEdgeVoronoi henew) {
        henew.ELleft = lb;
        henew.ELright = lb.ELright;
        (lb.ELright).ELleft = henew;
        lb.ELright = henew;
    }

    public void deref(SiteVoronoi v) {
        v.refcnt--;
        if (v.refcnt == 0 )
            v = null;
    }

    public EdgeVoronoi bisect(SiteVoronoi s1, SiteVoronoi s2) {
        float dx, dy, adx, ady;
        EdgeVoronoi newEdge;

        newEdge = new EdgeVoronoi();

        newEdge.reg[0] = s1;
        newEdge.reg[1] = s2;
        s1.refcnt++;
        s2.refcnt++;
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

        newEdge.edgenbr = nVEdges;
        if (cGNG.delaunayB)
            out_bisector(newEdge);
        nVEdges++;
        return(newEdge);
    }

    public void endpoint(EdgeVoronoi e, int lr, SiteVoronoi s) {
        e.ep[lr] = s;
        s.refcnt++;
        if (e.ep[RE-lr] == null)
            return;
        if (cGNG.voronoiB)
            out_ep(e);
        deref(e.reg[LE]);
        deref(e.reg[RE]);
        e = null;
    }

    public void makeVertex(SiteVoronoi v) {
        v.sitenbr = nVertices;
        nVertices++;
    }

    public void EL_delete(HalfEdgeVoronoi he) {
        (he.ELleft).ELright = he.ELright;
        (he.ELright).ELleft = he.ELleft;
        he.ELedge = null;
    }

    public SiteVoronoi rightReg(HalfEdgeVoronoi he) {
        if(he.ELedge == null)
            return(bottomSite);
        return( he.ELpm == LE ?
                he.ELedge.reg[RE] :
                    he.ELedge.reg[LE] );
    }

    public SiteVoronoi leftReg(HalfEdgeVoronoi he) {
        if (he.ELedge == null)
            return(bottomSite);
        return( he.ELpm == LE ?
                he.ELedge.reg[LE] :
                    he.ELedge.reg[RE] );
    }

    public void EL_initialize() {
        list = new ListGNG();
        EL_leftEnd = new HalfEdgeVoronoi(null, 0);
        EL_rightEnd = new HalfEdgeVoronoi(null, 0);
        EL_leftEnd.ELleft = null;
        EL_leftEnd.ELright = EL_rightEnd;
        EL_rightEnd.ELleft = EL_leftEnd;
        EL_rightEnd.ELright = null;
        list.insert(EL_leftEnd, list.head);
        list.insert(EL_rightEnd, list.last());
    }

    public HalfEdgeVoronoi ELleftbnd(FPoint p) {
        HalfEdgeVoronoi he;
        he = (list.first()).elem;
        // Now search linear list of half-edges for the correct one
        if ( he == EL_leftEnd || (he != EL_rightEnd && right_of(he,p)) ) {
            do {
                he = he.ELright;
            } while ( (he != EL_rightEnd) && right_of(he,p) );
            he = he.ELleft;
        } else {
            do {
                he = he.ELleft;
            } while ( he != EL_leftEnd && !right_of(he,p) );
        }
        return(he);
    }

    // returns true if p is to right of half-edge e
    public boolean right_of(HalfEdgeVoronoi el, FPoint p) {
        EdgeVoronoi e;
        SiteVoronoi topSite;
        boolean right_of_site, above, fast;
        float dxp, dyp, dxs, t1, t2, t3, yl;

        e = el.ELedge;
        topSite = e.reg[1];
        right_of_site = p.x > topSite.coord.x;
        if(right_of_site && (el.ELpm == LE) )
            return(true);
        if(!right_of_site && (el.ELpm == RE) )
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
        return (el.ELpm == LE ? above : !above);
    }

    public SiteVoronoi nextSite() {
        siteIdx++;
        if (siteIdx > nSites)
            return(null);
        return(vSites[siteIdx]);
    }
}
