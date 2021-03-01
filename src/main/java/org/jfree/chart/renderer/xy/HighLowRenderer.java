/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2020, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * --------------------
 * HighLowRenderer.java
 * --------------------
 * (C) Copyright 2001-2016, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard Atkinson;
 *                   Christian W. Zuckschwerdt;
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A renderer that draws high/low/open/close markers on an {@link XYPlot}
 * (requires a {@link OHLCDataset}).  This renderer does not include code to
 * calculate the crosshair point for the plot.
 *
 * The example shown here is generated by the {@code HighLowChartDemo1.java} 
 * program included in the JFreeChart Demo Collection:
 * <br><br>
 * <img src="doc-files/HighLowRendererSample.png" alt="HighLowRendererSample.png">
 */
public class HighLowRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8135673815876552516L;

    /** A flag that controls whether the open ticks are drawn. */
    private boolean drawOpenTicks;

    /** A flag that controls whether the close ticks are drawn. */
    private boolean drawCloseTicks;

    /**
     * The paint used for the open ticks (if {@code null}, the series
     * paint is used instead).
     */
    private transient Paint openTickPaint;

    /**
     * The paint used for the close ticks (if {@code null}, the series
     * paint is used instead).
     */
    private transient Paint closeTickPaint;

    /**
     * The tick length (in Java2D units).
     *
     * @since 1.0.10
     */
    private double tickLength;

    /**
     * The default constructor.
     */
    public HighLowRenderer() {
        super();
        this.drawOpenTicks = true;
        this.drawCloseTicks = true;
        this.tickLength = 2.0;
    }

    /**
     * Returns the flag that controls whether open ticks are drawn.
     *
     * @return A boolean.
     *
     * @see #getDrawCloseTicks()
     * @see #setDrawOpenTicks(boolean)
     */
    public boolean getDrawOpenTicks() {
        return this.drawOpenTicks;
    }

    /**
     * Sets the flag that controls whether open ticks are drawn, and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param draw  the flag.
     *
     * @see #getDrawOpenTicks()
     */
    public void setDrawOpenTicks(boolean draw) {
        this.drawOpenTicks = draw;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether close ticks are drawn.
     *
     * @return A boolean.
     *
     * @see #getDrawOpenTicks()
     * @see #setDrawCloseTicks(boolean)
     */
    public boolean getDrawCloseTicks() {
        return this.drawCloseTicks;
    }

    /**
     * Sets the flag that controls whether close ticks are drawn, and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param draw  the flag.
     *
     * @see #getDrawCloseTicks()
     */
    public void setDrawCloseTicks(boolean draw) {
        this.drawCloseTicks = draw;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the ticks for the open values.
     *
     * @return The paint used to draw the ticks for the open values (possibly
     *         {@code null}).
     *
     * @see #setOpenTickPaint(Paint)
     */
    public Paint getOpenTickPaint() {
        return this.openTickPaint;
    }

    /**
     * Sets the paint used to draw the ticks for the open values and sends a
     * {@link RendererChangeEvent} to all registered listeners.  If you set
     * this to {@code null} (the default), the series paint is used
     * instead.
     *
     * @param paint  the paint ({@code null} permitted).
     *
     * @see #getOpenTickPaint()
     */
    public void setOpenTickPaint(Paint paint) {
        this.openTickPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the ticks for the close values.
     *
     * @return The paint used to draw the ticks for the close values (possibly
     *         {@code null}).
     *
     * @see #setCloseTickPaint(Paint)
     */
    public Paint getCloseTickPaint() {
        return this.closeTickPaint;
    }

    /**
     * Sets the paint used to draw the ticks for the close values and sends a
     * {@link RendererChangeEvent} to all registered listeners.  If you set
     * this to {@code null} (the default), the series paint is used
     * instead.
     *
     * @param paint  the paint ({@code null} permitted).
     *
     * @see #getCloseTickPaint()
     */
    public void setCloseTickPaint(Paint paint) {
        this.closeTickPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the tick length (in Java2D units).
     *
     * @return The tick length.
     *
     * @since 1.0.10
     *
     * @see #setTickLength(double)
     */
    public double getTickLength() {
        return this.tickLength;
    }

    /**
     * Sets the tick length (in Java2D units) and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param length  the length.
     *
     * @since 1.0.10
     *
     * @see #getTickLength()
     */
    public void setTickLength(double length) {
        this.tickLength = length;
        fireChangeEvent();
    }

    /**
     * Returns the range of values the renderer requires to display all the
     * items from the specified dataset.
     *
     * @param dataset  the dataset ({@code null} permitted).
     *
     * @return The range ({@code null} if the dataset is {@code null}
     *         or empty).
     */
    @Override
    public Range findRangeBounds(XYDataset dataset) {
        if (dataset != null) {
            return DatasetUtils.findRangeBounds(dataset, true);
        }
        else {
            return null;
        }
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        ({@code null} permitted).
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        double x = dataset.getXValue(series, item);
        if (!domainAxis.getRange().contains(x)) {
            return;    // the x value is not within the axis range
        }
        double xx = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());

        // setup for collecting optional entity info...
        Shape entityArea = null;
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge location = plot.getRangeAxisEdge();

        Paint itemPaint = getItemPaint(series, item);
        Stroke itemStroke = getItemStroke(series, item);
        g2.setPaint(itemPaint);
        g2.setStroke(itemStroke);

        if (dataset instanceof OHLCDataset) {
            OHLCDataset hld = (OHLCDataset) dataset;

            double yHigh = hld.getHighValue(series, item);
            double yLow = hld.getLowValue(series, item);
            if (!Double.isNaN(yHigh) && !Double.isNaN(yLow)) {
                double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea,
                        location);
                double yyLow = rangeAxis.valueToJava2D(yLow, dataArea,
                        location);
                if (orientation == PlotOrientation.HORIZONTAL) {
                    g2.draw(new Line2D.Double(yyLow, xx, yyHigh, xx));
                    entityArea = new Rectangle2D.Double(Math.min(yyLow, yyHigh),
                            xx - 1.0, Math.abs(yyHigh - yyLow), 2.0);
                }
                else if (orientation == PlotOrientation.VERTICAL) {
                    g2.draw(new Line2D.Double(xx, yyLow, xx, yyHigh));
                    entityArea = new Rectangle2D.Double(xx - 1.0,
                            Math.min(yyLow, yyHigh), 2.0,
                            Math.abs(yyHigh - yyLow));
                }
            }

            double delta = getTickLength();
            if (domainAxis.isInverted()) {
                delta = -delta;
            }
            if (getDrawOpenTicks()) {
                double yOpen = hld.getOpenValue(series, item);
                if (!Double.isNaN(yOpen)) {
                    double yyOpen = rangeAxis.valueToJava2D(yOpen, dataArea,
                            location);
                    if (this.openTickPaint != null) {
                        g2.setPaint(this.openTickPaint);
                    }
                    else {
                        g2.setPaint(itemPaint);
                    }
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        g2.draw(new Line2D.Double(yyOpen, xx + delta, yyOpen,
                                xx));
                    }
                    else if (orientation == PlotOrientation.VERTICAL) {
                        g2.draw(new Line2D.Double(xx - delta, yyOpen, xx,
                                yyOpen));
                    }
                }
            }

            if (getDrawCloseTicks()) {
                double yClose = hld.getCloseValue(series, item);
                if (!Double.isNaN(yClose)) {
                    double yyClose = rangeAxis.valueToJava2D(
                        yClose, dataArea, location);
                    if (this.closeTickPaint != null) {
                        g2.setPaint(this.closeTickPaint);
                    }
                    else {
                        g2.setPaint(itemPaint);
                    }
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        g2.draw(new Line2D.Double(yyClose, xx, yyClose,
                                xx - delta));
                    }
                    else if (orientation == PlotOrientation.VERTICAL) {
                        g2.draw(new Line2D.Double(xx, yyClose, xx + delta,
                                yyClose));
                    }
                }
            }

        }
        else {
            // not a HighLowDataset, so just draw a line connecting this point
            // with the previous point...
            if (item > 0) {
                double x0 = dataset.getXValue(series, item - 1);
                double y0 = dataset.getYValue(series, item - 1);
                double y = dataset.getYValue(series, item);
                if (Double.isNaN(x0) || Double.isNaN(y0) || Double.isNaN(y)) {
                    return;
                }
                double xx0 = domainAxis.valueToJava2D(x0, dataArea,
                        plot.getDomainAxisEdge());
                double yy0 = rangeAxis.valueToJava2D(y0, dataArea, location);
                double yy = rangeAxis.valueToJava2D(y, dataArea, location);
                if (orientation == PlotOrientation.HORIZONTAL) {
                    g2.draw(new Line2D.Double(yy0, xx0, yy, xx));
                }
                else if (orientation == PlotOrientation.VERTICAL) {
                    g2.draw(new Line2D.Double(xx0, yy0, xx, yy));
                }
            }
        }

        if (entities != null) {
            addEntity(entities, entityArea, dataset, series, item, 0.0, 0.0);
        }

    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HighLowRenderer)) {
            return false;
        }
        HighLowRenderer that = (HighLowRenderer) obj;
        if (this.drawOpenTicks != that.drawOpenTicks) {
            return false;
        }
        if (this.drawCloseTicks != that.drawCloseTicks) {
            return false;
        }
        if (!PaintUtils.equal(this.openTickPaint, that.openTickPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.closeTickPaint, that.closeTickPaint)) {
            return false;
        }
        if (this.tickLength != that.tickLength) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.openTickPaint = SerialUtils.readPaint(stream);
        this.closeTickPaint = SerialUtils.readPaint(stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.openTickPaint, stream);
        SerialUtils.writePaint(this.closeTickPaint, stream);
    }

}
