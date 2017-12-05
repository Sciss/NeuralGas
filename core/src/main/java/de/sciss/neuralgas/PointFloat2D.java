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
 * A class representing a float point in the plane.
 */
public class PointFloat2D {
    /**
     * The x coordinate
     */
    public float x;

    /**
     * The y coordinate
     */
    public float y;

    /**
     * Constructor.
     */
    public PointFloat2D() {
        this.x = -1.0f;
        this.y = -1.0f;
    }

    /**
     * Constructor, allows setting the coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public PointFloat2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the member variables.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the member variables.
     *
     * @param p The coordinates
     */
    public void set(PointFloat2D p) {
        x = p.x;
        y = p.y;
    }

    /**
     * Test the member variables.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return Equal?
     */
    public boolean equal(float x, float y) {
        return (this.x == x) && (this.y == y);
    }

    @Override
    public String toString() {
        return String.format("PointFloat2D(x: %s, y: %s)", x, y);
    }
}