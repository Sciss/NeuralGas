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

/**
 * A class representing a half-edge in the Voronoi diagram
 *
 */
class HalfEdgeVoronoi {
  public HalfEdgeVoronoi EL_left;
  public HalfEdgeVoronoi EL_right;
  public EdgeVoronoi EL_edge  = null;
  public SiteVoronoi vertex   = null;
  public int EL_pm            = -1;
  public int EL_refCnt        = -1;
  public float yStar          = -1.0f;

  public HalfEdgeVoronoi() {
    EL_edge   = new EdgeVoronoi();
    vertex    = new SiteVoronoi();
    EL_pm     = 0;
    yStar     = 0.0f;
  }

  public HalfEdgeVoronoi(EdgeVoronoi e, int pm) {
    EL_edge   = e;
    EL_pm     = pm;
    vertex    = null;
    EL_refCnt = 0;
  }

  /**
   * Returns whether this edge is greater than the passed edge.
   *
   * @param he	 	The edge to compare this edge to.
   */
  public boolean greaterThan(HalfEdgeVoronoi he) {
    return yStar > he.yStar;
  }
  
  /**
   * Returns whether this edge is equal to the passed edge.
   *
   * @param he	 	The edge to compare this edge to.
   */
  public boolean equal(HalfEdgeVoronoi he) {
    return yStar == he.yStar;
  }
  
  /**
   * Prints this edge.
   */
  public void print() {
    System.out.println("HE: yStar = " + yStar + ", EL_pm = " + EL_pm);
  }

}
